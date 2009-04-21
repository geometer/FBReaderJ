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

import java.util.*;
import java.io.File;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;

import org.geometerplus.fbreader.formats.*;

public class BookCollection {
	private static BookCollection ourInstance;

	public static BookCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new BookCollection();
		}
		return ourInstance;
	}

	// TODO: this option is platform-dependent
	private final String BookDirectory = "/sdcard/Books";

	private final CollectionTree myCollectionByAuthor = new RootTree();
	private final CollectionTree myCollectionByTag = new RootTree();

	private	boolean myDoRebuild = true;

	private BookCollection() {
	}

	public void rebuild() {
		myDoRebuild = true;
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

	private void addDescription(LinkedList<BookDescription> list,
								ZLFile bookFile,
								Map<String,BookDescription> saved) {
		BookDescription description = saved.get(bookFile.getPath());
		boolean doReadMetaInfo = false;
		if (description == null) {
			doReadMetaInfo = true;
			description = new BookDescription(bookFile, false);
		}

		if (doReadMetaInfo) {
			final FormatPlugin plugin = PluginCollection.instance().getPlugin(bookFile);
			if ((plugin == null) || !plugin.readDescription(bookFile, description)) {
				return;
			}
			String title = description.getTitle();
			if ((title == null) || (title.length() == 0)) {
				description.setTitle(bookFile.getName(true));
			}
		}

		list.add(description);
	}

	private List<BookDescription> collectBookDescriptions() {
		final Queue<String> dirNameQueue = new LinkedList<String>();
		final HashSet<String> dirNameSet = new HashSet<String>();

		final Map<String,BookDescription> savedDescriptions = BooksDatabase.Instance().listBooks();
		final LinkedList<BookDescription> bookDescriptions = new LinkedList<BookDescription>();
		addDescription(bookDescriptions, getHelpFile(), savedDescriptions);

		dirNameQueue.offer(BookDirectory);
		while (!dirNameQueue.isEmpty()) {
			String name = dirNameQueue.poll();
			if (dirNameSet.contains(name)) {
				continue;
			}
			dirNameSet.add(name);
			File[] items = new File(name).listFiles();
			if (items == null) {
				continue;
			}
			for (File i : items) {
				if (i.getName().startsWith(".")) {
					continue;
				}
				final String fileName = i.getPath();
				if (i.isDirectory()) {
					dirNameQueue.add(fileName);
				} else {
					final ZLPhysicalFile file = new ZLPhysicalFile(i);
					if (PluginCollection.instance().getPlugin(file) != null) {
						addDescription(bookDescriptions, file, savedDescriptions);
					} else if (file.isArchive()) {
						if (!BookDescriptionUtil.checkInfo(file)) {
							BookDescriptionUtil.resetZipInfo(file);
							BookDescriptionUtil.saveInfo(file);
							// TODO: reset book information for all entries
						}
						for (String entryName : BookDescriptionUtil.listZipEntries(file)) {
							addDescription(bookDescriptions, ZLFile.createFile(entryName), savedDescriptions);
						}
					}
				}
			}
		}
		return bookDescriptions;
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
		System.err.println("before build: " + System.currentTimeMillis());
		final HashMap<Tag,TagTree> tagTreeMap = new HashMap<Tag,TagTree>();
		final HashMap<Author,AuthorTree> authorTreeMap = new HashMap<Author,AuthorTree>();
		final HashMap<AuthorSeriesPair,SeriesTree> seriesTreeMap = new HashMap<AuthorSeriesPair,SeriesTree>();

		final List<BookDescription> allDescriptions = collectBookDescriptions();
		System.err.println(allDescriptions.size() + " books");
		for (BookDescription description : allDescriptions) {
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
					authorTree.createBookSubTree(description);
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
				getTagTree(t, tagTreeMap).createBookSubTree(description);	
			}
		}

		BooksDatabase.Instance().executeAsATransaction(new Runnable() {
			public void run() {
				for (BookDescription description : allDescriptions) {
					description.save();
				}
			}
		});
		System.err.println("after build: " + System.currentTimeMillis());
	}

	public void synchronize() {
		if (myDoRebuild) {
			myCollectionByAuthor.clear();
			myCollectionByTag.clear();

			build();

			myCollectionByAuthor.sortAllChildren();
			myCollectionByTag.sortAllChildren();

			myDoRebuild = false;
		}
	}

	public final CollectionTree collectionByAuthor() {
		synchronize();
		return myCollectionByAuthor;
	}

	public final CollectionTree collectionByTag() {
		synchronize();
		return myCollectionByTag;
	}
}
