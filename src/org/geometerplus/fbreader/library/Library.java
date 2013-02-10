/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import java.util.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.tree.FBTree;

public final class Library {
	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	public static final int REMOVE_DONT_REMOVE = 0x00;
	public static final int REMOVE_FROM_LIBRARY = 0x01;
	public static final int REMOVE_FROM_DISK = 0x02;
	public static final int REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK;

	private final List<ChangeListener> myListeners = Collections.synchronizedList(new LinkedList<ChangeListener>());

	public interface ChangeListener {
		public enum Code {
			BookAdded,
			BookRemoved,
			StatusChanged,
			Found,
			NotFound
		}

		void onLibraryChanged(Code code);
	}

	public void addChangeListener(ChangeListener listener) {
		myListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		myListeners.remove(listener);
	}

	protected void fireModelChangedEvent(ChangeListener.Code code) {
		synchronized (myListeners) {
			for (ChangeListener l : myListeners) {
				l.onLibraryChanged(code);
			}
		}
	}

	public final IBookCollection Collection;

	private final RootTree myRootTree;

	private final static int STATUS_LOADING = 1;
	private final static int STATUS_SEARCHING = 2;
	private volatile int myStatusMask = 0;
	private final Object myStatusLock = new Object();

	private void setStatus(int status) {
		synchronized (myStatusLock) {
			if (myStatusMask != status) {
				myStatusMask = status;
				fireModelChangedEvent(ChangeListener.Code.StatusChanged);
			}
		}
	}

	public Library(IBookCollection collection) {
		Collection = collection;

		myRootTree = new RootTree(collection);

		new FavoritesTree(myRootTree);
		new RecentBooksTree(myRootTree);
		new AuthorListTree(myRootTree);
		new TitleListTree(myRootTree);
		new SeriesListTree(myRootTree);
		new TagListTree(myRootTree);
		new FileFirstLevelTree(myRootTree);
	}

	public LibraryTree getRootTree() {
		return myRootTree;
	}

	private FirstLevelTree getFirstLevelTree(String key) {
		return (FirstLevelTree)myRootTree.getSubTree(key);
	}

	public LibraryTree getLibraryTree(LibraryTree.Key key) {
		if (key == null) {
			return null;
		}
		if (key.Parent == null) {
			return key.Id.equals(myRootTree.getUniqueKey().Id) ? myRootTree : null;
		}
		final LibraryTree parentTree = getLibraryTree(key.Parent);
		return parentTree != null ? (LibraryTree)parentTree.getSubTree(key.Id) : null;
	}

	private void removeFromTree(String rootId, Book book) {
		final FirstLevelTree tree = getFirstLevelTree(rootId);
		if (tree != null) {
			tree.removeBook(book);
		}
	}

	public synchronized void refreshBookInfo(Book book) {
		if (book == null) {
			return;
		}

		Collection.saveBook(book, true);
		fireModelChangedEvent(ChangeListener.Code.BookAdded);
	}

	public boolean isUpToDate() {
		return myStatusMask == 0;
	}

	public void startBookSearch(final String pattern) {
		setStatus(myStatusMask | STATUS_SEARCHING);
		final Thread searcher = new Thread("Library.searchBooks") {
			public void run() {
				try {
					searchBooks(pattern);
				} finally {
					setStatus(myStatusMask & ~STATUS_SEARCHING);
				}
			}
		};
		searcher.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		searcher.start();
	}

	private void searchBooks(String pattern) {
		if (pattern == null) {
			fireModelChangedEvent(ChangeListener.Code.NotFound);
			return;
		}

		pattern = pattern.toLowerCase();

		final SearchResultsTree oldSearchResults = (SearchResultsTree)getFirstLevelTree(LibraryTree.ROOT_FOUND);
		if (oldSearchResults != null && pattern.equals(oldSearchResults.getPattern())) {
			fireModelChangedEvent(ChangeListener.Code.Found);
			return;
		}

		FirstLevelTree newSearchResults = null;
		synchronized (this) {
			for (Book book : Collection.booksForPattern(pattern)) {
				if (newSearchResults == null) {
					if (oldSearchResults != null) {
						oldSearchResults.removeSelf();
					}
					newSearchResults = new SearchResultsTree(myRootTree, LibraryTree.ROOT_FOUND, pattern);
					fireModelChangedEvent(ChangeListener.Code.Found);
				}
				newSearchResults.createBookWithAuthorsSubTree(book);
				fireModelChangedEvent(ChangeListener.Code.BookAdded);
			}
		}
		if (newSearchResults == null) {
			fireModelChangedEvent(ChangeListener.Code.NotFound);
		}
	}
}
