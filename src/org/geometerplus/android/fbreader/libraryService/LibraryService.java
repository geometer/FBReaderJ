/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;
import java.util.LinkedList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.FileObserver;

import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;

public class LibraryService extends Service {
	private static final class Observer extends FileObserver {
		private static final int MASK =
			MOVE_SELF | MOVED_TO | MOVED_FROM | DELETE_SELF | DELETE | CLOSE_WRITE | ATTRIB;

		public Observer(String path) {
			super(path, MASK);
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
					// TODO: File(path) removed; File(path) added
					break;
				case MOVED_FROM:
				case DELETE:
					// TODO: File(path) removed
					break;
				case DELETE_SELF:
					// TODO: File(path) removed; watching is stopped automatically (?)
					break;
				case CLOSE_WRITE:
				case ATTRIB:
					// TODO: File(path) changed (added, removed?)
					break;
				default:
					System.err.println("Unexpected event " + event + " on " + path);
					break;
			}
		}
	}

	public final class LibraryImplementation extends LibraryInterface.Stub {
		private final BookCollection myCollection;
		private final List<FileObserver> myFileObservers = new LinkedList<FileObserver>();

		LibraryImplementation() {
			BooksDatabase database = SQLiteBooksDatabase.Instance();
			if (database == null) {
				database = new SQLiteBooksDatabase(LibraryService.this, "LIBRARY SERVICE");
			}
			myCollection = new BookCollection(database);
			for (String path : myCollection.bookDirectories()) {
				final Observer observer = new Observer(path);
				observer.startWatching();
				myFileObservers.add(observer);
			}

			final long start = System.currentTimeMillis();
			myCollection.addListener(new BookCollection.Listener() {
				public void onBookEvent(BookEvent event, Book book) {
					switch (event) {
						case Added:
							System.err.println("Added " + book.getTitle());
							break;
					}
				}

				public void onBuildEvent(BuildEvent event) {
					switch (event) {
						case Started:
							System.err.println("Build started");
							break;
						case Succeeded:
							System.err.println("Build succeeded");
							break;
						case Failed:
							System.err.println("Build failed");
							break;
						case Completed:
							System.err.println("Build completed with " + myCollection.size() + " books in " + (System.currentTimeMillis() - start) + " milliseconds");
							break;
					}
				}
			});
			myCollection.startBuild();
		}

		public void deactivate() {
			for (FileObserver observer : myFileObservers) {
				observer.stopWatching();
			}
		}

		public int size() {
			return myCollection.size();
		}

		public List<String> books(String pattern) {
			return SerializerUtil.serializeBookList(myCollection.books(pattern));
		}

		public List<String> recentBooks() {
			return SerializerUtil.serializeBookList(myCollection.recentBooks());
		}

		public List<String> favorites() {
			return SerializerUtil.serializeBookList(myCollection.favorites());
		}

		public String getRecentBook(int index) {
			return SerializerUtil.serialize(myCollection.getRecentBook(index));
		}

		public String getBookById(long id) {
			return SerializerUtil.serialize(myCollection.getBookById(id));
		}

		public void removeBook(String book, boolean deleteFromDisk) {
			myCollection.removeBook(SerializerUtil.deserializeBook(book), deleteFromDisk);
		}

		public void addBookToRecentList(String book) {
			myCollection.addBookToRecentList(SerializerUtil.deserializeBook(book));
		}

		public void setBookFavorite(String book, boolean favorite) {
			myCollection.setBookFavorite(SerializerUtil.deserializeBook(book), favorite);
		}

		public List<String> invisibleBookmarks(String book) {
			return SerializerUtil.serializeBookmarkList(
				myCollection.invisibleBookmarks(SerializerUtil.deserializeBook(book))
			);
		}

		public List<String> allBookmarks() {
			return SerializerUtil.serializeBookmarkList(myCollection.allBookmarks());
		}

		public String saveBookmark(String serialized) {
			final Bookmark bookmark = SerializerUtil.deserializeBookmark(serialized);
			myCollection.saveBookmark(bookmark);
			return SerializerUtil.serialize(bookmark);
		}

		public void deleteBookmark(String serialized) {
			myCollection.deleteBookmark(SerializerUtil.deserializeBookmark(serialized));
		}
	}

	private volatile LibraryImplementation myLibrary;

	@Override
	public void onStart(Intent intent, int startId) {
		onStartCommand(intent, 0, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.err.println("LibraryService started for intent " + intent);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		System.err.println("LibraryService binded for intent " + intent);
		return myLibrary;
	}

	@Override
	public void onCreate() {
		System.err.println("LibraryService.onCreate()");
		super.onCreate();
		myLibrary = new LibraryImplementation();
	}

	@Override
	public void onDestroy() {
		System.err.println("LibraryService.onDestroy()");
		if (myLibrary != null) {
			myLibrary.deactivate();
			myLibrary = null;
		}
		super.onDestroy();
	}
}
