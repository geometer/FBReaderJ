/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.zlibrary.core.options.Config;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.api.FBReaderIntents;

public class BookCollectionShadow extends AbstractBookCollection<Book> implements ServiceConnection {
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
				if (FBReaderIntents.Event.LIBRARY_BOOK.equals(intent.getAction())) {
					final Book book = SerializerUtil.deserializeBook(intent.getStringExtra("book"), BookCollectionShadow.this);
					fireBookEvent(BookEvent.valueOf(type), book);
				} else {
					fireBuildEvent(Status.valueOf(type));
				}
			} catch (Exception e) {
				// ignore
			}
		}
	};

	public synchronized boolean bindToService(Context context, Runnable onBindAction) {
		if (myInterface != null && myContext == context) {
			if (onBindAction != null) {
				Config.Instance().runOnConnect(onBindAction);
			}
			return true;
		} else {
			if (onBindAction != null) {
				synchronized (myOnBindActions) {
					myOnBindActions.add(onBindAction);
				}
			}
			final boolean result = context.bindService(
				FBReaderIntents.internalIntent(FBReaderIntents.Action.LIBRARY_SERVICE),
				this,
				Service.BIND_AUTO_CREATE
			);
			if (result) {
				myContext = context;
			}
			return result;
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

	public List<Book> books(final BookQuery query) {
		return listCall(new ListCallable<Book>() {
			public List<Book> call() throws RemoteException {
				return SerializerUtil.deserializeBookList(
					myInterface.books(SerializerUtil.serialize(query)), BookCollectionShadow.this
				);
			}
		});
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

	public List<Book> recentlyAddedBooks(final int count) {
		return listCall(new ListCallable<Book>() {
			public List<Book> call() throws RemoteException {
				return SerializerUtil.deserializeBookList(
					myInterface.recentlyAddedBooks(count), BookCollectionShadow.this
				);
			}
		});
	}

	public List<Book> recentlyOpenedBooks(final int count) {
		return listCall(new ListCallable<Book>() {
			public List<Book> call() throws RemoteException {
				return SerializerUtil.deserializeBookList(
					myInterface.recentlyOpenedBooks(count), BookCollectionShadow.this
				);
			}
		});
	}

	public synchronized Book getRecentBook(int index) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getRecentBook(index), this);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public synchronized Book getBookByFile(String path) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByFile(path), this);
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookById(long id) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookById(id), this);
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookByUid(UID uid) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByUid(uid.Type, uid.Id), this);
		} catch (RemoteException e) {
			return null;
		}
	}

	public synchronized Book getBookByHash(String hash) {
		if (myInterface == null) {
			return null;
		}
		try {
			return SerializerUtil.deserializeBook(myInterface.getBookByHash(hash), this);
		} catch (RemoteException e) {
			return null;
		}
	}

	public List<Author> authors() {
		return listCall(new ListCallable<Author>() {
			public List<Author> call() throws RemoteException {
				final List<String> strings = myInterface.authors();
				final List<Author> authors = new ArrayList<Author>(strings.size());
				for (String s : strings) {
					authors.add(Util.stringToAuthor(s));
				}
				return authors;
			}
		});
	}

	public List<Tag> tags() {
		return listCall(new ListCallable<Tag>() {
			public List<Tag> call() throws RemoteException {
				final List<String> strings = myInterface.tags();
				final List<Tag> tags = new ArrayList<Tag>(strings.size());
				for (String s : strings) {
					tags.add(Util.stringToTag(s));
				}
				return tags;
			}
		});
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

	public List<String> series() {
		return listCall(new ListCallable<String>() {
			public List<String> call() throws RemoteException {
				return myInterface.series();
			}
		});
	}

	public List<String> titles(final BookQuery query) {
		return listCall(new ListCallable<String>() {
			public List<String> call() throws RemoteException {
				return myInterface.titles(SerializerUtil.serialize(query));
			}
		});
	}

	public List<String> firstTitleLetters() {
		return listCall(new ListCallable<String>() {
			public List<String> call() throws RemoteException {
				return myInterface.firstTitleLetters();
			}
		});
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

	public synchronized boolean canRemoveBook(Book book, boolean deleteFromDisk) {
		if (myInterface == null) {
			return false;
		}
		try {
			return myInterface.canRemoveBook(SerializerUtil.serialize(book), deleteFromDisk);
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

	public synchronized void addToRecentlyOpened(Book book) {
		if (myInterface != null) {
			try {
				myInterface.addToRecentlyOpened(SerializerUtil.serialize(book));
			} catch (RemoteException e) {
			}
		}
	}

	public synchronized void removeFromRecentlyOpened(Book book) {
		if (myInterface != null) {
			try {
				myInterface.removeFromRecentlyOpened(SerializerUtil.serialize(book));
			} catch (RemoteException e) {
			}
		}
	}

	public List<String> labels() {
		return listCall(new ListCallable<String>() {
			public List<String> call() throws RemoteException {
				return myInterface.labels();
			}
		});
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

	public void setHash(Book book, String hash) {
		if (myInterface == null) {
			return;
		}
		try {
			myInterface.setHash(SerializerUtil.serialize(book), hash);
		} catch (RemoteException e) {
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
	public String getCoverUrl(Book book) {
		if (myInterface == null) {
			return null;
		}
		try {
			return myInterface.getCoverUrl(book.getPath());
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public String getDescription(Book book) {
		if (myInterface == null) {
			return null;
		}
		try {
			return myInterface.getDescription(SerializerUtil.serialize(book));
		} catch (RemoteException e) {
			return null;
		}
	}

	@Override
	public List<Bookmark> bookmarks(final BookmarkQuery query) {
		return listCall(new ListCallable<Bookmark>() {
			public List<Bookmark> call() throws RemoteException {
				return SerializerUtil.deserializeBookmarkList(
					myInterface.bookmarks(SerializerUtil.serialize(query))
				);
			}
		});
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

	public synchronized List<String> deletedBookmarkUids() {
		return listCall(new ListCallable<String>() {
			public List<String> call() throws RemoteException {
				return myInterface.deletedBookmarkUids();
			}
		});
	}

	public void purgeBookmarks(List<String> uids) {
		if (myInterface != null) {
			try {
				myInterface.purgeBookmarks(uids);
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

	public List<HighlightingStyle> highlightingStyles() {
		return listCall(new ListCallable<HighlightingStyle>() {
			public List<HighlightingStyle> call() throws RemoteException {
				return SerializerUtil.deserializeStyleList(myInterface.highlightingStyles());
			}
		});
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

	public int getDefaultHighlightingStyleId() {
		if (myInterface == null) {
			return 1;
		}
		try {
			return myInterface.getDefaultHighlightingStyleId();
		} catch (RemoteException e) {
			return 1;
		}
	}

	public void setDefaultHighlightingStyleId(int styleId) {
		if (myInterface != null) {
			try {
				myInterface.setDefaultHighlightingStyleId(styleId);
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

	public List<FormatDescriptor> formats() {
		return listCall(new ListCallable<FormatDescriptor>() {
			public List<FormatDescriptor> call() throws RemoteException {
				final List<String> serialized = myInterface.formats();
				final List<FormatDescriptor> formats =
					new ArrayList<FormatDescriptor>(serialized.size());
				for (String s : serialized) {
					formats.add(Util.stringToFormatDescriptor(s));
				}
				return formats;
			}
		});
	}

	public synchronized boolean setActiveFormats(List<String> formats) {
		if (myInterface != null) {
			try {
				return myInterface.setActiveFormats(formats);
			} catch (RemoteException e) {
			}
		}
		return false;
	}

	private interface ListCallable<T> {
		List<T> call() throws RemoteException;
	}

	private synchronized <T> List<T> listCall(ListCallable<T> callable) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return callable.call();
		} catch (Exception e) {
			return Collections.emptyList();
		} catch (Throwable e) {
			// TODO: report problem
			return Collections.emptyList();
		}
	}

	// method from ServiceConnection interface
	public void onServiceConnected(ComponentName name, IBinder service) {
		synchronized (this) {
			myInterface = LibraryInterface.Stub.asInterface(service);
		}

		final List<Runnable> actions;
		synchronized (myOnBindActions) {
			actions = new ArrayList<Runnable>(myOnBindActions);
			myOnBindActions.clear();
		}
		for (Runnable a : actions) {
			Config.Instance().runOnConnect(a);
		}

		if (myContext != null) {
			myContext.registerReceiver(myReceiver, new IntentFilter(FBReaderIntents.Event.LIBRARY_BOOK));
			myContext.registerReceiver(myReceiver, new IntentFilter(FBReaderIntents.Event.LIBRARY_BUILD));
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
	}

	public Book createBook(long id, String url, String title, String encoding, String language) {
		return new Book(id, url.substring("file://".length()), title, encoding, language);
	}
}
