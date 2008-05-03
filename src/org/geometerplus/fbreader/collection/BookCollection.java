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
/*
	void collectSeriesNames(AuthorPtr author, std::set<std::string> &list) const;
	void removeTag(const std::string &tag, bool includeSubTags);
	void renameTag(const std::string &from, const std::string &to, bool includeSubTags);
	void cloneTag(const std::string &from, const std::string &to, bool includeSubTags);
	void addTagToAllBooks(const std::string &to);
	void addTagToBooksWithNoTags(const std::string &to);
	bool hasBooks(const std::string &tag) const;
	bool hasSubtags(const std::string &tag) const;
*/
	public final ZLStringOption PathOption =
		new ZLStringOption(ZLOption.CONFIG_CATEGORY, "Options", "BookPath", "/Books");
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

		/*
		final ArrayList bookFileNames = new ArrayList();
		final int numberOfDirs = dirs.size();
		for (int i = 0; i < numberOfDirs; ++i) {
			final String dirfile = (String)dirs.get(i);
			final ZLDir dir = new ZLFile(dirfile).getDirectory();
			if (dir == null) {
				continue;
			}

			final PluginCollection collection = PluginCollection.instance();
			final ArrayList files = dir.collectFiles();
			final int numberOfFiles = files.size();
			for (int j = 0; j < numberOfFiles; ++j) {
				String fileName = dir.getItemPath((String)files.get(j));
				ZLFile file = new ZLFile(fileName);
				if (collection.getPlugin(file, true) != null) {
					if (!bookFileNames.contains(fileName)) {
						bookFileNames.add(fileName);
					}
				// TODO: zip -> any archive
				} else if (file.getExtension().equals("zip")) {
					if (!BookDescriptionUtil.checkInfo(file)) {
						BookDescriptionUtil.resetZipInfo(file);
						BookDescriptionUtil.saveInfo(file);
					}
					final ArrayList zipEntries = new ArrayList();
					BookDescriptionUtil.listZipEntries(file, zipEntries);
					final int numberOfZipEntries = zipEntries.size();
					for (int k = 0; k < numberOfZipEntries; ++k) {
						String str = (String)zipEntries.get(k);
						if (!bookFileNames.contains(str)) {
							bookFileNames.add(str);
						}
					}
				}
			}
		}
		*/
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

	static public class LastOpenedBooks {
		public ZLIntegerRangeOption MaxListSizeOption;
		static private final String GROUP = "LastOpenedBooks";
		static private final String BOOK = "Book";
		private final ArrayList/*BookDescription*/ myBooks = new ArrayList();

		public LastOpenedBooks() {
			MaxListSizeOption = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, GROUP, "MaxSize", 1, 100, 10);
			final int size = MaxListSizeOption.getValue();
			for (int i = 0; i < size; ++i) {
				String num = BOOK;
				num += i;
				String name = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, num, "").getValue();
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

		public	ArrayList/*BookDescription*/ books() {
			return myBooks;
		}

		public void save() {
			int size = Math.min(MaxListSizeOption.getValue(), myBooks.size());
			for (int i = 0; i < size; ++i) {
				String num = BOOK;
				num += i;
				new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, num, "").setValue(((BookDescription)myBooks.get(i)).FileName);
			}
		}
	}
}
