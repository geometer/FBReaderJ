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

package org.geometerplus.android.fbreader.libraryService;

import java.util.*;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.FileObserver;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.options.Config;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;

public class LibraryService extends Service {
	private static SQLiteBooksDatabase ourDatabase;
	private static final Object ourDatabaseLock = new Object();

	static final String BOOK_EVENT_ACTION = "fbreader.library_service.book_event";
	static final String BUILD_EVENT_ACTION = "fbreader.library_service.build_event";

	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);

	private static final class Observer extends FileObserver {
		private static final int MASK =
			MOVE_SELF | MOVED_TO | MOVED_FROM | DELETE_SELF | DELETE | CLOSE_WRITE | ATTRIB;

		private final String myPrefix;
		private final BookCollection myCollection;

		public Observer(String path, BookCollection collection) {
			super(path, MASK);
			myPrefix = path + '/';
			myCollection = collection;
		}

		@Override
		public void onEvent(int event, String path) {
			event = event & ALL_EVENTS;
			System.err.println("Event " + event + " on " + path);
			switch (event) {
				case MOVE_SELF:
					// TODO: File(path) removed; stop watching (?)
					break;
				case MOVED_TO:
					myCollection.rescan(myPrefix + path);
					break;
				case MOVED_FROM:
				case DELETE:
					myCollection.rescan(myPrefix + path);
					break;
				case DELETE_SELF:
					// TODO: File(path) removed; watching is stopped automatically (?)
					break;
				case CLOSE_WRITE:
				case ATTRIB:
					myCollection.rescan(myPrefix + path);
					break;
				default:
					System.err.println("Unexpected event " + event + " on " + myPrefix + path);
					break;
			}
		}
	}

	public final class LibraryImplementation extends LibraryInterface.Stub {
		private final BooksDatabase myDatabase;
		private final List<FileObserver> myFileObservers = new LinkedList<FileObserver>();
		private BookCollection myCollection;

		LibraryImplementation(BooksDatabase db) {
			myDatabase = db;
			myCollection = new BookCollection(myDatabase, Paths.bookPath());
			reset(true);
		}

		public void reset(final boolean force) {
			Config.Instance().runOnConnect(new Runnable() {
				public void run() {
					resetInternal(force);
				}
			});
		}

		private void resetInternal(boolean force) {
			final List<String> bookDirectories = Paths.bookPath();
			if (!force &&
				myCollection.status() != BookCollection.Status.NotStarted &&
				bookDirectories.equals(myCollection.BookDirectories)
			) {
				return;
			}

			deactivate();
			myFileObservers.clear();

			myCollection = new BookCollection(myDatabase, bookDirectories);
			for (String dir : bookDirectories) {
				final Observer observer = new Observer(dir, myCollection);
				observer.startWatching();
				myFileObservers.add(observer);
			}

			myCollection.addListener(new BookCollection.Listener() {
				public void onBookEvent(BookEvent event, Book book) {
					final Intent intent = new Intent(BOOK_EVENT_ACTION);
					intent.putExtra("type", event.toString());
					intent.putExtra("book", SerializerUtil.serialize(book));
					sendBroadcast(intent);
				}

				public void onBuildEvent(BookCollection.Status status) {
					final Intent intent = new Intent(BUILD_EVENT_ACTION);
					intent.putExtra("type", status.toString());
					sendBroadcast(intent);
				}
			});
			myCollection.startBuild();
		}

		public void deactivate() {
			for (FileObserver observer : myFileObservers) {
				observer.stopWatching();
			}
		}

		public String status() {
			return myCollection.status().toString();
		}

		public int size() {
			return myCollection.size();
		}

		public List<String> books(String query) {
			return SerializerUtil.serializeBookList(
				myCollection.books(SerializerUtil.deserializeBookQuery(query))
			);
		}

		public boolean hasBooks(String query) {
			return myCollection.hasBooks(SerializerUtil.deserializeBookQuery(query).Filter);
		}

		public List<String> recentBooks() {
			return SerializerUtil.serializeBookList(myCollection.recentBooks());
		}

		public String getRecentBook(int index) {
			return SerializerUtil.serialize(myCollection.getRecentBook(index));
		}

		public String getBookByFile(String file) {
			return SerializerUtil.serialize(myCollection.getBookByFile(ZLFile.createFileByPath(file)));
		}

		public String getBookById(long id) {
			return SerializerUtil.serialize(myCollection.getBookById(id));
		}

		public String getBookByUid(String type, String id) {
			return SerializerUtil.serialize(myCollection.getBookByUid(new UID(type, id)));
		}

		public String getBookByHash(String hash) {
			return SerializerUtil.serialize(myCollection.getBookByHash(hash));
		}

		public List<String> authors() {
			final List<Author> authors = myCollection.authors();
			final List<String> strings = new ArrayList<String>(authors.size());
			for (Author a : authors) {
				strings.add(Util.authorToString(a));
			}
			return strings;
		}

		public boolean hasSeries() {
			return myCollection.hasSeries();
		}

		public List<String> series() {
			return myCollection.series();
		}

		public List<String> tags() {
			final List<Tag> tags = myCollection.tags();
			final List<String> strings = new ArrayList<String>(tags.size());
			for (Tag t : tags) {
				strings.add(Util.tagToString(t));
			}
			return strings;
		}

		public List<String> titles(String query) {
			return myCollection.titles(SerializerUtil.deserializeBookQuery(query));
		}

		public List<String> firstTitleLetters() {
			return myCollection.firstTitleLetters();
		}

		public boolean saveBook(String book) {
			return myCollection.saveBook(SerializerUtil.deserializeBook(book));
		}

		public void removeBook(String book, boolean deleteFromDisk) {
			myCollection.removeBook(SerializerUtil.deserializeBook(book), deleteFromDisk);
		}

		public void addBookToRecentList(String book) {
			myCollection.addBookToRecentList(SerializerUtil.deserializeBook(book));
		}

		public List<String> labels() {
			return myCollection.labels();
		}

		public PositionWithTimestamp getStoredPosition(long bookId) {
			final ZLTextPosition position = myCollection.getStoredPosition(bookId);
			return position != null ? new PositionWithTimestamp(position) : null;
		}

		public void storePosition(long bookId, PositionWithTimestamp pos) {
			if (pos == null) {
				return;
			}
			myCollection.storePosition(bookId, new ZLTextFixedPosition.WithTimestamp(
				pos.ParagraphIndex, pos.ElementIndex, pos.CharIndex, pos.Timestamp
			));
		}

		public boolean isHyperlinkVisited(String book, String linkId) {
			return myCollection.isHyperlinkVisited(SerializerUtil.deserializeBook(book), linkId);
		}

		public void markHyperlinkAsVisited(String book, String linkId) {
			myCollection.markHyperlinkAsVisited(SerializerUtil.deserializeBook(book), linkId);
		}

		@Override
		public Bitmap getCover(String book, int maxWidth, int maxHeight, boolean[] delayed) {
			final ZLImage image =
				myCollection.getCover(SerializerUtil.deserializeBook(book), maxWidth, maxHeight);
			if (image == null) {
				return null;
			}
			if (image instanceof ZLImageProxy) {
				myImageSynchronizer.synchronize((ZLImageProxy)image, null);
			}
			final ZLAndroidImageManager manager =
				(ZLAndroidImageManager)ZLAndroidImageManager.Instance();
			final ZLAndroidImageData data = manager.getImageData(image);
			return data != null ? data.getBitmap(maxWidth, maxHeight) : null;
		}

		public List<String> bookmarks(String query) {
			return SerializerUtil.serializeBookmarkList(myCollection.bookmarks(
				SerializerUtil.deserializeBookmarkQuery(query)
			));
		}

		public String saveBookmark(String serialized) {
			final Bookmark bookmark = SerializerUtil.deserializeBookmark(serialized);
			myCollection.saveBookmark(bookmark);
			return SerializerUtil.serialize(bookmark);
		}

		public void deleteBookmark(String serialized) {
			myCollection.deleteBookmark(SerializerUtil.deserializeBookmark(serialized));
		}

		public List<String> notes(String query) {
			return SerializerUtil.serializeNoteList(myCollection.notes(
				SerializerUtil.deserializeNoteQuery(query)
			));
		}

		public String saveNote(String serialized) {
			final Note note = SerializerUtil.deserializeNote(serialized);
			myCollection.saveNote(note);
			return SerializerUtil.serialize(note);
		}

		public void deleteNote(String serialized) {
			myCollection.deleteNote(SerializerUtil.deserializeNote(serialized));
		}

		public String getHighlightingStyle(int styleId) {
			return SerializerUtil.serialize(myCollection.getHighlightingStyle(styleId));
		}

		public List<String> highlightingStyles() {
			return SerializerUtil.serializeStyleList(myCollection.highlightingStyles());
		}

		public void saveHighlightingStyle(String style) {
			myCollection.saveHighlightingStyle(SerializerUtil.deserializeStyle(style));
		}

		public void rescan(String path) {
			myCollection.rescan(path);
		}

		public String getHash(String book, boolean force) {
			return myCollection.getHash(SerializerUtil.deserializeBook(book), force);
		}
	}

	private volatile LibraryImplementation myLibrary;

	@Override
	public void onStart(Intent intent, int startId) {
		onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return myLibrary;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		synchronized (ourDatabaseLock) {
			if (ourDatabase == null) {
				ourDatabase = new SQLiteBooksDatabase(LibraryService.this);
			}
		}
		myLibrary = new LibraryImplementation(ourDatabase);
	}

	@Override
	public void onDestroy() {
		if (myLibrary != null) {
			final LibraryImplementation l = myLibrary;
			myLibrary = null;
			l.deactivate();
		}
		myImageSynchronizer.clear();
		super.onDestroy();
	}
}
