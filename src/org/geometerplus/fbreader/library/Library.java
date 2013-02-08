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

import java.io.File;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.tree.FBTree;

public final class Library {
	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

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
	private final BooksDatabase myDatabase;

	private final Map<ZLFile,Book> myBooks =
		Collections.synchronizedMap(new HashMap<ZLFile,Book>());
	private final RootTree myRootTree;
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
		Collection = new BookCollection(db);

		myRootTree = new RootTree(Collection);

		new FavoritesTree(myRootTree);
		new RecentBooksTree(myRootTree);
		new FirstLevelTree(myRootTree, LibraryTree.ROOT_BY_AUTHOR);
		new FirstLevelTree(myRootTree, LibraryTree.ROOT_BY_TITLE);
		new FirstLevelTree(myRootTree, LibraryTree.ROOT_BY_TAG);
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

		for (String s : Paths.BookPathOption().getValue()) {
			dirQueue.offer(new ZLPhysicalFile(new File(s)));
		}

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
			return getFirstLevelTree(LibraryTree.ROOT_BY_TAG).getTagSubTree(tag);
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
			final AuthorTree authorTree = getFirstLevelTree(LibraryTree.ROOT_BY_AUTHOR).getAuthorSubTree(a);
			if (seriesInfo == null) {
				authorTree.getBookSubTree(book);
			} else {
				authorTree.getSeriesSubTree(seriesInfo.Title).getBookInSeriesSubTree(book);
			}
		}

		if (seriesInfo != null) {
			FirstLevelTree seriesRoot = getFirstLevelTree(LibraryTree.ROOT_BY_SERIES);
			if (seriesRoot == null) {
				seriesRoot = new FirstLevelTree(
					myRootTree,
					myRootTree.indexOf(getFirstLevelTree(LibraryTree.ROOT_BY_TITLE)) + 1,
					LibraryTree.ROOT_BY_SERIES
				);
			}
			seriesRoot.getSeriesSubTree(seriesInfo.Title).getBookInSeriesSubTree(book);
		}

		if (myDoGroupTitlesByFirstLetter) {
			final String letter = TitleTree.firstTitleLetter(book);
			if (letter != null) {
				final TitleTree tree =
					getFirstLevelTree(LibraryTree.ROOT_BY_TITLE).getTitleSubTree(letter);
				tree.getBookWithAuthorsSubTree(book);
			}
		} else {
			getFirstLevelTree(LibraryTree.ROOT_BY_TITLE).getBookWithAuthorsSubTree(book);
		}

		List<Tag> tags = book.tags();
		if (tags.isEmpty()) {
			tags = (List<Tag>)myNullList;
		}
		for (Tag t : tags) {
			getTagTree(t).getBookWithAuthorsSubTree(book);
		}

		final SearchResultsTree found =
			(SearchResultsTree)getFirstLevelTree(LibraryTree.ROOT_FOUND);
		if (found != null && book.matches(found.getPattern())) {
			found.getBookWithAuthorsSubTree(book);
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
			int index = tree.indexOf(new BookWithAuthorsTree(Collection, book));
			if (index >= 0) {
				tree.removeBook(book, false);
				new BookWithAuthorsTree(tree, book, index);
			}
		}
	}

	public synchronized void refreshBookInfo(Book book) {
		if (book == null) {
			return;
		}

		myBooks.remove(book.File);
		refreshInTree(LibraryTree.ROOT_FAVORITES, book);
		removeFromTree(LibraryTree.ROOT_FOUND, book);
		removeFromTree(LibraryTree.ROOT_BY_TITLE, book);
		removeFromTree(LibraryTree.ROOT_BY_SERIES, book);
		removeFromTree(LibraryTree.ROOT_BY_AUTHOR, book);
		removeFromTree(LibraryTree.ROOT_BY_TAG, book);
		addBookToLibrary(book);
		fireModelChangedEvent(ChangeListener.Code.BookAdded);
	}

	private void build() {
		// Step 0: get database books marked as "existing"
		final FileInfoSet fileInfos = new FileInfoSet(myDatabase);
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

		for (Book book : Collection.favorites()) {
			getFirstLevelTree(LibraryTree.ROOT_FAVORITES).getBookWithAuthorsSubTree(book);
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

	public boolean isUpToDate() {
		return myStatusMask == 0;
	}

	public Book getRecentBook() {
		List<Long> recentIds = myDatabase.loadRecentBookIds();
		for (Long id : recentIds) {
			try {
				if (PluginCollection.Instance().getPlugin(Book.getById(id).File).type() != FormatPlugin.Type.EXTERNAL) {
					return Book.getById(id);
				}
			} catch (NullPointerException e) {
			}
		}
		return null;
	}

	public Book getPreviousBook() {
		List<Long> recentIds = myDatabase.loadRecentBookIds();
		boolean firstSkipped = false;
		for (Long id : recentIds) {
			if (firstSkipped) {
				try {
					if (PluginCollection.Instance().getPlugin(Book.getById(id).File).type() != FormatPlugin.Type.EXTERNAL) {
						return Book.getById(id);
					}
				} catch (NullPointerException e) {
				}
			}
			firstSkipped = true;
		}
		return null;
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
						newSearchResults = new SearchResultsTree(myRootTree, LibraryTree.ROOT_FOUND, pattern);
						fireModelChangedEvent(ChangeListener.Code.Found);
					}
					newSearchResults.getBookWithAuthorsSubTree(book);
					fireModelChangedEvent(ChangeListener.Code.BookAdded);
				}
			}
		}
		if (newSearchResults == null) {
			fireModelChangedEvent(ChangeListener.Code.NotFound);
		}
	}

	public void addBookToRecentList(Book book) {
		Collection.addBookToRecentList(book);
	}

	public boolean isBookInFavorites(Book book) {
		if (book == null) {
			return false;
		}
		final LibraryTree rootFavorites = getFirstLevelTree(LibraryTree.ROOT_FAVORITES);
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
		final LibraryTree rootFavorites = getFirstLevelTree(LibraryTree.ROOT_FAVORITES);
		rootFavorites.getBookWithAuthorsSubTree(book);
		Collection.setBookFavorite(book, true);
	}

	public void removeBookFromFavorites(Book book) {
		if (getFirstLevelTree(LibraryTree.ROOT_FAVORITES).removeBook(book, false)) {
			Collection.setBookFavorite(book, false);
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

	public void removeBookFromRecentList(Book book) {
		getFirstLevelTree(ROOT_RECENT).removeBook(book, false);
		final List<Long> ids = myDatabase.loadRecentBookIds();
		ids.remove(book.getId());
		myDatabase.saveRecentBookIds(ids);
	}
	
	public void removeBook(Book book, int removeMode) {
		if (removeMode == REMOVE_DONT_REMOVE) {
			return;
		}
		myBooks.remove(book.File);
		if (getFirstLevelTree(LibraryTree.ROOT_RECENT).removeBook(book, false)) {
			final List<Long> ids = myDatabase.loadRecentBookIds();
			ids.remove(book.getId());
			myDatabase.saveRecentBookIds(ids);
		}
		getFirstLevelTree(LibraryTree.ROOT_FAVORITES).removeBook(book, false);
		myRootTree.removeBook(book, true);

		if ((removeMode & REMOVE_FROM_DISK) != 0) {
			book.File.getPhysicalFile().delete();
		}
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
