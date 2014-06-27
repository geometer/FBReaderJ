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
import org.geometerplus.android.fbreader.network.ServiceNetworkContext;

public class SynchroniserService extends Service implements IBookCollection.Listener, Runnable {
	private final ZLNetworkContext myNetworkContext = new ServiceNetworkContext(this);
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private static volatile Thread ourSynchronizationThread;

	private final List<Book> myQueue = Collections.synchronizedList(new LinkedList<Book>());
	private final Set<Book> myProcessed = new HashSet<Book>();

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
					System.err.println("HELLO THREAD");
					try {
						ourSynchronizationThread.sleep(5000);
					} catch (InterruptedException e) {
					}
					System.err.println("START SYNCRONIZING");
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
						uploadBookToServer(book.File.getPhysicalFile());
					}
					System.err.println("BYE-BYE THREAD");
					ourSynchronizationThread = null;
				}
			};
			ourSynchronizationThread.setPriority(Thread.MIN_PRIORITY);
			ourSynchronizationThread.start();
		}
	}

	private static String toJSON(Object object) {
		final StringWriter writer = new StringWriter();
		try {
			JSONValue.writeJSONString(object, writer);
		} catch (IOException e) {
			throw new RuntimeException("JSON serialization failed", e);
		}
		return writer.toString();
	}

	private final static String DOMAIN = "demo.fbreader.org";
	private final static String BASE_URL = "https://" + DOMAIN + "/app/";

	private static abstract class JsonRequest extends ZLNetworkRequest.PostWithBody {
		JsonRequest(String app, Object data) {
			super(BASE_URL + app, toJSON(data), false);
		}

		@Override
		public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			final StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			processResponse(JSONValue.parse(buffer.toString()));
		}

		protected abstract void processResponse(Object response);
	}

	private static final class UploadRequest extends ZLNetworkRequest.FileUpload {
		UploadRequest(File file) {
			super(BASE_URL + "book.upload", file, false);
		}

		@Override
		public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			final StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			final Object response = JSONValue.parse(buffer.toString());
			String id = null;
			String error = null;
			try {
				if (response instanceof Map) {
					id = (String)((Map)response).get("id");
					error = (String)((Map)response).get("error");
				}
			} catch (Exception e) {
				// ignore
			}
			if (error != null) {
				System.err.println("UPLOAD FAILURE: " + error);
			} else if (id != null) {
				System.err.println("UPLOADED SUCCESSFULLY: " + id);
			} else {
				System.err.println("UNEXPECED RESPONSE: " + response);
			}
		}
	}

	private void uploadBookToServer(ZLPhysicalFile file) {
		final UID uid = BookUtil.createUid(file, "SHA-1");
		if (uid == null) {
			System.err.println("Failed: SHA-1 checksum not computed");
			return;
		}
		final Map<String,Object> result = new HashMap<String,Object>();
		final JsonRequest verificationRequest =
			new JsonRequest("books.by.hash", Collections.singletonMap("sha1", uid.Id)) {
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
			final List responseList = (List)response;
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
				final Map<String,String> firstBookInfo = (Map<String,String>)responseList.get(0);
				System.err.println("BOOK ALREADY UPLOADED: " + firstBookInfo.get("id"));
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
