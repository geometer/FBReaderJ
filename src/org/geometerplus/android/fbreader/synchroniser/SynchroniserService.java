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
import android.os.IBinder;

import org.json.simple.JSONValue;

import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.auth.ServiceNetworkContext;

public class SynchroniserService extends Service implements IBookCollection.Listener, Runnable {
	private final ZLNetworkContext myNetworkContext = new ServiceNetworkContext(this);
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private static volatile Thread ourSynchronizationThread;

	private final List<Book> myQueue = Collections.synchronizedList(new LinkedList<Book>());
	private final Set<Book> myProcessed = new HashSet<Book>();
	private final Set<String> myHashesFromServer = new HashSet<String>();

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

	@Override
	public synchronized void run() {
		System.err.println("SYNCHRONIZER BINDED TO LIBRARY");
		myCollection.addListener(this);
		if (ourSynchronizationThread == null) {
			ourSynchronizationThread = new Thread() {
				public void run() {
					try {
						System.err.println("HELLO THREAD");
						myHashesFromServer.clear();
						try {
							myNetworkContext.perform(new PostRequest("all.hashes", null) {
								@Override
								public void processResponse(Object response) {
									myHashesFromServer.addAll((List)response);
								}
							});
							System.err.println("RECEIVED: " + myHashesFromServer.size() + " HASHES");
						} catch (Exception e) {
							e.printStackTrace();
							System.err.println("DO NOT SYNCHRONIZE: ALL HASHES REQUEST FAILED");
							return;
						}
						System.err.println("START SYNCRONIZATION");
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
							System.err.println("Processing " + book.getTitle() + " [" + book.File.getPath() + "]");
							uploadBookToServer(book);
						}
					} finally {
						System.err.println("BYE-BYE THREAD");
						ourSynchronizationThread = null;
					}
				}
			};
			ourSynchronizationThread.setPriority(Thread.MIN_PRIORITY);
			ourSynchronizationThread.start();
		}
	}

	private final static String DOMAIN = "demo.fbreader.org";
	private final static String BASE_URL = "https://" + DOMAIN + "/app/";

	private static abstract class PostRequest extends ZLNetworkRequest.PostWithMap {
		PostRequest(String app, Map<String,String> data) {
			super(BASE_URL + app, false);
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
		UploadRequest(File file) {
			super(BASE_URL + "book.upload", file, false);
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
				myHashesFromServer.addAll(hashes);
			} else {
				System.err.println("UNEXPECED RESPONSE: " + response);
			}
		}
	}

	private void uploadBookToServer(Book book) {
		final ZLPhysicalFile file = book.File.getPhysicalFile();
		if (file == null) {
			return;
		}
		final String hash = myCollection.getHash(book);
		if (hash == null) {
			System.err.println("Failed: checksum not computed");
			return;
		}
		if (myHashesFromServer.contains(hash)) {
			System.err.println("HASH ALREADY IN THE TABLE");
			return;
		}
		final Map<String,Object> result = new HashMap<String,Object>();
		final PostRequest verificationRequest =
			new PostRequest("books.by.hash", Collections.singletonMap("sha1", hash)) {
				@Override
				public void processResponse(Object response) {
					result.put("result", response);
				}
			};
		try {
			myNetworkContext.perform(verificationRequest);
		} catch (ZLNetworkException e) {
			e.printStackTrace();
		}
		final String csrfToken = myNetworkContext.getCookieValue(DOMAIN, "csrftoken");
		final Object response = result.get("result");
		try {
			final List<Map<String,Object>> responseList = (List<Map<String,Object>>)response;
			if (responseList.isEmpty()) {
				try {
					final UploadRequest uploadRequest = new UploadRequest(file.javaFile());
					uploadRequest.addHeader("Referer", verificationRequest.getURL());
					uploadRequest.addHeader("X-CSRFToken", csrfToken);
					myNetworkContext.perform(uploadRequest);
				} catch (ZLNetworkException e) {
					e.printStackTrace();
				}
			} else {
				for (Map<String,Object> bookInfo : responseList) {
					System.err.println("BOOK ALREADY UPLOADED: " + bookInfo.get("id"));
					myHashesFromServer.addAll((List<String>)bookInfo.get("hashes"));
				}
			}
		} catch (Exception e) {
			System.err.println("UNEXPECTED RESPONSE: " + response);
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
