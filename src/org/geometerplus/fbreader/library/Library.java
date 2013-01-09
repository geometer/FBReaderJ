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

import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.fbreader.tree.FBTree;

public final class Library {
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

	public static final String ROOT_FOUND = "found";
	public static final String ROOT_FAVORITES = "favorites";
	public static final String ROOT_RECENT = "recent";
	public static final String ROOT_BY_AUTHOR = "byAuthor";
	public static final String ROOT_BY_TITLE = "byTitle";
	public static final String ROOT_BY_SERIES = "bySeries";
	public static final String ROOT_BY_TAG = "byTag";
	public static final String ROOT_FILE_TREE = "fileTree";

	public static final int REMOVE_DONT_REMOVE = 0x00;
	public static final int REMOVE_FROM_LIBRARY = 0x01;
	public static final int REMOVE_FROM_DISK = 0x02;
	public static final int REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK;

	private static Library ourInstance;
	public static Library Instance() {
		if (ourInstance == null) {
			ourInstance = new Library(BooksDatabase.Instance());
		}
		return ourInstance;
	}

	private final BooksDatabase myDatabase;
	private final BookCollection myCollection;

	private final RootTree myRootTree = new RootTree();
	private boolean myDoGroupTitlesByFirstLetter;

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

	public Library(BooksDatabase db) {
		myDatabase = db;
		myCollection = new BookCollection(db);
		myCollection.addChangeListener(new BookCollection.ChangeListener() {
			public void onCollectionChanged(Code code, Book book) {
				switch (code) {
					case BookAdded:
						addBookToLibraryWithNoCheck(book);
						if (myCollection.size() % 16 == 0) {
							Library.this.fireModelChangedEvent(ChangeListener.Code.BookAdded);
						}
						break;
					case BuildStarted:
						Library.this.fireModelChangedEvent(ChangeListener.Code.StatusChanged);
						setStatus(myStatusMask | STATUS_LOADING);
						break;
					case BuildCompleted:
						Library.this.fireModelChangedEvent(ChangeListener.Code.BookAdded);
						setStatus(myStatusMask & ~STATUS_LOADING);
						break;
				}
			}
		});

		new FavoritesTree(myRootTree, ROOT_FAVORITES);
		new FirstLevelTree(myRootTree, ROOT_RECENT);
		new FirstLevelTree(myRootTree, ROOT_BY_AUTHOR);
		new FirstLevelTree(myRootTree, ROOT_BY_TITLE);
		new FirstLevelTree(myRootTree, ROOT_BY_TAG);
		new FileFirstLevelTree(myRootTree, ROOT_FILE_TREE);
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

	public static ZLResourceFile getHelpFile() {
		final Locale locale = Locale.getDefault();

		ZLResourceFile file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + locale.getLanguage() + "_" + locale.getCountry() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}

		file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + locale.getLanguage() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}

