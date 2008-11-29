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

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.fbreader.description.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public class BookCollection {
	public final ZLStringOption PathOption =
		new ZLStringOption(ZLOption.CONFIG_CATEGORY, "Options", "BookPath", "/sdcard");
	public final ZLBooleanOption ScanSubdirsOption =
		new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "Options", "ScanSubdirs", false);

	private final ArrayList myBooks = new ArrayList();
	private final ArrayList myAuthors = new ArrayList();
	private	final HashSet myExternalBooks = new HashSet();

	private	String myPath;
	private	boolean myScanSubdirs;
	private	boolean myDoStrongRebuild = true;
	private	boolean myDoWeakRebuild;

	public ArrayList books() {
		synchronize();
		return myBooks;
	}

	public boolean isBookExternal(BookDescription description) {
		synchronize();
		return myExternalBooks.contains(description);
	}

	public	void rebuild(boolean strong) {
		if (strong) {
			myDoStrongRebuild = true;
		} else {
			myDoWeakRebuild = true;
		}
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

	public	boolean synchronize() {
		boolean doStrongRebuild =
			myDoStrongRebuild ||
			(myScanSubdirs != ScanSubdirsOption.getValue()) ||
			(myPath != PathOption.getValue());

		if (!doStrongRebuild && !myDoWeakRebuild) {
			return false;
		}

		myPath = PathOption.getValue();
		myScanSubdirs = ScanSubdirsOption.getValue();
		myDoWeakRebuild = false;
		myDoStrongRebuild = false;

		if (doStrongRebuild) {
			myBooks.clear();
			myAuthors.clear();
			myExternalBooks.clear();

			final HashSet fileNamesSet = collectBookFileNames();
			for (Iterator it = fileNamesSet.iterator(); it.hasNext(); ) {
				addDescription(BookDescription.getDescription((String)it.next()));
			}

			final ArrayList bookListFileNames = new BookList().fileNames();
			final int sizeOfList = bookListFileNames.size();
			for (int i = 0; i < sizeOfList; ++i) {
				final String fileName = (String)bookListFileNames.get(i);
				if (!fileNamesSet.contains(fileName)) {
					BookDescription description = BookDescription.getDescription(fileName);
					if (description != null) {
						addDescription(description);
						myExternalBooks.add(description);
					}
				}
			}
		} else {
			final BookList bookList = new BookList();
			final ArrayList bookListFileNames = bookList.fileNames();
			final ArrayList fileNames = new ArrayList();

			final ArrayList books = myBooks;
			final int booksLen = books.size();
			for (int i = 0; i < booksLen; ++i) {
				final BookDescription book = (BookDescription)books.get(i);
				final String bookFileName = book.FileName;
				if (!myExternalBooks.contains(book) || bookListFileNames.contains(bookFileName)) {
					fileNames.add(bookFileName);
				}
			}
			myBooks.clear();
			myAuthors.clear();

			final int fileNamesLen = fileNames.size();
			for (int i = 0; i < fileNamesLen; ++i) {
				addDescription(BookDescription.getDescription((String)fileNames.get(i), false));
			}	
		}

		Collections.sort(myBooks);
		return true;
	}

	private HashSet collectDirNames() {
		ArrayList nameQueue = new ArrayList();
		HashSet nameSet = new HashSet();

		String path = myPath;
		int pos = path.indexOf(File.pathSeparator);
		while (pos != -1) {
			nameQueue.add(path.substring(0, pos));
			path = path.substring(0, pos + 1);
			pos = path.indexOf(File.pathSeparator);
		}
		if (path.length() != 0) {
			nameQueue.add(path);
		}

		while (!nameQueue.isEmpty()) {
			String name = (String)nameQueue.get(0);
			nameQueue.remove(0);
			if (!nameSet.contains(name)) {
				if (myScanSubdirs) {
					ZLDir dir = new ZLFile(name).getDirectory();
					if (dir != null) {
						ArrayList subdirs = dir.collectSubDirs();
						for (int i = 0; i < subdirs.size(); ++i) {
							nameQueue.add(dir.getItemPath((String)subdirs.get(i)));
						}
					}
				}
				nameSet.add(name);
			}
		}
		return nameSet;
	}

	private void addDescription(BookDescription description) {
		if (description != null) {
			myBooks.add(description);
		}
	}

	public ArrayList authors() {
		synchronize();
		if (myAuthors.isEmpty() && !myBooks.isEmpty()) {
			final ArrayList books = myBooks;
			final ArrayList authors = myAuthors;
			final int len = books.size();
			Author author = null;
			for (int i = 0; i < len; ++i) {
				Author newAuthor = ((BookDescription)books.get(i)).getAuthor();
				if ((author == null) ||
						(author.getSortKey() != newAuthor.getSortKey()) ||
						(author.getDisplayName() != newAuthor.getDisplayName())) {
					author = newAuthor;
					authors.add(author);
				}
			}
		}

		return myAuthors;
	}

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

	static public class LastOpenedBooks {
		public ZLIntegerRangeOption MaxListSizeOption;
		static private final String GROUP = "LastOpenedBooks";
		static private final String BOOK = "Book";
		private final ArrayList myBooks = new ArrayList();

		public LastOpenedBooks() {
			MaxListSizeOption = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, GROUP, "MaxSize", 1, 100, 10);
			final int size = MaxListSizeOption.getValue();
			final ZLStringOption option = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, "", "");
			for (int i = 0; i < size; ++i) {
				option.changeName(BOOK + i);
				String name = option.getValue();
				if (name.length() != 0) {
					BookDescription description = BookDescription.getDescription(name);
					if (description != null) {
						myBooks.add(description);
					}
				}
			}
		}

		public	void addBook(String fileName) {
			for (int i = 0; i < myBooks.size(); i++) {
				if (((BookDescription)(myBooks.get(i))).FileName.equals(fileName)) {
					myBooks.remove(myBooks.get(i));
					break;
				}
			}

			BookDescription description = BookDescription.getDescription(fileName);
			if (description != null) {
				myBooks.add(0, description);
			}

			final int maxSize = MaxListSizeOption.getValue();
			while (myBooks.size() > maxSize) {
				myBooks.remove(myBooks.size() - 1);
			}
			save();
		}

		public ArrayList books() {
			return myBooks;
		}

		public void save() {
			final ZLStringOption option = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, "", "");
			int size = Math.min(MaxListSizeOption.getValue(), myBooks.size());
			for (int i = 0; i < size; ++i) {
				option.changeName(BOOK + i);
				option.setValue(((BookDescription)myBooks.get(i)).FileName);
			}
		}
	}
}
