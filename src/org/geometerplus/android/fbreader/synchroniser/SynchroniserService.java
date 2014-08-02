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

package org.geometerplus.android.fbreader.synchroniser;

import java.io.*;
import java.util.*;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import org.json.simple.JSONValue;

import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.auth.ServiceNetworkContext;

public class SynchroniserService extends Service implements IBookCollection.Listener, Runnable {
	enum SyncStatus {
		AlreadyUploaded(Book.SYNCHRONIZED_LABEL),
		Uploaded(Book.SYNCHRONIZED_LABEL),
		ToBeDeleted(Book.SYNC_DELETED_LABEL),
		Failure(Book.SYNC_FAILURE_LABEL),
		ServerError(null),
		SyncronizationDisabled(null),
		FailedPreviuousTime(null),
		HashNotComputed(null);

		private static final List<String> AllLabels = Arrays.asList(
			Book.SYNCHRONIZED_LABEL,
			Book.SYNC_FAILURE_LABEL,
			Book.SYNC_DELETED_LABEL,
			Book.SYNC_TOSYNC_LABEL
		);

		public final String Label;

		SyncStatus(String label) {
			Label = label;
		}
	}

	private final SyncOptions mySyncOptions = new SyncOptions();

	private static final class SyncronizationDisabledException extends RuntimeException {
	}

	private final ZLNetworkContext myNetworkContext = new ServiceNetworkContext(this) {
		private String myAccountName;

		@Override
		protected void perform(ZLNetworkRequest request, int socketTimeout, int connectionTimeout) throws ZLNetworkException {
			if (!canPerformRequest()) {
				throw new SyncronizationDisabledException();
			}
			final String accountName = getAccountName(SyncOptions.DOMAIN, SyncOptions.REALM);
			if (!MiscUtil.equals(myAccountName, accountName)) {
				reloadCookie();
				myAccountName = accountName;
			}
			super.perform(request, socketTimeout, connectionTimeout);
		}
	};
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private static volatile Thread ourSynchronizationThread;

	private final List<Book> myQueue = Collections.synchronizedList(new LinkedList<Book>());
	private final Set<Book> myProcessed = new HashSet<Book>();

	private final Set<String> myActualHashesFromServer = new HashSet<String>();
	private final Set<String> myDeletedHashesFromServer = new HashSet<String>();
	private volatile boolean myHashTablesAreInitialized = false;

	@Override
	public IBinder onBind(Intent intent) {
		SQLiteCookieDatabase.init(this);
		myCollection.bindToService(this, this);
		return null;
	}

