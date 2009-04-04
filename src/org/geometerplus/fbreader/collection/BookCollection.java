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
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.fbreader.description.*;
import org.geometerplus.fbreader.formats.PluginCollection;

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

	private HashSet collectBookFileNames() {
		final HashSet dirs = collectDirNames();
		final HashSet bookFileNames = new HashSet();

		for (Iterator it = dirs.iterator(); it.hasNext(); ) {
			final ZLDir dir = new ZLFile((String)it.next()).getDirectory();
			if (dir == null) {
				continue;
			}
			final ArrayList files = dir.collectFiles();
			if (files.isEmpty()) {
				continue;
			}
			final int len = files.size();
			for (int i = 0; i < len; ++i) {
				final String fileName = dir.getItemPath((String)files.get(i));
				final ZLFile file = new ZLFile(fileName);
				if (PluginCollection.instance().getPlugin(file, true) != null) {
					bookFileNames.add(fileName);
				} else if (file.getExtension().equals("zip")) {
					if (!BookDescriptionUtil.checkInfo(file)) {
						BookDescriptionUtil.resetZipInfo(file);
						BookDescriptionUtil.saveInfo(file);
					}
					final ArrayList zipEntries = new ArrayList();
					BookDescriptionUtil.listZipEntries(file, zipEntries);
					final int zipEntriesLen = zipEntries.size();
					for (int j = 0; j < zipEntriesLen; ++j) {
						final String entryName = (String)zipEntries.get(j);
						if (!bookFileNames.contains(entryName)) {
							bookFileNames.add(entryName);
						}
					}
				}
			}
		}

		return bookFileNames;
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

	private void build() {
		final HashSet fileNamesSet = collectBookFileNames();
		final HashMap<String,TagTree> tagTreeMap = new HashMap<String,TagTree>();
		final HashMap<Author,AuthorTree> authorTreeMap = new HashMap<Author,AuthorTree>();
		final HashMap<AuthorSeriesPair,SeriesTree> seriesTreeMap = new HashMap<AuthorSeriesPair,SeriesTree>();

		for (Iterator it = fileNamesSet.iterator(); it.hasNext(); ) {
			final BookDescription description = BookDescription.getDescription((String)it.next());

			final Author author = description.getAuthor();
			final String series = description.getSeriesName();
			AuthorTree authorTree = authorTreeMap.get(author);
			if (authorTree == null) {
				authorTree = myCollectionByAuthor.createAuthorSubTree(author);
				authorTreeMap.put(author, authorTree);
			}
			if (series.length() == 0) {
				authorTree.createBookSubTree(description);
			} else {
				final AuthorSeriesPair pair = new AuthorSeriesPair(author, series);
				SeriesTree seriesTree = seriesTreeMap.get(pair);
				if (seriesTree == null) {
					seriesTree = authorTree.createSeriesSubTree(series);
					seriesTreeMap.put(pair, seriesTree);
				}
				seriesTree.createBookInSeriesSubTree(description);
			}

			final List<String> tags = description.getTags();
			if (!tags.isEmpty()) {
				for (String tag : description.getTags()) {
					TagTree tagTree = tagTreeMap.get(tag);
					if (tagTree == null) {
						tagTree = myCollectionByTag.createTagSubTree(tag);
						tagTreeMap.put(tag, tagTree);
					}
					tagTree.createBookSubTree(description);	
				}
			} else {
				TagTree tagTree = tagTreeMap.get(null);
				if (tagTree == null) {
					tagTree = myCollectionByTag.createTagSubTree(null);
					tagTreeMap.put(null, tagTree);
				}
				tagTree.createBookSubTree(description);	
			}
		}
	}

	public void synchronize() {
		if (myDoRebuild) {
			myCollectionByAuthor.clear();
			myCollectionByTag.clear();

			ZLConfig.Instance().executeAsATransaction(new Runnable() {
				public void run() {
					build();
				}
			});

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

	private HashSet collectDirNames() {
		ArrayList nameQueue = new ArrayList();
		HashSet nameSet = new HashSet();

		nameQueue.add(BookDirectory);

		while (!nameQueue.isEmpty()) {
			String name = (String)nameQueue.get(0);
			nameQueue.remove(0);
			if (!nameSet.contains(name)) {
				ZLDir dir = new ZLFile(name).getDirectory();
				if (dir != null) {
					ArrayList subdirs = dir.collectSubDirs();
					for (int i = 0; i < subdirs.size(); ++i) {
						nameQueue.add(dir.getItemPath((String)subdirs.get(i)));
					}
				}
				nameSet.add(name);
			}
		}
		return nameSet;
	}

	/*
	private static Author _author(ArrayList books, int index) {
		return ((BookDescription)books.get(index)).getAuthor();
	}

	public void collectSeriesNames(Author author, HashSet set) {
		synchronize();
		final ArrayList books = myBooks;
		if (books.isEmpty()) {
			return;
		}
		int leftIndex = 0;
		if (author.compareTo(_author(books, leftIndex)) < 0) {
			return;
		}
		int rightIndex = books.size() - 1;
		if (author.compareTo(_author(books, rightIndex)) > 0) {
			return;
		}
		while (rightIndex > leftIndex) {
			int middleIndex = leftIndex + (rightIndex - leftIndex) / 2;
			final Author middleAuthor = _author(books, middleIndex);
			final int result = author.compareTo(middleAuthor);
			if (result > 0) {
				leftIndex = middleIndex + 1;
			} else if (result < 0) {
				rightIndex = middleIndex;
			} else {
				for (int i = middleIndex; i >= 0; --i) {
					BookDescription book = (BookDescription)books.get(i);
					if (!author.equals(book.getAuthor())) {
						break;
					}
					set.add(book.getSeriesName());
				}
				for (int i = middleIndex + 1; i <= rightIndex; ++i) {
					BookDescription book = (BookDescription)books.get(i);
					if (!author.equals(book.getAuthor())) {
						break;
					}
					set.add(book.getSeriesName());
				}
				break;
			}
		}
	}

	public void removeTag(String tag, boolean includeSubTags) {
		synchronize();
		final ArrayList books = myBooks;
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			new BookDescription.WritableBookDescription(
				(BookDescription)books.get(i)
			).removeTag(tag, includeSubTags);
		}
	}

	public void renameTag(String from, String to, boolean includeSubTags) {
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(to);
		if ((checkedName.length() > 0) && !checkedName.equals(from)) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).renameTag(from, checkedName, includeSubTags);
			}
		}
	}

	public void cloneTag(String from, String to, boolean includeSubTags) {
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(to);
		if ((checkedName.length() > 0) && !checkedName.equals(from)) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).cloneTag(from, checkedName, includeSubTags);
			}
		}
	}

	public void addTagToAllBooks(String tag) {
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(tag);
		if (checkedName.length() != 0) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).addTag(checkedName, false);
			}
		}
	}

	public void addTagToBooksWithNoTags(String tag) {
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(tag);
		if (checkedName.length() != 0) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				BookDescription description = (BookDescription)books.get(i);
				if (description.getTags().isEmpty()) {
					new BookDescription.WritableBookDescription(description).addTag(checkedName, false);
				}
			}
		}
	}

	public boolean hasBooks(String tag) {
		synchronize();
		final ArrayList books = myBooks;
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			final ArrayList tags = ((BookDescription)books.get(i)).getTags();
			final int tagsLen = tags.size();
			for (int j  = 0; j < tagsLen; ++j) {
				if (tag.equals(tags.get(j))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasSubtags(String tag) {
		synchronize();
		final String prefix = tag + '/';
		final ArrayList books = myBooks;
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			final ArrayList tags = ((BookDescription)books.get(i)).getTags();
			final int tagsLen = tags.size();
			for (int j  = 0; j < tagsLen; ++j) {
				if (((String)tags.get(j)).startsWith(prefix)) {
					return true;
				}
			}
		}
		return false;
	}
	*/
}
