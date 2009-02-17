/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

	private final TreeMap<Author,TreeSet<BookDescription>> myAuthorToBookMap =
		new TreeMap<Author,TreeSet<BookDescription>>();
	private final TreeMap<String,TreeSet<BookDescription>> myTagsToBookMap =
		new TreeMap<String,TreeSet<BookDescription>>();
	private final TreeSet<BookDescription> myBooksWithoutTags = new TreeSet<BookDescription>();

	private	boolean myDoRebuild = true;

	private BookCollection() {
	}

	public Collection<Author> authors() {
		synchronize();
		return myAuthorToBookMap.keySet();
	}

	public Collection<String> tags() {
		synchronize();
		return myTagsToBookMap.keySet();
	}

	public Collection<BookDescription> booksByAuthor(Author author) {
		synchronize();
		return Collections.unmodifiableSet(myAuthorToBookMap.get(author));
	}

	public Collection<BookDescription> booksByTag(String tag) {
		synchronize();
		return Collections.unmodifiableSet((tag != null) ? myTagsToBookMap.get(tag) : myBooksWithoutTags);
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

	public void synchronize() {
		if (myDoRebuild) {
			myAuthorToBookMap.clear();
			myTagsToBookMap.clear();
			myBooksWithoutTags.clear();

			final HashSet fileNamesSet = collectBookFileNames();
			for (Iterator it = fileNamesSet.iterator(); it.hasNext(); ) {
				addDescription(BookDescription.getDescription((String)it.next()));
			}
			myDoRebuild = false;
		}
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

	private void addDescription(BookDescription description) {
		if (description != null) {
			List<String> tags = description.getTags();
			if (tags.isEmpty()) {
				myBooksWithoutTags.add(description);
			} else {
				for (String tag : tags) {
					TreeSet<BookDescription> set = myTagsToBookMap.get(tag);
					if (set == null) {
						set = new TreeSet<BookDescription>();
						myTagsToBookMap.put(tag, set);
					}
					set.add(description);
				}
			}

			final Author author = description.getAuthor();
			TreeSet<BookDescription> set = myAuthorToBookMap.get(author);
			if (set == null) {
				set = new TreeSet<BookDescription>();
				myAuthorToBookMap.put(author, set);
			}
			set.add(description);
		}
	}

	private static Author _author(ArrayList books, int index) {
		return ((BookDescription)books.get(index)).getAuthor();
	}

	public void collectSeriesNames(Author author, HashSet set) {
		/*
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
		*/
	}

	public void removeTag(String tag, boolean includeSubTags) {
		/*
		synchronize();
		final ArrayList books = myBooks;
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			new BookDescription.WritableBookDescription(
				(BookDescription)books.get(i)
			).removeTag(tag, includeSubTags);
		}
		*/
	}

	public void renameTag(String from, String to, boolean includeSubTags) {
		/*
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(to);
		if ((checkedName.length() > 0) && !checkedName.equals(from)) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).renameTag(from, checkedName, includeSubTags);
			}
		}
		*/
	}

	public void cloneTag(String from, String to, boolean includeSubTags) {
		/*
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(to);
		if ((checkedName.length() > 0) && !checkedName.equals(from)) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).cloneTag(from, checkedName, includeSubTags);
			}
		}
		*/
	}

	public void addTagToAllBooks(String tag) {
		/*
		final String checkedName = BookDescriptionUtil.removeWhiteSpacesFromTag(tag);
		if (checkedName.length() != 0) {
			synchronize();
			final ArrayList books = myBooks;
			final int len = books.size();
			for (int i = 0; i < len; ++i) {
				new BookDescription.WritableBookDescription((BookDescription)books.get(i)).addTag(checkedName, false);
			}
		}
		*/
	}

	public void addTagToBooksWithNoTags(String tag) {
		/*
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
		*/
	}

	public boolean hasBooks(String tag) {
		/*
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
		*/
		return false;
	}

	public boolean hasSubtags(String tag) {
		/*
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
		*/
		return false;
	}
}
