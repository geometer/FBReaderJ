/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

import org.geometerplus.fbreader.Paths;

public final class Library {
	private final LinkedList<Book> myBooks = new LinkedList<Book>();
	private final HashSet<Book> myExternalBooks = new HashSet<Book>();
	private final LibraryTree myLibraryByAuthor = new RootTree();
	private final LibraryTree myLibraryByTag = new RootTree();
	private final LibraryTree myRecentBooks = new RootTree();
	private final LibraryTree mySearchResult = new RootTree();

	private boolean myDoRebuild = true;

	public Library() {
	}

	public void clear() {
		myDoRebuild = true;

		myBooks.clear();
		myExternalBooks.clear();
		myLibraryByAuthor.clear();
		myLibraryByTag.clear();
		myRecentBooks.clear();
		mySearchResult.clear();
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
			} else if (!fileInfos.check(physicalFile)) {
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
			collectBooks(file, fileInfos, savedBooks, !fileInfos.check(file));
			file.setCached(false);
		}
		myBooks.add(getBook(getHelpFile(), fileInfos, savedBooks, false));

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
		//System.err.println("before build: " + System.currentTimeMillis() % 20000);
		final HashMap<Tag,TagTree> tagTreeMap = new HashMap<Tag,TagTree>();
		final HashMap<Author,AuthorTree> authorTreeMap = new HashMap<Author,AuthorTree>();
		final HashMap<AuthorSeriesPair,SeriesTree> seriesTreeMap = new HashMap<AuthorSeriesPair,SeriesTree>();
		final HashMap<Long,Book> bookById = new HashMap<Long,Book>();

		collectBooks();
		//System.err.println(myBooks.size() + " books " + System.currentTimeMillis() % 20000);
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

		final BooksDatabase db = BooksDatabase.Instance();
		for (long id : db.loadRecentBookIds()) {
			Book book = bookById.get(id);
			if (book != null) {
				myRecentBooks.createBookSubTree(book, true);
			}
		}

		db.executeAsATransaction(new Runnable() {
			public void run() {
				for (Book book : myBooks) {
					book.save();
				}
			}
		});
		//System.err.println("after build: " + System.currentTimeMillis() % 20000);
	}

	public void synchronize() {
		if (myDoRebuild) {
			build();

			myLibraryByAuthor.sortAllChildren();
			myLibraryByTag.sortAllChildren();

			myDoRebuild = false;
		}
	}

	public LibraryTree byAuthor() {
		synchronize();
		return myLibraryByAuthor;
	}

	public LibraryTree byTag() {
		synchronize();
		return myLibraryByTag;
	}

	public LibraryTree recentBooks() {
		synchronize();
		return myRecentBooks;
	}

	public static Book getRecentBook() {
		List<Long> recentIds = BooksDatabase.Instance().loadRecentBookIds();
		return (recentIds.size() > 0) ? Book.getById(recentIds.get(0)) : null;
	}

	public LibraryTree searchBooks(String pattern) {
		synchronize();
		mySearchResult.clear();
		if (pattern != null) {
			pattern = pattern.toLowerCase();
			for (Book book : myBooks) {
				if (book.matches(pattern)) {
					mySearchResult.createBookSubTree(book, true);
				}
			}
			mySearchResult.sortAllChildren();
		}
		return mySearchResult;
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

	public static final int REMOVE_DONT_REMOVE = 0x00;
	public static final int REMOVE_FROM_LIBRARY = 0x01;
	public static final int REMOVE_FROM_DISK = 0x02;
	public static final int REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK;

	public int getRemoveBookMode(Book book) {
		synchronize();
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
		synchronize();
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

		BooksDatabase.Instance().deleteFromBookList(book.getId());
		if ((removeMode & REMOVE_FROM_DISK) != 0) {
			book.File.getPhysicalFile().delete();
		}
	}
}
