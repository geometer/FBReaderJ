/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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
import java.io.PrintWriter;
import java.util.*;
import java.lang.ref.WeakReference;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.Paths;

public final class Library {
	public static final int STATE_NOT_INITIALIZED = 0;
	public static final int STATE_FULLY_INITIALIZED = 1;

	private final LinkedList<Book> myBooks = new LinkedList<Book>();
	private final HashSet<Book> myExternalBooks = new HashSet<Book>();
	private final LibraryTree myLibraryByAuthor = new RootTree();
	private final LibraryTree myLibraryByTitle = new RootTree();
	private final LibraryTree myLibraryByTag = new RootTree();
	private final LibraryTree myRecentBooks = new RootTree();
	private final LibraryTree myFavorites = new RootTree();
	private LibraryTree mySearchResult = new RootTree();

	private volatile int myState = STATE_NOT_INITIALIZED;
	private volatile boolean myInterrupted = false;

	public Library() {
	}

	public boolean hasState(int state) {
		return myState >= state || myInterrupted;
	}

	public void waitForState(int state) {
		while (myState < state && !myInterrupted) {
			synchronized(this) {
				if (myState < state && !myInterrupted) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public static ZLResourceFile getHelpFile() {
		final ZLResourceFile file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + Locale.getDefault().getLanguage() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}

		return ZLResourceFile.createResourceFile("data/help/MiniHelp.en.fb2");
	}

	private static Book getBook(ZLFile bookFile, FileInfoSet fileInfos, Map<Long,Book> saved, boolean doReadMetaInfo) {
		Book book = saved.remove(fileInfos.getId(bookFile));
		if (book == null) {
			doReadMetaInfo = true;
			book = new Book(bookFile);
		}

		if (doReadMetaInfo && !book.readMetaInfo()) {
			return null;
		}
		return book;
	}

	private void collectBooks(
		ZLFile file,
		FileInfoSet fileInfos,
		Map<Long,Book> savedBooks,
		boolean doReadMetaInfo
	) {
		Book book = getBook(file, fileInfos, savedBooks, doReadMetaInfo);
		if (book != null) {
			myBooks.add(book);
		} else if (file.isArchive()) {
			for (ZLFile entry : fileInfos.archiveEntries(file)) {
				collectBooks(entry, fileInfos, savedBooks, doReadMetaInfo);
			}
		}
	}

	private void collectExternalBooks(FileInfoSet fileInfos, Map<Long,Book> savedBooks) {
		final HashSet<ZLPhysicalFile> myUpdatedFiles = new HashSet<ZLPhysicalFile>();
		final HashSet<Long> files = new HashSet<Long>(savedBooks.keySet());
		for (Long fileId: files) {
			final ZLFile bookFile = fileInfos.getFile(fileId);
			if (bookFile == null) {
				continue;
			}
			final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
			if (physicalFile == null || !physicalFile.exists()) {
				continue;
			}
			boolean reloadMetaInfo = false; 
			if (myUpdatedFiles.contains(physicalFile)) {
				reloadMetaInfo = true;
			} else if (!fileInfos.check(physicalFile, physicalFile != bookFile)) {
				reloadMetaInfo = true;
				myUpdatedFiles.add(physicalFile);
			}
			final Book book = getBook(bookFile, fileInfos, savedBooks, reloadMetaInfo);
			if (book == null) {
				continue;
			}
			final long bookId = book.getId();
			if (bookId != -1 && BooksDatabase.Instance().checkBookList(bookId)) {
				myBooks.add(book);
				myExternalBooks.add(book);
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

	private void collectBooks() {
		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();

		FileInfoSet fileInfos = new FileInfoSet();

		final Map<Long,Book> savedBooks = BooksDatabase.Instance().loadBooks(fileInfos);

		for (ZLPhysicalFile file : physicalFilesList) {
			// TODO: better value for this flag
			final boolean flag = !"epub".equals(file.getExtension());
			collectBooks(file, fileInfos, savedBooks, !fileInfos.check(file, flag));
			file.setCached(false);
		}
		final Book helpBook = getBook(getHelpFile(), fileInfos, savedBooks, false);
		if (helpBook != null) {
			myBooks.add(helpBook);
		}

		collectExternalBooks(fileInfos, savedBooks);

		fileInfos.save();
	}

	private static class AuthorSeriesPair {
		private final Author myAuthor;
		private final String mySeries;

		AuthorSeriesPair(Author author, String series) {
			myAuthor = author;
			mySeries = series;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof AuthorSeriesPair)) {
				return false;
			}
			AuthorSeriesPair pair = (AuthorSeriesPair)object;
			return ZLMiscUtil.equals(myAuthor, pair.myAuthor) && mySeries.equals(pair.mySeries);
		}

		public int hashCode() {
			return Author.hashCode(myAuthor) + mySeries.hashCode();
		}
	}

	private final ArrayList myNullList = new ArrayList(1);
	{
		myNullList.add(null);
	}

	private TagTree getTagTree(Tag tag, HashMap<Tag,TagTree> tagTreeMap) {
		TagTree tagTree = tagTreeMap.get(tag);
		if (tagTree == null) {
			LibraryTree parent =
				((tag != null) && (tag.Parent != null)) ?
					getTagTree(tag.Parent, tagTreeMap) : myLibraryByTag;
			tagTree = parent.createTagSubTree(tag);
			tagTreeMap.put(tag, tagTree);
		}
		return tagTree;
	}

	private void build() {
		final HashMap<Tag,TagTree> tagTreeMap = new HashMap<Tag,TagTree>();
		final HashMap<Author,AuthorTree> authorTreeMap = new HashMap<Author,AuthorTree>();
		final HashMap<AuthorSeriesPair,SeriesTree> seriesTreeMap = new HashMap<AuthorSeriesPair,SeriesTree>();
		final HashMap<Long,Book> bookById = new HashMap<Long,Book>();

		collectBooks();

		for (Book book : myBooks) {
			bookById.put(book.getId(), book);
			List<Author> authors = book.authors();
			if (authors.isEmpty()) {
				authors = (List<Author>)myNullList;
			}
			final SeriesInfo seriesInfo = book.getSeriesInfo();
			for (Author a : authors) {
				AuthorTree authorTree = authorTreeMap.get(a);
				if (authorTree == null) {
					authorTree = myLibraryByAuthor.createAuthorSubTree(a);
					authorTreeMap.put(a, authorTree);
				}
				if (seriesInfo == null) {
					authorTree.createBookSubTree(book, false);
				} else {
					final String series = seriesInfo.Name;
					final AuthorSeriesPair pair = new AuthorSeriesPair(a, series);
					SeriesTree seriesTree = seriesTreeMap.get(pair);
					if (seriesTree == null) {
						seriesTree = authorTree.createSeriesSubTree(series);
						seriesTreeMap.put(pair, seriesTree);
					}
					seriesTree.createBookInSeriesSubTree(book);
				}
			}

			List<Tag> tags = book.tags();
			if (tags.isEmpty()) {
				tags = (List<Tag>)myNullList;
			}
			for (Tag t : tags) {
				getTagTree(t, tagTreeMap).createBookSubTree(book, true);
			}
		}

		boolean doGroupTitlesByFirstLetter = false;
		if (myBooks.size() > 10) {
			final HashSet<Character> letterSet = new HashSet<Character>();
			for (Book book : myBooks) {
				String title = book.getTitle();
				if (title != null) {
					title = title.trim();
					if (!"".equals(title)) {
						letterSet.add(title.charAt(0));
					}
				}
			}
			doGroupTitlesByFirstLetter = letterSet.size() > myBooks.size() + 4;
		}
		if (doGroupTitlesByFirstLetter) {
			final HashMap<Character,TitleTree> letterTrees = new HashMap<Character,TitleTree>();
			for (Book book : myBooks) {
				String title = book.getTitle();
				if (title == null) {
					continue;
				}
				title = title.trim();
				if ("".equals(title)) {
					continue;
				}
				Character c = title.charAt(0);
				TitleTree tree = letterTrees.get(c);
				if (tree == null) {
					tree = myLibraryByTitle.createTitleSubTree(c.toString());
					letterTrees.put(c, tree);
				}
				tree.createBookSubTree(book, true);
			}
		} else {
			for (Book book : myBooks) {
				myLibraryByTitle.createBookSubTree(book, true);
			}
		}

		final BooksDatabase db = BooksDatabase.Instance();
		for (long id : db.loadRecentBookIds()) {
			Book book = bookById.get(id);
			if (book != null) {
				myRecentBooks.createBookSubTree(book, true);
			}
		}

		for (long id : db.loadFavoritesIds()) {
			Book book = bookById.get(id);
			if (book != null) {
				myFavorites.createBookSubTree(book, true);
			}
		}

		myFavorites.sortAllChildren();
		myLibraryByAuthor.sortAllChildren();
		myLibraryByTitle.sortAllChildren();
		myLibraryByTag.sortAllChildren();

		db.executeAsATransaction(new Runnable() {
			public void run() {
				for (Book book : myBooks) {
					book.save();
				}
			}
		});

		myState = STATE_FULLY_INITIALIZED;
	}

	public synchronized void synchronize() {
		if (myState == STATE_NOT_INITIALIZED) {
			try {
				myInterrupted = false;
				build();
			} catch (Throwable t) {
				myInterrupted = true;
			}
			notifyAll();
		}
	}

	public LibraryTree byAuthor() {
		waitForState(STATE_FULLY_INITIALIZED);
		return myLibraryByAuthor;
	}

	public LibraryTree byTitle() {
		waitForState(STATE_FULLY_INITIALIZED);
		return myLibraryByTitle;
	}

	public LibraryTree byTag() {
		waitForState(STATE_FULLY_INITIALIZED);
		return myLibraryByTag;
	}

	public LibraryTree recentBooks() {
		waitForState(STATE_FULLY_INITIALIZED);
		return myRecentBooks;
	}

	public static Book getRecentBook() {
		List<Long> recentIds = BooksDatabase.Instance().loadRecentBookIds();
		return (recentIds.size() > 0) ? Book.getById(recentIds.get(0)) : null;
	}

	public LibraryTree favorites() {
		waitForState(STATE_FULLY_INITIALIZED);
		return myFavorites;
	}

	public LibraryTree searchResults() {
		return mySearchResult;
	}

	public LibraryTree searchBooks(String pattern) {
		waitForState(STATE_FULLY_INITIALIZED);
		final RootTree newSearchResults = new RootTree();
		if (pattern != null) {
			pattern = pattern.toLowerCase();
			for (Book book : myBooks) {
				if (book.matches(pattern)) {
					newSearchResults.createBookSubTree(book, true);
				}
			}
			newSearchResults.sortAllChildren();
			if (newSearchResults.hasChildren()) {
				mySearchResult = newSearchResults;
			}
		}
		return newSearchResults;
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
		waitForState(STATE_FULLY_INITIALIZED);
		return myFavorites.containsBook(book);
	}

	public void addBookToFavorites(Book book) {
		waitForState(STATE_FULLY_INITIALIZED);
		if (!myFavorites.containsBook(book)) {
			myFavorites.createBookSubTree(book, true);
			myFavorites.sortAllChildren();
			BooksDatabase.Instance().addToFavorites(book.getId());
		}
	}

	public void removeBookFromFavorites(Book book) {
		waitForState(STATE_FULLY_INITIALIZED);
		if (myFavorites.removeBook(book)) {
			BooksDatabase.Instance().removeFromFavorites(book.getId());
		}
	}

	public static final int REMOVE_DONT_REMOVE = 0x00;
	public static final int REMOVE_FROM_LIBRARY = 0x01;
	public static final int REMOVE_FROM_DISK = 0x02;
	public static final int REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK;

	public int getRemoveBookMode(Book book) {
		waitForState(STATE_FULLY_INITIALIZED);
		return (myExternalBooks.contains(book) ? REMOVE_FROM_LIBRARY : REMOVE_DONT_REMOVE)
			| (canDeleteBookFile(book) ? REMOVE_FROM_DISK : REMOVE_DONT_REMOVE);
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
		waitForState(STATE_FULLY_INITIALIZED);
		myBooks.remove(book);
		myLibraryByAuthor.removeBook(book);
		myLibraryByTag.removeBook(book);
		if (myRecentBooks.removeBook(book)) {
			final BooksDatabase db = BooksDatabase.Instance();
			final List<Long> ids = db.loadRecentBookIds();
			ids.remove(book.getId());
			db.saveRecentBookIds(ids);
		}
		mySearchResult.removeBook(book);
		myFavorites.removeBook(book);

		BooksDatabase.Instance().deleteFromBookList(book.getId());
		if ((removeMode & REMOVE_FROM_DISK) != 0) {
			book.File.getPhysicalFile().delete();
		}
	}

	private static final HashMap<String,WeakReference<ZLImage>> ourCoverMap =
		new HashMap<String,WeakReference<ZLImage>>();
	private static final WeakReference<ZLImage> NULL_IMAGE = new WeakReference<ZLImage>(null);

	public static ZLImage getCover(ZLFile file) {
		synchronized(ourCoverMap) {
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
