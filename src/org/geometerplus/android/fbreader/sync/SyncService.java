/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.sync;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import org.json.simple.JSONValue;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.fbreader.network.sync.SyncData;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

public class SyncService extends Service implements IBookCollection.Listener<Book> {
	private static void log(String message) {
		Log.d("FBReader.Sync", message);
	}

	private enum Status {
		AlreadyUploaded(Book.SYNCHRONISED_LABEL),
		Uploaded(Book.SYNCHRONISED_LABEL),
		ToBeDeleted(Book.SYNC_DELETED_LABEL),
		Failure(Book.SYNC_FAILURE_LABEL),
		AuthenticationError(null),
		ServerError(null),
		SynchronizationDisabled(null),
		FailedPreviuousTime(null),
		HashNotComputed(null);

		private static final List<String> AllLabels = Arrays.asList(
			Book.SYNCHRONISED_LABEL,
			Book.SYNC_FAILURE_LABEL,
			Book.SYNC_DELETED_LABEL,
			Book.SYNC_TOSYNC_LABEL
		);

		public final String Label;

		Status(String label) {
			Label = label;
		}
	}

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private final SyncOptions mySyncOptions = new SyncOptions();
	private final SyncData mySyncData = new SyncData();

	private final SyncNetworkContext myBookUploadContext =
		new SyncNetworkContext(this, mySyncOptions, mySyncOptions.UploadAllBooks);
	private final SyncNetworkContext mySyncPositionsContext =
		new SyncNetworkContext(this, mySyncOptions, mySyncOptions.Positions);
	private final SyncNetworkContext mySyncBookmarksContext =
		new SyncNetworkContext(this, mySyncOptions, mySyncOptions.Bookmarks);

	private static volatile Thread ourSynchronizationThread;
	private static volatile Thread ourQuickSynchronizationThread;

	private final List<Book> myQueue = Collections.synchronizedList(new LinkedList<Book>());

	private static final class Hashes {
		final Set<String> Actual = new HashSet<String>();
		final Set<String> Deleted = new HashSet<String>();
		volatile boolean Initialised = false;

		void clear() {
			Actual.clear();
			Deleted.clear();
			Initialised = false;
		}

		void addAll(Collection<String> actual, Collection<String> deleted) {
			if (actual != null) {
				Actual.addAll(actual);
			}
			if (deleted != null) {
				Deleted.addAll(deleted);
			}
		}

		@Override
		public String toString() {
			return String.format(
				"%s/%s HASHES (%s)",
				Actual.size(),
				Deleted.size(),
				Initialised ? "complete" : "partial"
			);
		}
	};

	private final Hashes myHashesFromServer = new Hashes();

