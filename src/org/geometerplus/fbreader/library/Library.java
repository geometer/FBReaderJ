/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import java.io.File;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public final class Library extends AbstractLibrary {
	public static final String ROOT_FOUND = "found";
	public static final String ROOT_FAVORITES = "favorites";
	public static final String ROOT_RECENT = "recent";
	public static final String ROOT_BY_AUTHOR = "byAuthor";
	public static final String ROOT_BY_TITLE = "byTitle";
	public static final String ROOT_BY_SERIES = "bySeries";
	public static final String ROOT_BY_TAG = "byTag";
	public static final String ROOT_FILE_TREE = "fileTree";

	private static Library ourInstance;
	public static Library Instance() {
		if (ourInstance == null) {
			ourInstance = new Library(BooksDatabase.Instance());
		}
		return ourInstance;
	}

	private final BooksDatabase myDatabase;

	private final Map<ZLFile,Book> myBooks =
		Collections.synchronizedMap(new HashMap<ZLFile,Book>());
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

	private void collectBooks(
		ZLFile file, FileInfoSet fileInfos,
		Map<Long,Book> savedBooksByFileId, Map<Long,Book> orphanedBooksByFileId,
		Set<Book> newBooks,
		boolean doReadMetaInfo
	) {
		final long fileId = fileInfos.getId(file);
		if (savedBooksByFileId.get(fileId) != null) {
			return;
		}

		try {
			final Book book = orphanedBooksByFileId.get(fileId);
			if (book != null) {
				if (doReadMetaInfo) {
					book.readMetaInfo();
				}
				addBookToLibrary(book);
				fireModelChangedEvent(ChangeListener.Code.BookAdded);
				newBooks.add(book);
				return;
			}
		} catch (BookReadingException e) {
			// ignore
		}

		try {
			final Book book = new Book(file);
			addBookToLibrary(book);
			fireModelChangedEvent(ChangeListener.Code.BookAdded);
			newBooks.add(book);
			return;
		} catch (BookReadingException e) {
			// ignore
		}

		if (file.isArchive()) {
			for (ZLFile entry : fileInfos.archiveEntries(file)) {
				collectBooks(
					entry, fileInfos,
					savedBooksByFileId, orphanedBooksByFileId,
					newBooks,
					doReadMetaInfo
				);
			}
		}
	}

	private List<ZLPhysicalFile> collectPhysicalFiles() {
		final Queue<ZLFile> dirQueue = new LinkedList<ZLFile>();
		final HashSet<ZLFile> dirSet = new HashSet<ZLFile>();
		final LinkedList<ZLPhysicalFile> fileList = new LinkedList<ZLPhysicalFile>();

		dirQueue.offer(new ZLPhysicalFile(new File(Paths.BooksDirectoryOption().getValue())));

		while (!dirQueue.isEmpty()) {
			for (ZLFile file : dirQueue.poll().children()) {
				if (file.isDirectory()) {
					if (!dirSet.contains(file)) {
						dirQueue.add(file);
						dirSet.add(file);
					}
				} else {
					file.setCached(true);
					fileList.add((ZLPhysicalFile)file);
				}
			}
		}
		return fileList;
	}

	private final List<?> myNullList = Collections.singletonList(null);

	private LibraryTree getTagTree(Tag tag) {
		if (tag == null || tag.Parent == null) {
			return getFirstLevelTree(ROOT_BY_TAG).getTagSubTree(tag);
		} else {
			return getTagTree(tag.Parent).getTagSubTree(tag);
		}
	}

	private synchronized void addBookToLibrary(Book book) {
		if (myBooks.containsKey(book.File)) {
			return;
		}
		myBooks.put(book.File, book);

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
				authorTree.getSeriesSubTree(seriesInfo.Name).getBookInSeriesSubTree(book);
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
			seriesRoot.getSeriesSubTree(seriesInfo.Name).getBookInSeriesSubTree(book);
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

		myBooks.remove(book.File);
		refreshInTree(ROOT_FAVORITES, book);
		refreshInTree(ROOT_RECENT, book);
		removeFromTree(ROOT_FOUND, book);
		removeFromTree(ROOT_BY_TITLE, book);
		removeFromTree(ROOT_BY_SERIES, book);
		removeFromTree(ROOT_BY_AUTHOR, book);
		removeFromTree(ROOT_BY_TAG, book);
		addBookToLibrary(book);
		fireModelChangedEvent(ChangeListener.Code.BookAdded);
	}

	private void build() {
		// Step 0: get database books marked as "existing"
		final FileInfoSet fileInfos = new FileInfoSet();
		final Map<Long,Book> savedBooksByFileId = myDatabase.loadBooks(fileInfos, true);
		final Map<Long,Book> savedBooksByBookId = new HashMap<Long,Book>();
		for (Book b : savedBooksByFileId.values()) {
			savedBooksByBookId.put(b.getId(), b);
		}

		// Step 1: set myDoGroupTitlesByFirstLetter value,
		// add "existing" books into recent and favorites lists
		if (savedBooksByFileId.size() > 10) {
			final HashSet<String> letterSet = new HashSet<String>();
			for (Book book : savedBooksByFileId.values()) {
				final String letter = TitleTree.firstTitleLetter(book);
				if (letter != null) {
					letterSet.add(letter);
				}
			}
			myDoGroupTitlesByFirstLetter = savedBooksByFileId.values().size() > letterSet.size() * 5 / 4;
		}

		for (long id : myDatabase.loadRecentBookIds()) {
			Book book = savedBooksByBookId.get(id);
			if (book == null) {
				book = Book.getById(id);
				if (book != null && !book.File.exists()) {
					book = null;
				}
			}
			if (book != null) {
				new BookTree(getFirstLevelTree(ROOT_RECENT), book, true);
			}
		}

		for (long id : myDatabase.loadFavoritesIds()) {
			Book book = savedBooksByBookId.get(id);
			if (book == null) {
				book = Book.getById(id);
				if (book != null && !book.File.exists()) {
					book = null;
				}
			}
			if (book != null) {
				getFirstLevelTree(ROOT_FAVORITES).getBookSubTree(book, true);
			}
		}

		fireModelChangedEvent(ChangeListener.Code.BookAdded);

		// Step 2: check if files corresponding to "existing" books really exists;
		//         add books to library if yes (and reload book info if needed);
		//         remove from recent/favorites list if no;
		//         collect newly "orphaned" books
		final Set<Book> orphanedBooks = new HashSet<Book>();
		final Set<ZLPhysicalFile> physicalFiles = new HashSet<ZLPhysicalFile>();
		int count = 0;
		for (Book book : savedBooksByFileId.values()) {
			synchronized (this) {
				final ZLPhysicalFile file = book.File.getPhysicalFile();
				if (file != null) {
					physicalFiles.add(file);
				}
				if (file != book.File && file != null && file.getPath().endsWith(".epub")) {
					myDatabase.deleteFromBookList(book.getId());
					continue;
				}
				if (book.File.exists()) {
					boolean doAdd = true;
					if (file == null) {
						continue;
					}
					if (!fileInfos.check(file, true)) {
						try {
							book.readMetaInfo();
							book.save();
						} catch (BookReadingException e) {
							doAdd = false;
						}
						file.setCached(false);
					}
					if (doAdd) {
						addBookToLibrary(book);
						if (++count % 16 == 0) {
							fireModelChangedEvent(ChangeListener.Code.BookAdded);
						}
					}
				} else {
					myRootTree.removeBook(book, true);
					fireModelChangedEvent(ChangeListener.Code.BookRemoved);
					orphanedBooks.add(book);
				}
			}
		}
		fireModelChangedEvent(ChangeListener.Code.BookAdded);
		myDatabase.setExistingFlag(orphanedBooks, false);

		// Step 3: collect books from physical files; add new, update already added,
		//         unmark orphaned as existing again, collect newly added
		final Map<Long,Book> orphanedBooksByFileId = myDatabase.loadBooks(fileInfos, false);
		final Set<Book> newBooks = new HashSet<Book>();

		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();
		for (ZLPhysicalFile file : physicalFilesList) {
			if (physicalFiles.contains(file)) {
				continue;
			}
			collectBooks(
				file, fileInfos,
				savedBooksByFileId, orphanedBooksByFileId,
				newBooks,
				!fileInfos.check(file, true)
			);
			file.setCached(false);
		}
		
		// Step 4: add help file
		try {
			final ZLFile helpFile = getHelpFile();
			Book helpBook = savedBooksByFileId.get(fileInfos.getId(helpFile));
			if (helpBook == null) {
				helpBook = new Book(helpFile);
			}
			addBookToLibrary(helpBook);
			fireModelChangedEvent(ChangeListener.Code.BookAdded);
		} catch (BookReadingException e) {
			// that's impossible
			e.printStackTrace();
		}

		// Step 5: save changes into database
		fileInfos.save();

		myDatabase.executeAsATransaction(new Runnable() {
			public void run() {
				for (Book book : newBooks) {
					book.save();
				}
			}
		});
		myDatabase.setExistingFlag(newBooks, true);
	}

	private volatile boolean myBuildStarted = false;

	public synchronized void startBuild() {
		if (myBuildStarted) {
			fireModelChangedEvent(ChangeListener.Code.StatusChanged);
			return;
		}
		myBuildStarted = true;

		setStatus(myStatusMask | STATUS_LOADING);
		final Thread builder = new Thread("Library.build") {
			public void run() {
				try {
					build();
				} finally {
					setStatus(myStatusMask & ~STATUS_LOADING);
				}
			}
		};
		builder.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		builder.start();
	}

	@Override
	public boolean isUpToDate() {
		return myStatusMask == 0;
	}

	@Override
	public Book getRecentBook() {
		List<Long> recentIds = myDatabase.loadRecentBookIds();
		return recentIds.size() > 0 ? Book.getById(recentIds.get(0)) : null;
	}

	@Override
	public Book getPreviousBook() {
		List<Long> recentIds = myDatabase.loadRecentBookIds();
		return recentIds.size() > 1 ? Book.getById(recentIds.get(1)) : null;
	}

	@Override
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
		final List<Book> booksCopy;
		synchronized (myBooks) {
			booksCopy = new ArrayList<Book>(myBooks.values());
		}
		for (Book book : booksCopy) {
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

	@Override
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

	@Override
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

	@Override
	public void addBookToFavorites(Book book) {
		if (isBookInFavorites(book)) {
			return;
		}
		final LibraryTree rootFavorites = getFirstLevelTree(ROOT_FAVORITES);
		rootFavorites.getBookSubTree(book, true);
		myDatabase.addToFavorites(book.getId());
	}

	@Override
	public void removeBookFromFavorites(Book book) {
		if (getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false)) {
			myDatabase.removeFromFavorites(book.getId());
			fireModelChangedEvent(ChangeListener.Code.BookRemoved);
		}
	}

	@Override
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

	@Override
	public void removeBook(Book book, int removeMode) {
		if (removeMode == REMOVE_DONT_REMOVE) {
			return;
		}
		myBooks.remove(book.File);
		if (getFirstLevelTree(ROOT_RECENT).removeBook(book, false)) {
			final List<Long> ids = myDatabase.loadRecentBookIds();
			ids.remove(book.getId());
			myDatabase.saveRecentBookIds(ids);
		}
		getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false);
		myRootTree.removeBook(book, true);

		myDatabase.deleteFromBookList(book.getId());
		if ((removeMode & REMOVE_FROM_DISK) != 0) {
			book.File.getPhysicalFile().delete();
		}
	}

	@Override
	public List<Bookmark> allBookmarks() {
		return BooksDatabase.Instance().loadAllVisibleBookmarks();
	}

	@Override
	public List<Bookmark> invisibleBookmarks(Book book) {
		final List<Bookmark> list = BooksDatabase.Instance().loadBookmarks(book.getId(), false);
		Collections.sort(list, new Bookmark.ByTimeComparator());
		return list;
	}
}