		return ZLResourceFile.createResourceFile("data/help/MiniHelp.en.fb2");
	}

	private final List<?> myNullList = Collections.singletonList(null);

	private LibraryTree getTagTree(Tag tag) {
		if (tag == null || tag.Parent == null) {
			return getFirstLevelTree(ROOT_BY_TAG).getTagSubTree(tag);
		} else {
			return getTagTree(tag.Parent).getTagSubTree(tag);
		}
	}

	private synchronized void addBookToLibraryWithNoCheck(Book book) {
		final String xml = BookSerializerUtil.serialize(book);
		book = BookSerializerUtil.deserialize(xml);

		List<Author> authors = book.authors();
		if (authors.isEmpty()) {
			authors = (List<Author>)myNullList;
		}
		final SeriesInfo seriesInfo = book.getSeriesInfo();
		for (Author a : authors) {
			final AuthorTree authorTree = getFirstLevelTree(ROOT_BY_AUTHOR).getAuthorSubTree(a);
			if (seriesInfo == null) {
				authorTree.getBookSubTree(book, false);
			} else {
				authorTree.getSeriesSubTree(seriesInfo.Title).getBookInSeriesSubTree(book);
			}
		}

		if (seriesInfo != null) {
			FirstLevelTree seriesRoot = getFirstLevelTree(ROOT_BY_SERIES);
			if (seriesRoot == null) {
				seriesRoot = new FirstLevelTree(
					myRootTree,
					myRootTree.indexOf(getFirstLevelTree(ROOT_BY_TITLE)) + 1,
					ROOT_BY_SERIES
				);
			}
			seriesRoot.getSeriesSubTree(seriesInfo.Title).getBookInSeriesSubTree(book);
		}

		if (myDoGroupTitlesByFirstLetter) {
			final String letter = TitleTree.firstTitleLetter(book);
			if (letter != null) {
				final TitleTree tree =
					getFirstLevelTree(ROOT_BY_TITLE).getTitleSubTree(letter);
				tree.getBookSubTree(book, true);
			}
		} else {
			getFirstLevelTree(ROOT_BY_TITLE).getBookSubTree(book, true);
		}

		List<Tag> tags = book.tags();
		if (tags.isEmpty()) {
			tags = (List<Tag>)myNullList;
		}
		for (Tag t : tags) {
			getTagTree(t).getBookSubTree(book, true);
		}

		final SearchResultsTree found =
			(SearchResultsTree)getFirstLevelTree(ROOT_FOUND);
		if (found != null && book.matches(found.getPattern())) {
			found.getBookSubTree(book, true);
		}
	}

	private void removeFromTree(String rootId, Book book) {
		final FirstLevelTree tree = getFirstLevelTree(rootId);
		if (tree != null) {
			tree.removeBook(book, false);
		}
	}

	private void refreshInTree(String rootId, Book book) {
		final FirstLevelTree tree = getFirstLevelTree(rootId);
		if (tree != null) {
			int index = tree.indexOf(new BookTree(book, true));
			if (index >= 0) {
				tree.removeBook(book, false);
				new BookTree(tree, book, true, index);
			}
		}
	}

	public synchronized void refreshBookInfo(Book book) {
		if (book == null) {
			return;
		}

		refreshInTree(ROOT_FAVORITES, book);
		refreshInTree(ROOT_RECENT, book);
		removeFromTree(ROOT_FOUND, book);
		removeFromTree(ROOT_BY_TITLE, book);
		removeFromTree(ROOT_BY_SERIES, book);
		removeFromTree(ROOT_BY_AUTHOR, book);
		removeFromTree(ROOT_BY_TAG, book);
		addBookToLibraryWithNoCheck(book);
		fireModelChangedEvent(ChangeListener.Code.BookAdded);
	}

	public synchronized void startBuild() {
		myCollection.startBuild();
	}

	public boolean isUpToDate() {
		return myStatusMask == 0;
	}

	public Book getRecentBook() {
		return myCollection.getRecentBook(0);
	}

	public Book getPreviousBook() {
		return myCollection.getRecentBook(1);
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

		final SearchResultsTree oldSearchResults = (SearchResultsTree)getFirstLevelTree(ROOT_FOUND);
		if (oldSearchResults != null && pattern.equals(oldSearchResults.getPattern())) {
			fireModelChangedEvent(ChangeListener.Code.Found);
			return;
		}
		
		FirstLevelTree newSearchResults = null;
		for (Book book : myCollection.books()) {
			if (book.matches(pattern)) {
				synchronized (this) {
					if (newSearchResults == null) {
						if (oldSearchResults != null) {
							oldSearchResults.removeSelf();
						}
						newSearchResults = new SearchResultsTree(myRootTree, ROOT_FOUND, pattern);
						fireModelChangedEvent(ChangeListener.Code.Found);
					}
					newSearchResults.getBookSubTree(book, true);
					fireModelChangedEvent(ChangeListener.Code.BookAdded);
				}
			}
		}
		if (newSearchResults == null) {
			fireModelChangedEvent(ChangeListener.Code.NotFound);
		}
	}

	public void addBookToRecentList(Book book) {
		final List<Long> ids = myDatabase.loadRecentBookIds();
		final Long bookId = book.getId();
		ids.remove(bookId);
		ids.add(0, bookId);
		if (ids.size() > 12) {
			ids.remove(12);
		}
		myDatabase.saveRecentBookIds(ids);
	}

	public boolean isBookInFavorites(Book book) {
		if (book == null) {
			return false;
		}
		final LibraryTree rootFavorites = getFirstLevelTree(ROOT_FAVORITES);
		for (FBTree tree : rootFavorites.subTrees()) {
			if (tree instanceof BookTree && book.equals(((BookTree)tree).Book)) {
				return true;
			}
		}
		return false;
	}

	public void addBookToFavorites(Book book) {
		if (isBookInFavorites(book)) {
			return;
		}
		final LibraryTree rootFavorites = getFirstLevelTree(ROOT_FAVORITES);
		rootFavorites.getBookSubTree(book, true);
		myDatabase.addToFavorites(book.getId());
	}

	public void removeBookFromFavorites(Book book) {
		if (getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false)) {
			myDatabase.removeFromFavorites(book.getId());
			fireModelChangedEvent(ChangeListener.Code.BookRemoved);
		}
	}

	public boolean canRemoveBookFile(Book book) {
		ZLFile file = book.File;
		if (file.getPhysicalFile() == null) {
			return false;
		}
		while (file instanceof ZLArchiveEntryFile) {
			file = file.getParent();
			if (file.children().size() != 1) {
				return false;
			}
		}
		return true;
	}

	public void removeBook(Book book, int removeMode) {
		if (removeMode == REMOVE_DONT_REMOVE) {
			return;
		}
		myCollection.removeBook(book, (removeMode & REMOVE_FROM_DISK) != 0);
		getFirstLevelTree(ROOT_RECENT).removeBook(book, false);
		getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false);
		myRootTree.removeBook(book, true);
	}

	public List<Bookmark> allBookmarks() {
		return BooksDatabase.Instance().loadAllVisibleBookmarks();
	}

	public List<Bookmark> invisibleBookmarks(Book book) {
		final List<Bookmark> list = BooksDatabase.Instance().loadBookmarks(book.getId(), false);
		Collections.sort(list, new Bookmark.ByTimeComparator());
		return list;
	}
}
