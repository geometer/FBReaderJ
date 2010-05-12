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
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.fbreader.Paths;

public final class Library {
	private static Library ourInstance;

	public static Library Instance() {
		if (ourInstance == null) {
			ourInstance = new Library();
		}
		return ourInstance;
	}

	private final LinkedList<Book> myBooks = new LinkedList<Book>();
	private final LibraryTree myLibraryByAuthor = new RootTree();
	private final LibraryTree myLibraryByTag = new RootTree();
	private final LibraryTree myRecentBooks = new RootTree();
	private final LibraryTree mySearchResult = new RootTree();

	private	boolean myDoRebuild = true;

	private Library() {
	}

	public void clear() {
		myDoRebuild = true;

		myBooks.clear();
		myLibraryByAuthor.clear();
		myLibraryByTag.clear();
		myRecentBooks.clear();
		mySearchResult.clear();
	}

	public ZLResourceFile getHelpFile() {
		final ZLResourceFile file = ZLResourceFile.createResourceFile(
			"data/help/MiniHelp." + Locale.getDefault().getLanguage() + ".fb2"
		);
		if (file.exists()) {
			return file;
		}

		return ZLResourceFile.createResourceFile("data/help/MiniHelp.en.fb2");
	}

	private static Book getBook(ZLFile bookFile, FileInfoSet fileInfos, Map<Long,Book> saved, boolean doReadMetaInfo) {
		Book book = saved.get(fileInfos.getId(bookFile));
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

	private List<ZLPhysicalFile> collectPhysicalFiles() {
		final Queue<ZLFile> dirQueue = new LinkedList<ZLFile>();
		final HashSet<ZLFile> dirSet = new HashSet<ZLFile>();
		final LinkedList<ZLPhysicalFile> fileList = new LinkedList<ZLPhysicalFile>();

		dirQueue.offer(new ZLPhysicalFile(new File(Paths.BooksDirectoryOption.getValue())));

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
		final long start = System.currentTimeMillis();
		//android.os.Debug.startMethodTracing("/sdcard/ll0");
		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();
		//android.os.Debug.stopMethodTracing();
		//System.err.println(physicalFilesList.size() + " files " + (System.currentTimeMillis() - start));

		//android.os.Debug.startMethodTracing("/sdcard/ll2");
		FileInfoSet fileInfos = new FileInfoSet();
		//android.os.Debug.stopMethodTracing();
		//System.err.println("file infos have been loaded " + (System.currentTimeMillis() - start));

		//android.os.Debug.startMethodTracing("/sdcard/ll1");
		final Map<Long,Book> savedBooks = BooksDatabase.Instance().listBooks(fileInfos);
		//android.os.Debug.stopMethodTracing();
		//System.err.println(savedBooks.size() + " saved books " + (System.currentTimeMillis() - start));

		//android.os.Debug.startMethodTracing("/sdcard/ll3");
		for (ZLPhysicalFile file : physicalFilesList) {
			collectBooks(file, fileInfos, savedBooks, !fileInfos.check(file));
			file.setCached(false);
		}
		myBooks.add(getBook(getHelpFile(), fileInfos, savedBooks, false));
		//android.os.Debug.stopMethodTracing();
		//System.err.println("books have been synchronized " + (System.currentTimeMillis() - start));

		//android.os.Debug.startMethodTracing("/sdcard/ll4");
		fileInfos.save();
		//android.os.Debug.stopMethodTracing();
	}

	private static class AuthorSeriesPair {
		private final Author myAuthor;
		private final String mySeries;

		AuthorSeriesPair(Author author, String series) {
			myAuthor = author;
			mySeries = series;
		}

		public boolean equals(Object object) {
			if (!(object instanceof AuthorSeriesPair)) {
				return false;
			}
			AuthorSeriesPair pair = (AuthorSeriesPair)object;
			return Author.areEquals(myAuthor, pair.myAuthor) && mySeries.equals(pair.mySeries);
		}

		public int hashCode() {
			return Author.hashCode(myAuthor) + mySeries.hashCode();
		}
	}

	private static final ArrayList ourNullList = new ArrayList(1);
	static {
		ourNullList.add(null);
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
				authors = (List<Author>)ourNullList;
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
				tags = (List<Tag>)ourNullList;
			}
			for (Tag t : tags) {
				getTagTree(t, tagTreeMap).createBookSubTree(book, true);
			}
		}

		final BooksDatabase db = BooksDatabase.Instance();
		for (long id : db.listRecentBookIds()) {
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

	public Book getRecentBook() {
		List<Long> recentIds = BooksDatabase.Instance().listRecentBookIds();
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

	public void addBookToRecentList(Book book) {
		final BooksDatabase db = BooksDatabase.Instance();
		final List<Long> ids = db.listRecentBookIds();
		final Long bookId = book.getId();
		ids.remove(bookId);
		ids.add(0, bookId);
		if (ids.size() > 12) {
			ids.remove(12);
		} 
		db.saveRecentBookIds(ids);
	}

	public boolean canDeleteBook(Book book) {
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

	public void deleteBook(Book book) {
		synchronize();
		myBooks.remove(book);
		myLibraryByAuthor.removeBook(book);
		myLibraryByTag.removeBook(book);
		if (myRecentBooks.removeBook(book)) {
			final BooksDatabase db = BooksDatabase.Instance();
			final List<Long> ids = db.listRecentBookIds();
			ids.remove(book.getId());
			db.saveRecentBookIds(ids);
		}
		mySearchResult.removeBook(book);
		book.File.getPhysicalFile().delete();
	}
}