	private PendingIntent syncIntent() {
		return PendingIntent.getService(
			this, 0, new Intent(this, getClass()).setAction(FBReaderIntents.Action.SYNC_SYNC), 0
		);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent != null ? intent.getAction() : FBReaderIntents.Action.SYNC_SYNC;
		if (FBReaderIntents.Action.SYNC_START.equals(action)) {
			final AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			alarmManager.cancel(syncIntent());

			final Config config = Config.Instance();
			config.runOnConnect(new Runnable() {
				public void run() {
					config.requestAllValuesForGroup("Sync");
					config.requestAllValuesForGroup("SyncData");

					if (!mySyncOptions.Enabled.getValue()) {
						log("disabled");
						return;
					}
					log("enabled");
					alarmManager.setInexactRepeating(
						AlarmManager.ELAPSED_REALTIME,
						SystemClock.elapsedRealtime(),
						AlarmManager.INTERVAL_HOUR,
						syncIntent()
					);
					SQLiteCookieDatabase.init(SyncService.this);
					myCollection.bindToService(SyncService.this, myQuickSynchroniser);
				}
			});
		} else if (FBReaderIntents.Action.SYNC_STOP.equals(action)) {
			final AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			alarmManager.cancel(syncIntent());
			log("stopped");
			stopSelf();
		} else if (FBReaderIntents.Action.SYNC_SYNC.equals(action)) {
			SQLiteCookieDatabase.init(this);
			myCollection.bindToService(this, myQuickSynchroniser);
			myCollection.bindToService(this, myStandardSynchroniser);
		} else if (FBReaderIntents.Action.SYNC_QUICK_SYNC.equals(action)) {
			log("quick sync");
			SQLiteCookieDatabase.init(this);
			myCollection.bindToService(this, myQuickSynchroniser);
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void addBook(Book book) {
		if (BookUtil.fileByBook(book).getPhysicalFile() != null) {
			myQueue.add(book);
		}
	}

	private synchronized void initHashTables() {
		if (myHashesFromServer.Initialised) {
			return;
		}

		try {
			myBookUploadContext.reloadCookie();
			final int pageSize = 500;
			final Map<String,String> data = new HashMap<String,String>();
			data.put("page_size", String.valueOf(pageSize));
			for (int pageNo = 0; !myHashesFromServer.Initialised; ++pageNo) {
				data.put("page_no", String.valueOf(pageNo));
				myBookUploadContext.perform(new PostRequest("all.hashes.paged", data) {
					@Override
					public void processResponse(Object response) {
						final Map<String,List<String>> map = (Map<String,List<String>>)response;
						final List<String> actualHashes = map.get("actual");
						final List<String> deletedHashes = map.get("deleted");
						myHashesFromServer.addAll(actualHashes, deletedHashes);
						if (actualHashes.size() < pageSize && deletedHashes.size() < pageSize) {
							myHashesFromServer.Initialised = true;
						}
					}
				});
				log("RECEIVED: " + myHashesFromServer.toString());
			}
		} catch (SynchronizationDisabledException e) {
			myHashesFromServer.clear();
			throw e;
		} catch (Exception e) {
			myHashesFromServer.clear();
			e.printStackTrace();
		}
	}

	private final Runnable myStandardSynchroniser = new Runnable() {
		@Override
		public synchronized void run() {
			if (!mySyncOptions.Enabled.getValue()) {
				return;
			}
			myBookUploadContext.reloadCookie();

			myCollection.addListener(SyncService.this);
			if (ourSynchronizationThread == null) {
				ourSynchronizationThread = new Thread() {
					public void run() {
						final long start = System.currentTimeMillis();
						int count = 0;

						final Map<Status,Integer> statusCounts = new HashMap<Status,Integer>();
						try {
							myHashesFromServer.clear();
							for (BookQuery q = new BookQuery(new Filter.Empty(), 20);; q = q.next()) {
								final List<Book> books = myCollection.books(q);
								if (books.isEmpty()) {
									break;
								}
								for (Book b : books) {
									addBook(b);
								}
							}
							Status status = null;
							while (!myQueue.isEmpty() && status != Status.AuthenticationError) {
								final Book book = myQueue.remove(0);
								++count;
								status = uploadBookToServer(book);
								if (status.Label != null) {
									for (String label : Status.AllLabels) {
										if (status.Label.equals(label)) {
											book.addNewLabel(label);
										} else {
											book.removeLabel(label);
										}
									}
									myCollection.saveBook(book);
								}
								final Integer sc = statusCounts.get(status);
								statusCounts.put(status, sc != null ? sc + 1 : 1);
							}
						} finally {
							log("SYNCHRONIZATION FINISHED IN " + (System.currentTimeMillis() - start) + "msecs");
							log("TOTAL BOOKS PROCESSED: " + count);
							for (Status value : Status.values()) {
								log("STATUS " + value + ": " + statusCounts.get(value));
							}
							ourSynchronizationThread = null;
						}
					}
				};
				ourSynchronizationThread.setPriority(Thread.MIN_PRIORITY);
				ourSynchronizationThread.start();
			}
		}
	};

	private final Runnable myQuickSynchroniser = new Runnable() {
		@Override
		public synchronized void run() {
			if (!mySyncOptions.Enabled.getValue()) {
				return;
			}
			mySyncPositionsContext.reloadCookie();

			if (ourQuickSynchronizationThread == null) {
				ourQuickSynchronizationThread = new Thread() {
					public void run() {
						try {
							syncPositions();
							syncCustomShelves();
							BookmarkSyncUtil.sync(mySyncBookmarksContext, myCollection);
						} finally {
							ourQuickSynchronizationThread = null;
						}
					}
				};
				ourQuickSynchronizationThread.setPriority(Thread.MAX_PRIORITY);
				ourQuickSynchronizationThread.start();
			}
		}
	};

	private static abstract class PostRequest extends JsonRequest {
		PostRequest(String app, Map<String,String> data) {
			super(SyncOptions.BASE_URL + "app/" + app);
			if (data != null) {
				for (Map.Entry<String, String> entry : data.entrySet()) {
					addPostParameter(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private final class UploadRequest extends ZLNetworkRequest.FileUpload {
		private final Book myBook;
		private final String myHash;
		Status Result = Status.Failure;

		UploadRequest(File file, Book book, String hash) {
			super(SyncOptions.BASE_URL + "app/book.upload", file, false);
			myBook = book;
			myHash = hash;
		}

		@Override
		public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
			final Object response = JSONValue.parse(new InputStreamReader(stream));
			String id = null;
			List<String> hashes = null;
			String error = null;
			String code = null;
			try {
				final List<Map> responseList = (List<Map>)response;
				if (responseList.size() == 1) {
					final Map resultMap = (Map)responseList.get(0).get("result");
					id = (String)resultMap.get("id");
					hashes = (List<String>)resultMap.get("hashes");
					error = (String)resultMap.get("error");
					code = (String)resultMap.get("code");
				}
			} catch (Exception e) {
				// ignore
			}

			if (hashes != null && !hashes.isEmpty()) {
				myHashesFromServer.addAll(hashes, null);
				if (!hashes.contains(myHash)) {
					myCollection.setHash(myBook, hashes.get(0));
				}
			}
			if (error != null) {
				log("UPLOAD FAILURE: " + error);
				if ("ALREADY_UPLOADED".equals(code)) {
					Result = Status.AlreadyUploaded;
				}
			} else if (id != null) {
				log("UPLOADED SUCCESSFULLY: " + id);
				Result = Status.Uploaded;
			} else {
				log("UNEXPECED RESPONSE: " + response);
			}
		}
	}

	private Status uploadBookToServer(Book book) {
		try {
			return uploadBookToServerInternal(book);
		} catch (SynchronizationDisabledException e) {
			return Status.SynchronizationDisabled;
		}
	}

	private Status uploadBookToServerInternal(Book book) {
		final File file = BookUtil.fileByBook(book).getPhysicalFile().javaFile();
		final String hash = myCollection.getHash(book, false);
		final boolean force = book.hasLabel(Book.SYNC_TOSYNC_LABEL);
		if (hash == null) {
			return Status.HashNotComputed;
		} else if (myHashesFromServer.Actual.contains(hash)) {
			return Status.AlreadyUploaded;
		} else if (!force && myHashesFromServer.Actual.contains(hash)) {
			return Status.ToBeDeleted;
		} else if (!force && book.hasLabel(Book.SYNC_FAILURE_LABEL)) {
			return Status.FailedPreviuousTime;
		}
		if (file.length() > 120 * 1024 * 1024) {
			return Status.Failure;
		}

		initHashTables();

		final Map<String,Object> result = new HashMap<String,Object>();
		final PostRequest verificationRequest =
			new PostRequest("book.status.by.hash", Collections.singletonMap("sha1", hash)) {
				@Override
				public void processResponse(Object response) {
					result.putAll((Map)response);
				}
			};
		try {
			myBookUploadContext.perform(verificationRequest);
		} catch (ZLNetworkAuthenticationException e) {
			e.printStackTrace();
			return Status.AuthenticationError;
		} catch (ZLNetworkException e) {
			e.printStackTrace();
			return Status.ServerError;
		}
		final String csrfToken = myBookUploadContext.getCookieValue(SyncOptions.DOMAIN, "csrftoken");
		try {
			final String status = (String)result.get("status");
			if ((force && !"found".equals(status)) || "not found".equals(status)) {
				try {
					final UploadRequest uploadRequest = new UploadRequest(file, book, hash);
					uploadRequest.addHeader("Referer", verificationRequest.getURL());
					uploadRequest.addHeader("X-CSRFToken", csrfToken);
					myBookUploadContext.perform(uploadRequest);
					return uploadRequest.Result;
				} catch (ZLNetworkAuthenticationException e) {
					e.printStackTrace();
					return Status.AuthenticationError;
				} catch (ZLNetworkException e) {
					e.printStackTrace();
					return Status.ServerError;
				}
			} else {
				final List<String> hashes = (List<String>)result.get("hashes");
				if ("found".equals(status)) {
					myHashesFromServer.addAll(hashes, null);
					return Status.AlreadyUploaded;
				} else /* if ("deleted".equals(status)) */ {
					myHashesFromServer.addAll(null, hashes);
					return Status.ToBeDeleted;
				}
			}
		} catch (Exception e) {
			log("UNEXPECTED RESPONSE: " + result);
			return Status.ServerError;
		}
	}

	private void syncPositions() {
		try {
			mySyncPositionsContext.perform(new JsonRequest2(
				SyncOptions.BASE_URL + "sync/position.exchange", mySyncData.data(myCollection)
			) {
				@Override
				public void processResponse(Object response) {
					if (mySyncData.updateFromServer((Map<String,Object>)response)) {
						sendBroadcast(new Intent(FBReaderIntents.Event.SYNC_UPDATED));
					}
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void syncCustomShelves() {
	}

	@Override
	public void onDestroy() {
		myCollection.removeListener(this);
		myCollection.unbind();
		super.onDestroy();
	}

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
			default:
				break;
			case Added:
				addBook(book);
				break;
			case Opened:
				SyncOperations.quickSync(this, mySyncOptions);
				break;
		}
	}

	@Override
	public void onBuildEvent(IBookCollection.Status status) {
	}
}
