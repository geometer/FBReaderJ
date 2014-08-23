/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.options.Config;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.api.FBReaderIntents;

public class BookCollectionShadow extends AbstractBookCollection implements ServiceConnection {
	private volatile Context myContext;
	private volatile LibraryInterface myInterface;
	private final List<Runnable> myOnBindActions = new LinkedList<Runnable>();

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (!hasListeners()) {
				return;
			}

			try {
				final String type = intent.getStringExtra("type");
				if (LibraryService.BOOK_EVENT_ACTION.equals(intent.getAction())) {
					final Book book = SerializerUtil.deserializeBook(intent.getStringExtra("book"));
					fireBookEvent(BookEvent.valueOf(type), book);
				} else {
					fireBuildEvent(Status.valueOf(type));
				}
			} catch (Exception e) {
				// ignore
			}
		}
	};

	public synchronized void bindToService(Context context, Runnable onBindAction) {
		if (myInterface != null && myContext == context) {
			if (onBindAction != null) {
				Config.Instance().runOnConnect(onBindAction);
			}
		} else {
			if (onBindAction != null) {
				myOnBindActions.add(onBindAction);
			}
			context.bindService(
				FBReaderIntents.internalIntent(FBReaderIntents.Action.LIBRARY_SERVICE),
				this,
				LibraryService.BIND_AUTO_CREATE
			);
			myContext = context;
		}
	}

	public synchronized void unbind() {
		if (myContext != null && myInterface != null) {
			try {
				myContext.unregisterReceiver(myReceiver);
			} catch (IllegalArgumentException e) {
				// called before regisration, that's ok
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				myContext.unbindService(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			myInterface = null;
			myContext = null;
		}
	}

	public synchronized void reset(boolean force) {
		if (myInterface != null) {
			try {
				myInterface.reset(force);
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized int size() {
		if (myInterface == null) {
			return 0;
		}
		try {
			return myInterface.size();
		} catch (RemoteException e) {
			return 0;
		}
	}

	public synchronized Status status() {
		if (myInterface == null) {
			return Status.NotStarted;
		}
		try {
			return Status.valueOf(myInterface.status());
		} catch (Throwable t) {
			return Status.NotStarted;
		}
	}

	public synchronized List<Book> books(BookQuery query) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookList(myInterface.books(SerializerUtil.serialize(query)));
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized boolean hasBooks(Filter filter) {
		if (myInterface == null) {
			return false;
		}
		try {
			return myInterface.hasBooks(SerializerUtil.serialize(new BookQuery(filter, 1)));
		} catch (RemoteException e) {
			return false;
		}
	}

	public synchronized List<Book> recentBooks() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookList(myInterface.recentBooks());
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized Book getRecentBook(int index) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getRecentBook(index));
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized Book getBookByFile(ZLFile file) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByFile(file.getPath()));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookById(long id) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookById(id));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookByUid(UID uid) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByUid(uid.Type, uid.Id));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookByHash(String hash) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByHash(hash));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized List<Author> authors() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			final List<String> strings = myInterface.authors();
			final List<Author> authors = new ArrayList<Author>(strings.size());
			for (String s : strings) {
				authors.add(Util.stringToAuthor(s));
			}
			return authors;
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized List<Tag> tags() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			final List<String> strings = myInterface.tags();
			final List<Tag> tags = new ArrayList<Tag>(strings.size());
			for (String s : strings) {
				tags.add(Util.stringToTag(s));
			}
			return tags;
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized boolean hasSeries() {
		if (myInterface != null) {
			try {
				return myInterface.hasSeries();
			} catch (RemoteException e) {
			}
		}
		return false;
	}

	public synchronized List<String> series() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return myInterface.series();
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized List<String> titles(BookQuery query) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return myInterface.titles(SerializerUtil.serialize(query));
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized List<String> firstTitleLetters() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return myInterface.firstTitleLetters();
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized boolean saveBook(Book book) {
		if (myInterface == null) {
			return false;
		}
		try {
			return myInterface.saveBook(SerializerUtil.serialize(book));
		} catch (RemoteException e) {
			return false;
		}
	}

	public synchronized void removeBook(Book book, boolean deleteFromDisk) {
		if (myInterface != null) {
			try {
				myInterface.removeBook(SerializerUtil.serialize(book), deleteFromDisk);
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void addBookToRecentList(Book book) {
		if (myInterface != null) {
			try {
				myInterface.addBookToRecentList(SerializerUtil.serialize(book));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized List<String> labels() {
		if (myInterface != null) {
			try {
				return myInterface.labels();
			} catch (RemoteException e) {
			}
		}
		return Collections.emptyList();
	}

	public String getHash(Book book, boolean force) {
		if (myInterface == null) {
			return null;
		}
		try {
			return myInterface.getHash(SerializerUtil.serialize(book), force);
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		if (myInterface == null) {
			return null;
		}

		try {
			final PositionWithTimestamp pos = myInterface.getStoredPosition(bookId);
			if (pos == null) {
				return null;
			}

			return new ZLTextFixedPosition.WithTimestamp(
				pos.ParagraphIndex, pos.ElementIndex, pos.CharIndex, pos.Timestamp
			);
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized void storePosition(long bookId, ZLTextPosition position) {
		if (position != null && myInterface != null) {
			try {
				myInterface.storePosition(bookId, new PositionWithTimestamp(position));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized boolean isHyperlinkVisited(Book book, String linkId) {
		if (myInterface == null) {
			return false;
		}

		try {
			return myInterface.isHyperlinkVisited(SerializerUtil.serialize(book), linkId);
		} catch (RemoteException e) {
			return false;
		}
	}

	public synchronized void markHyperlinkAsVisited(Book book, String linkId) {
		if (myInterface != null) {
			try {
				myInterface.markHyperlinkAsVisited(SerializerUtil.serialize(book), linkId);
			} catch (RemoteException e) {
			}
		}
	}

	@Override
	public synchronized ZLImage getCover(Book book, int maxWidth, int maxHeight) {
		if (myInterface == null) {
			return null;
		}
		try {
			final boolean[] delayed = new boolean[1];
			return new ZLBitmapImage(myInterface.getCover(SerializerUtil.serialize(book), maxWidth, maxHeight, delayed));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized List<Bookmark> bookmarks(BookmarkQuery query) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeBookmarkList(
				myInterface.bookmarks(SerializerUtil.serialize(query))
			);
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized void saveBookmark(Bookmark bookmark) {
		if (myInterface != null) {
			try {
				bookmark.update(SerializerUtil.deserializeBookmark(
					myInterface.saveBookmark(SerializerUtil.serialize(bookmark))
				));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void deleteBookmark(Bookmark bookmark) {
		if (myInterface != null) {
			try {
				myInterface.deleteBookmark(SerializerUtil.serialize(bookmark));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized HighlightingStyle getHighlightingStyle(int styleId) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeStyle(myInterface.getHighlightingStyle(styleId));
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized List<HighlightingStyle> highlightingStyles() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return SerializerUtil.deserializeStyleList(myInterface.highlightingStyles());
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	public synchronized void saveHighlightingStyle(HighlightingStyle style) {
		if (myInterface != null) {
			try {
				myInterface.saveHighlightingStyle(SerializerUtil.serialize(style));
			} catch (RemoteException e) {
				// ignore
			}
		}
	}

	public synchronized void rescan(String path) {
		if (myInterface != null) {
			try {
				myInterface.rescan(path);
			} catch (RemoteException e) {
				// ignore
			}
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceConnected(ComponentName name, IBinder service) {
		myInterface = LibraryInterface.Stub.asInterface(service);
		while (!myOnBindActions.isEmpty()) {
			Config.Instance().runOnConnect(myOnBindActions.remove(0));
		}
		if (myContext != null) {
			myContext.registerReceiver(myReceiver, new IntentFilter(LibraryService.BOOK_EVENT_ACTION));
			myContext.registerReceiver(myReceiver, new IntentFilter(LibraryService.BUILD_EVENT_ACTION));
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
	}
}
