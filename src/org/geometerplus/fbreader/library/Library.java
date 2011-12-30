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
import java.lang.ref.WeakReference;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.Paths;

public final class Library {
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
			ourInstance = new Library();
		}
		return ourInstance;
	}

	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	private final List<Book> myBooks = Collections.synchronizedList(new LinkedList<Book>());
	private final RootTree myRootTree = new RootTree(this);
	private boolean myDoGroupTitlesByFirstLetter;

	private final List<ChangeListener> myListeners = Collections.synchronizedList(new LinkedList<ChangeListener>());

	private final static int STATUS_LOADING = 1;
	private final static int STATUS_SEARCHING = 2;
	private volatile int myStatusMask = 0;

	private synchronized void setStatus(int status) {
		myStatusMask = status;
		fireModelChangedEvent(ChangeListener.Code.StatusChanged);
	}

	private Library() {
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

	public void addChangeListener(ChangeListener listener) {
		myListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		myListeners.remove(listener);
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

		Book book = orphanedBooksByFileId.get(fileId);
		if (book != null && (!doReadMetaInfo || book.readMetaInfo())) {
			addBookToLibrary(book);
			fireModelChangedEvent(ChangeListener.Code.BookAdded);
			newBooks.add(book);
			return;
		}

		book = new Book(file);
		if (book.readMetaInfo()) {
			addBookToLibrary(book);
			fireModelChangedEvent(ChangeListener.Code.BookAdded);
			newBooks.add(book);
			return;
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
		myBooks.add(book);

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

	private void fireModelChangedEvent(ChangeListener.Code code) {
		synchronized (myListeners) {
			for (ChangeListener l : myListeners) {
				l.onLibraryChanged(code);
			}
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

		myBooks.remove(book);
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
		final BooksDatabase db = BooksDatabase.Instance();

		// Step 0: get database books marked as "existing"
		final FileInfoSet fileInfos = new FileInfoSet();
		final Map<Long,Book> savedBooksByFileId = db.loadBooks(fileInfos, true);
		final Map<Long,Book> savedBooksByBookId = new HashMap<Long,Book>();
		for (Book b : savedBooksByFileId.values()) {
			savedBooksByBookId.put(b.getId(), b);
		}

		// Step 1: add "existing" books recent and favorites lists
		for (long id : db.loadRecentBookIds()) {
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

		for (long id : db.loadFavoritesIds()) {
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

		final Set<Book> orphanedBooks = new HashSet<Book>();
		int count = 0;
		for (Book book : savedBooksByFileId.values()) {
			synchronized (this) {
				if (book.File.exists()) {
					boolean doAdd = true;
					final ZLPhysicalFile file = book.File.getPhysicalFile();
					if (file == null) {
						continue;
					}
					if (!fileInfos.check(file, true)) {
						if (book.readMetaInfo()) {
							book.save();
						} else {
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
		db.setExistingFlag(orphanedBooks, false);

		// Step 3: collect books from physical files; add new, update already added,
		//         unmark orphaned as existing again, collect newly added
		final Map<Long,Book> orphanedBooksByFileId = db.loadBooks(fileInfos, false);
		final Set<Book> newBooks = new HashSet<Book>();

		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();
		for (ZLPhysicalFile file : physicalFilesList) {
			collectBooks(
				file, fileInfos,
				savedBooksByFileId, orphanedBooksByFileId,
				newBooks,
				!fileInfos.check(file, true)
			);
			file.setCached(false);
		}
		
		// Step 4: add help file
		final ZLFile helpFile = getHelpFile();
		Book helpBook = savedBooksByFileId.get(fileInfos.getId(helpFile));
		if (helpBook == null) {
			helpBook = new Book(helpFile);
			helpBook.readMetaInfo();
		}
		addBookToLibrary(helpBook);
		fireModelChangedEvent(ChangeListener.Code.BookAdded);

		// Step 5: save changes into database
		fileInfos.save();

		db.executeAsATransaction(new Runnable() {
			public void run() {
				for (Book book : newBooks) {
					book.save();
				}
			}
		});
		db.setExistingFlag(newBooks, true);
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

	public static Book getRecentBook() {
		List<Long> recentIds = BooksDatabase.Instance().loadRecentBookIds();
		return recentIds.size() > 0 ? Book.getById(recentIds.get(0)) : null;
	}

	public static Book getPreviousBook() {
		List<Long> recentIds = BooksDatabase.Instance().loadRecentBookIds();
		return recentIds.size() > 1 ? Book.getById(recentIds.get(1)) : null;
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
		final List<Book> booksCopy;
		synchronized (myBooks) {
			booksCopy = new ArrayList<Book>(myBooks);
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

	public static void addBookToRecentList(Book book) {
		final BooksDatabase db = BooksDatabase.Instance();
		final List<Long> ids = db.loadRecentBookIds();
		final Long bookId = book.getId();
		ids.remove(bookId);
		ids.add(0, bookId);
		if (ids.size() > 12) {
			ids.remove(12);
		}
		db.saveRecentBookIds(ids);
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
		BooksDatabase.Instance().addToFavorites(book.getId());
	}

	public void removeBookFromFavorites(Book book) {
		if (getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false)) {
			BooksDatabase.Instance().removeFromFavorites(book.getId());
			fireModelChangedEvent(ChangeListener.Code.BookRemoved);
		}
	}

	public static final int REMOVE_DONT_REMOVE = 0x00;
	public static final int REMOVE_FROM_LIBRARY = 0x01;
	public static final int REMOVE_FROM_DISK = 0x02;
	public static final int REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK;

	public int getRemoveBookMode(Book book) {
		return canDeleteBookFile(book) ? REMOVE_FROM_DISK : REMOVE_DONT_REMOVE;
	}

	private boolean canDeleteBookFile(Book book) {
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
		myBooks.remove(book);
		if (getFirstLevelTree(ROOT_RECENT).removeBook(book, false)) {
			final BooksDatabase db = BooksDatabase.Instance();
			final List<Long> ids = db.loadRecentBookIds();
			ids.remove(book.getId());
			db.saveRecentBookIds(ids);
		}
		getFirstLevelTree(ROOT_FAVORITES).removeBook(book, false);
		myRootTree.removeBook(book, true);

		BooksDatabase.Instance().deleteFromBookList(book.getId());
		if ((removeMode & REMOVE_FROM_DISK) != 0) {
			book.File.getPhysicalFile().delete();
		}
	}

	private static final HashMap<String,WeakReference<ZLImage>> ourCoverMap =
		new HashMap<String,WeakReference<ZLImage>>();
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);

	public static ZLImage getCover(ZLFile file) {
		if (file == null) {
			return null;
		}
		synchronized (ourCoverMap) {
			final String path = file.getPath();
			final WeakReference<ZLImage> ref = ourCoverMap.get(path);
			if (ref == NULL_IMAGE) {
				return null;
			} else if (ref != null) {
				final ZLImage image = ref.get();
				if (image != null) {
					return image;
				}
			}
			ZLImage image = null;
			final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
			if (plugin != null) {
				image = plugin.readCover(file);
			}
			if (image == null) {
				ourCoverMap.put(path, NULL_IMAGE);
			} else {
				ourCoverMap.put(path, new WeakReference<ZLImage>(image));
			}
			return image;
		}
	}

	public static String getAnnotation(ZLFile file) {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
		return plugin != null ? plugin.readAnnotation(file) : null;
	}
}
