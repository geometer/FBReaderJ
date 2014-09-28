/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

public class SyncService extends Service implements IBookCollection.Listener {
	private static void log(String message) {
		Log.d("FBReader.Sync", message);
	}

	private enum Status {
		AlreadyUploaded(Book.SYNCHRONISED_LABEL),
		Uploaded(Book.SYNCHRONISED_LABEL),
		ToBeDeleted(Book.SYNC_DELETED_LABEL),
		Failure(Book.SYNC_FAILURE_LABEL),
		ServerError(null),
		SyncronizationDisabled(null),
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

	private static volatile Thread ourSynchronizationThread;
	private static volatile Thread ourQuickSynchronizationThread;

	private final List<Book> myQueue = Collections.synchronizedList(new LinkedList<Book>());

	private final Set<String> myActualHashesFromServer = new HashSet<String>();
	private final Set<String> myDeletedHashesFromServer = new HashSet<String>();
	private volatile boolean myHashTablesAreInitialized = false;

	private PendingIntent syncIntent() {
		return PendingIntent.getService(
			this, 0, new Intent(this, getClass()).setAction(SyncOperations.Action.SYNC), 0
		);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent != null ? intent.getAction() : SyncOperations.Action.SYNC;
		if (SyncOperations.Action.START.equals(action)) {
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
						AlarmManager.INTERVAL_HALF_HOUR,
						syncIntent()
					);
					SQLiteCookieDatabase.init(SyncService.this);
					myCollection.bindToService(SyncService.this, myQuickSynchroniser);
				}
			});
		} else if (SyncOperations.Action.STOP.equals(action)) {
			final AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			alarmManager.cancel(syncIntent());
			log("stopped");
			stopSelf();
		} else if (SyncOperations.Action.SYNC.equals(action)) {
			SQLiteCookieDatabase.init(this);
			myCollection.bindToService(this, myQuickSynchroniser);
			myCollection.bindToService(this, myStandardSynchroniser);
		} else if (SyncOperations.Action.QUICK_SYNC.equals(action)) {
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
		if (book.File.getPhysicalFile() != null) {
			myQueue.add(book);
		}
	}

	private synchronized void clearHashTables() {
		myActualHashesFromServer.clear();
		myDeletedHashesFromServer.clear();
		myHashTablesAreInitialized = false;
	}

	private synchronized void initHashTables() {
		if (myHashTablesAreInitialized) {
			return;
		}

		try {
			myBookUploadContext.reloadCookie();
			myBookUploadContext.perform(new PostRequest("all.hashes", null) {
				@Override
				public void processResponse(Object response) {
					final Map<String,List<String>> map = (Map<String,List<String>>)response;
					myActualHashesFromServer.addAll(map.get("actual"));
					myDeletedHashesFromServer.addAll(map.get("deleted"));
					myHashTablesAreInitialized = true;
				}
			});
			log(String.format("RECEIVED: %s/%s HASHES", myActualHashesFromServer.size(), myDeletedHashesFromServer.size()));
		} catch (SyncronizationDisabledException e) {
			throw e;
		} catch (Exception e) {
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
							clearHashTables();
							for (BookQuery q = new BookQuery(new Filter.Empty(), 20);; q = q.next()) {
								final List<Book> books = myCollection.books(q);
								if (books.isEmpty()) {
									break;
								}
								for (Book b : books) {
									addBook(b);
								}
							}
							while (!myQueue.isEmpty()) {
								final Book book = myQueue.remove(0);
								++count;
								final Status status = uploadBookToServer(book);
								if (status.Label != null) {
									for (String label : Status.AllLabels) {
										if (status.Label.equals(label)) {
											book.addLabel(label);
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
		Status Result = Status.Failure;

		UploadRequest(File file) {
			super(SyncOptions.BASE_URL + "app/book.upload", file, false);
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
			if (error != null) {
				log("UPLOAD FAILURE: " + error);
				if ("ALREADY_UPLOADED".equals(code)) {
					Result = Status.AlreadyUploaded;
				}
			} else if (id != null && hashes != null) {
				log("UPLOADED SUCCESSFULLY: " + id);
				myActualHashesFromServer.addAll(hashes);
				Result = Status.Uploaded;
			} else {
				log("UNEXPECED RESPONSE: " + response);
			}
		}
	}

	private Status uploadBookToServer(Book book) {
		try {
			return uploadBookToServerInternal(book);
		} catch (SyncronizationDisabledException e) {
			return Status.SyncronizationDisabled;
		}
	}

	private Status uploadBookToServerInternal(Book book) {
		final File file = book.File.getPhysicalFile().javaFile();
		if (file.length() > 30 * 1024 * 1024) {
			return Status.Failure;
		}
		final String hash = myCollection.getHash(book, false);
		final boolean force = book.labels().contains(Book.SYNC_TOSYNC_LABEL);
		if (hash == null) {
			return Status.HashNotComputed;
		} else if (myActualHashesFromServer.contains(hash)) {
			return Status.AlreadyUploaded;
		} else if (!force && myDeletedHashesFromServer.contains(hash)) {
			return Status.ToBeDeleted;
		} else if (!force && book.labels().contains(Book.SYNC_FAILURE_LABEL)) {
			return Status.FailedPreviuousTime;
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
		} catch (ZLNetworkException e) {
			e.printStackTrace();
			return Status.ServerError;
		}
		final String csrfToken = myBookUploadContext.getCookieValue(SyncOptions.DOMAIN, "csrftoken");
		try {
			final String status = (String)result.get("status");
			if ((force && !"found".equals(status)) || "not found".equals(status)) {
				try {
					final UploadRequest uploadRequest = new UploadRequest(file);
					uploadRequest.addHeader("Referer", verificationRequest.getURL());
					uploadRequest.addHeader("X-CSRFToken", csrfToken);
					myBookUploadContext.perform(uploadRequest);
					return uploadRequest.Result;
				} catch (ZLNetworkException e) {
					e.printStackTrace();
					return Status.ServerError;
				}
			} else {
				final List<String> hashes = (List<String>)result.get("hashes");
				if ("found".equals(status)) {
					myActualHashesFromServer.addAll(hashes);
					return Status.AlreadyUploaded;
				} else /* if ("deleted".equals(status)) */ {
					myDeletedHashesFromServer.addAll(hashes);
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
						sendBroadcast(new Intent(SyncOperations.UPDATED));
					}
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
