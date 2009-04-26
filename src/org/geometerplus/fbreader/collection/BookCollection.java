/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.collection;

import java.io.File;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;

import org.geometerplus.fbreader.formats.*;

public final class BookCollection {
	private static BookCollection ourInstance;

	public static BookCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new BookCollection();
		}
		return ourInstance;
	}

	// TODO: this option is platform-dependent
	private final ZLFile BookDirectory = new ZLPhysicalFile(new File("/sdcard/Books"));

	private final LinkedList<BookDescription> myBookDescriptions = new LinkedList<BookDescription>();
	private final CollectionTree myCollectionByAuthor = new RootTree();
	private final CollectionTree myCollectionByTag = new RootTree();
	private final CollectionTree myRecentBooks = new RootTree();
	private final CollectionTree mySearchResult = new RootTree();

	private	boolean myDoRebuild = true;

	private BookCollection() {
	}

	public void clear() {
		myDoRebuild = true;

		myBookDescriptions.clear();
		myCollectionByAuthor.clear();
		myCollectionByTag.clear();
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

	private static BookDescription getDescription(ZLFile bookFile, Map<String,BookDescription> saved, boolean doReadMetaInfo) {
		BookDescription description = saved.get(bookFile.getPath());
		if (description == null) {
			doReadMetaInfo = true;
			description = new BookDescription(bookFile, false);
		}

		if (doReadMetaInfo) {
			final FormatPlugin plugin = PluginCollection.instance().getPlugin(bookFile);
			if ((plugin == null) || !plugin.readDescription(bookFile, description)) {
				return null;
			}
			String title = description.getTitle();
			if ((title == null) || (title.length() == 0)) {
				description.setTitle(bookFile.getName(true));
			}
		}
		return description;
	}

	private void collectBookDescriptions(
		ZLFile file,
		FileInfoSet fileInfos,
		Map<String,BookDescription> savedDescriptions,
		boolean doReadMetaInfo
	) {
		BookDescription description = getDescription(file, savedDescriptions, doReadMetaInfo);
		if (description != null) {
			myBookDescriptions.add(description);
		} else if (file.isArchive()) {
			for (ZLFile entry : fileInfos.archiveEntries(file)) {
				collectBookDescriptions(entry, fileInfos, savedDescriptions, doReadMetaInfo);
			}
		}
	}

	private List<ZLPhysicalFile> collectPhysicalFiles() {
		final Queue<ZLFile> dirQueue = new LinkedList<ZLFile>();
		final HashSet<ZLFile> dirSet = new HashSet<ZLFile>();
		final LinkedList<ZLPhysicalFile> fileList = new LinkedList<ZLPhysicalFile>();

		dirQueue.offer(BookDirectory);
		while (!dirQueue.isEmpty()) {
			for (ZLFile file : dirQueue.poll().children()) {
				if (file.getName(true).startsWith(".")) {
					continue;
				}
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

	private void collectBookDescriptions() {
		//android.os.Debug.startMethodTracing("/sdcard/ll0");
		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();
		//android.os.Debug.stopMethodTracing();
		//System.err.println(physicalFilesList.size() + " files " + System.currentTimeMillis() % 20000);
		//android.os.Debug.startMethodTracing("/sdcard/ll1");
		final Map<String,BookDescription> savedDescriptions = BooksDatabase.Instance().listBooks();
		//android.os.Debug.stopMethodTracing();
		//System.err.println(savedDescriptions.size() + " saved books " + System.currentTimeMillis() % 20000);

		//android.os.Debug.startMethodTracing("/sdcard/ll2");
		FileInfoSet fileInfos = new FileInfoSet();
		fileInfos.loadAll();
		//android.os.Debug.stopMethodTracing();
		//System.err.println("file infos have been loaded " + System.currentTimeMillis() % 20000);

		//android.os.Debug.startMethodTracing("/sdcard/ll3");
		for (ZLPhysicalFile file : physicalFilesList) {
			collectBookDescriptions(file, fileInfos, savedDescriptions, !fileInfos.check(file));
			file.setCached(false);
		}
		myBookDescriptions.add(getDescription(getHelpFile(), savedDescriptions, false));
		//android.os.Debug.stopMethodTracing();
		//System.err.println("descriptions have been synchronized " + System.currentTimeMillis() % 20000);

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
			return myAuthor.equals(pair.myAuthor) && mySeries.equals(pair.mySeries);
		}

		public int hashCode() {
			return myAuthor.hashCode() + mySeries.hashCode();
		}
	}

	private static final ArrayList ourNullList = new ArrayList(1);
	static {
		ourNullList.add(null);
	}

	private TagTree getTagTree(Tag tag, HashMap<Tag,TagTree> tagTreeMap) {
		TagTree tagTree = tagTreeMap.get(tag);
		if (tagTree == null) {
			CollectionTree parent =
				((tag != null) && (tag.Parent != null)) ?
					getTagTree(tag.Parent, tagTreeMap) : myCollectionByTag;
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
		final HashMap<Long,BookDescription> bookById = new HashMap<Long,BookDescription>();

		collectBookDescriptions();
		//System.err.println(myBookDescriptions.size() + " books " + System.currentTimeMillis() % 20000);
		for (BookDescription description : myBookDescriptions) {
			bookById.put(description.getBookId(), description);
			List<Author> authors = description.authors();
			if (authors.isEmpty()) {
				authors = (List<Author>)ourNullList;
			}
			final SeriesInfo seriesInfo = description.getSeriesInfo();
			for (Author a : authors) {
				AuthorTree authorTree = authorTreeMap.get(a);
				if (authorTree == null) {
					authorTree = myCollectionByAuthor.createAuthorSubTree(a);
					authorTreeMap.put(a, authorTree);
				}
				if (seriesInfo == null) {
					authorTree.createBookSubTree(description, false);
				} else {
					final String series = seriesInfo.Name;
					final AuthorSeriesPair pair = new AuthorSeriesPair(a, series);
					SeriesTree seriesTree = seriesTreeMap.get(pair);
					if (seriesTree == null) {
						seriesTree = authorTree.createSeriesSubTree(series);
						seriesTreeMap.put(pair, seriesTree);
					}
					seriesTree.createBookInSeriesSubTree(description);
				}
			}

			List<Tag> tags = description.tags();
			if (tags.isEmpty()) {
				tags = (List<Tag>)ourNullList;
			}
			for (Tag t : tags) {
				getTagTree(t, tagTreeMap).createBookSubTree(description, true);
			}
		}

		final BooksDatabase db = BooksDatabase.Instance();
		for (long id : db.listRecentBookIds()) {
			BookDescription description = bookById.get(id);
			if (description != null) {
				myRecentBooks.createBookSubTree(description, true);
			}
		}

		db.executeAsATransaction(new Runnable() {
			public void run() {
				for (BookDescription description : myBookDescriptions) {
					description.save();
				}
			}
		});
		//System.err.println("after build: " + System.currentTimeMillis() % 20000);
	}

	public void synchronize() {
		if (myDoRebuild) {
			build();

			myCollectionByAuthor.sortAllChildren();
			myCollectionByTag.sortAllChildren();

			myDoRebuild = false;
		}
	}

	public CollectionTree collectionByAuthor() {
		synchronize();
		return myCollectionByAuthor;
	}

	public CollectionTree collectionByTag() {
		synchronize();
		return myCollectionByTag;
	}

	public CollectionTree recentBooks() {
		synchronize();
		return myRecentBooks;
	}

	public CollectionTree searchBooks(String pattern) {
		synchronize();
		mySearchResult.clear();
		if (pattern != null) {
			pattern = pattern.toLowerCase();
			for (BookDescription book : myBookDescriptions) {
				if (book.matches(pattern)) {
					mySearchResult.createBookSubTree(book, true);
				}
			}
			mySearchResult.sortAllChildren();
		}
		return mySearchResult;
	}

	public void addBookToRecentList(BookDescription description) {
		final BooksDatabase db = BooksDatabase.Instance();
		final List<Long> ids = db.listRecentBookIds();
		final Long bookId = description.getBookId();
		ids.remove(bookId);
		ids.add(0, bookId);
		if (ids.size() > 12) {
			ids.remove(12);
		} 
		db.saveRecentBookIds(ids);
	}
}