	private void addBook(Book book) {
		if (!myProcessed.contains(book) && book.File.getPhysicalFile() != null) {
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
			myNetworkContext.perform(new PostRequest("all.hashes", null) {
				@Override
				public void processResponse(Object response) {
					final Map<String,List<String>> map = (Map<String,List<String>>)response;
					myActualHashesFromServer.addAll(map.get("actual"));
					myDeletedHashesFromServer.addAll(map.get("deleted"));
					myHashTablesAreInitialized = true;
				}
			});
			System.err.println(String.format("RECEIVED: %s/%s HASHES", myActualHashesFromServer.size(), myDeletedHashesFromServer.size()));
		} catch (SyncronizationDisabledException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean canPerformRequest() {
		if (!mySyncOptions.Enabled.getValue()) {
			return false;
		}

		switch (mySyncOptions.UploadAllBooks.getValue()) {
			default:
			case never:
				return false;
			case always:
			{
				final ConnectivityManager cm =
					(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
				return cm != null && cm.getActiveNetworkInfo().isConnected();
			}
			case viaWifi:
			{
				final ConnectivityManager cm =
					(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
				if (cm == null) {
					return false;
				}
				final NetworkInfo info = cm.getActiveNetworkInfo();
				return
					info != null &&
					info.isConnected() &&
					info.getType() == ConnectivityManager.TYPE_WIFI;
			}
		}
	}

	@Override
	public synchronized void run() {
		myCollection.addListener(this);
		if (ourSynchronizationThread == null) {
			ourSynchronizationThread = new Thread() {
				public void run() {
					try {
						sleep(10000);
					} catch (InterruptedException e) {
					}
					final long start = System.currentTimeMillis();
					int count = 0;
					final Map<SyncStatus,Integer> statusCounts = new HashMap<SyncStatus,Integer>();
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
							if (myProcessed.contains(book)) {
								continue;
							}
							myProcessed.add(book);
							++count;
							final SyncStatus status = uploadBookToServer(book);
							if (status.Label != null) {
								for (String label : SyncStatus.AllLabels) {
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
						System.err.println("SYNCHRONIZATION FINISHED IN " + (System.currentTimeMillis() - start) + "msecs");
						System.err.println("TOTAL BOOKS PROCESSED: " + count);
						for (SyncStatus value : SyncStatus.values()) {
							System.err.println("STATUS " + value + ": " + statusCounts.get(value));
						}
						ourSynchronizationThread = null;
					}
				}
			};
			ourSynchronizationThread.setPriority(Thread.MIN_PRIORITY);
			ourSynchronizationThread.start();
		}
	}

	private static abstract class PostRequest extends ZLNetworkRequest.PostWithMap {
		PostRequest(String app, Map<String,String> data) {
			super(SyncOptions.URL + "app/" + app, false);
			if (data != null) {
				for (Map.Entry<String, String> entry : data.entrySet()) {
					addPostParameter(entry.getKey(), entry.getValue());
				}
			}
		}

		@Override
		public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
			processResponse(JSONValue.parse(new InputStreamReader(stream)));
		}

		protected abstract void processResponse(Object response);
	}

	private final class UploadRequest extends ZLNetworkRequest.FileUpload {
		boolean Success = false;

		UploadRequest(File file) {
			super(SyncOptions.URL + "app/book.upload", file, false);
		}

		@Override
		public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
			final Object response = JSONValue.parse(new InputStreamReader(stream));
			String id = null;
			List<String> hashes = null;
			String error = null;
			try {
				final List<Map> responseList = (List<Map>)response;
				if (responseList.size() == 1) {
					final Map resultMap = (Map)responseList.get(0).get("result");
					id = (String)resultMap.get("id");
					hashes = (List<String>)resultMap.get("hashes");
					error = (String)resultMap.get("error");
				}
			} catch (Exception e) {
				// ignore
			}
			if (error != null) {
				System.err.println("UPLOAD FAILURE: " + error);
			} else if (id != null && hashes != null) {
				System.err.println("UPLOADED SUCCESSFULLY: " + id);
				myActualHashesFromServer.addAll(hashes);
				Success = true;
			} else {
				System.err.println("UNEXPECED RESPONSE: " + response);
			}
		}
	}

	private SyncStatus uploadBookToServer(Book book) {
		try {
			return uploadBookToServerInternal(book);
		} catch (SyncronizationDisabledException e) {
			return SyncStatus.SyncronizationDisabled;
		}
	}

	private SyncStatus uploadBookToServerInternal(Book book) {
		final File file = book.File.getPhysicalFile().javaFile();
		if (file.length() > 20 * 1024 * 1024) {
			return SyncStatus.Failure;
		}
		final String hash = myCollection.getHash(book);
		final boolean force = book.labels().contains(Book.SYNC_TOSYNC_LABEL);
		if (hash == null) {
			return SyncStatus.HashNotComputed;
		} else if (myActualHashesFromServer.contains(hash)) {
			return SyncStatus.AlreadyUploaded;
		} else if (!force && myDeletedHashesFromServer.contains(hash)) {
			return SyncStatus.ToBeDeleted;
		} else if (!force && book.labels().contains(Book.SYNC_FAILURE_LABEL)) {
			return SyncStatus.FailedPreviuousTime;
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
			myNetworkContext.perform(verificationRequest);
		} catch (ZLNetworkException e) {
			e.printStackTrace();
			return SyncStatus.ServerError;
		}
		final String csrfToken = myNetworkContext.getCookieValue(SyncOptions.DOMAIN, "csrftoken");
		try {
			final String status = (String)result.get("status");
			if ((force && !"found".equals(status)) || "not found".equals(status)) {
				try {
					final UploadRequest uploadRequest = new UploadRequest(file);
					uploadRequest.addHeader("Referer", verificationRequest.getURL());
					uploadRequest.addHeader("X-CSRFToken", csrfToken);
					myNetworkContext.perform(uploadRequest);
					return uploadRequest.Success ? SyncStatus.Uploaded : SyncStatus.Failure;
				} catch (ZLNetworkException e) {
					e.printStackTrace();
					return SyncStatus.ServerError;
				}
			} else {
				final List<String> hashes = (List<String>)result.get("hashes");
				if ("found".equals(status)) {
					myActualHashesFromServer.addAll(hashes);
					return SyncStatus.AlreadyUploaded;
				} else /* if ("deleted".equals(status)) */ {
					myDeletedHashesFromServer.addAll(hashes);
					return SyncStatus.ToBeDeleted;
				}
			}
		} catch (Exception e) {
			System.err.println("UNEXPECTED RESPONSE: " + result);
			return SyncStatus.ServerError;
		}
	}

	@Override
	public void onDestroy() {
		myCollection.removeListener(this);
		myCollection.unbind();
		System.err.println("SYNCHRONIZER UNBINDED FROM LIBRARY");
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
		}
	}

	@Override
	public void onBuildEvent(IBookCollection.Status status) {
	}
}
