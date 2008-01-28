package org.fbreader.collection;

import java.util.*;

import org.fbreader.description.Author;
import org.fbreader.description.BookDescription;
import org.fbreader.description.BookDescriptionUtil;
import org.fbreader.formats.FormatPlugin.PluginCollection;
import org.zlibrary.core.util.*;
import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;

public class BookCollection {
	public final ZLStringOption PathOption;
	public final ZLBooleanOption ScanSubdirsOption;
	private final static String OPTIONS = "Options";

	private ArrayList myAuthors;
	private	HashMap myCollection;
	private	HashSet myExternalBooks;

	private	String myPath;
	private	boolean myScanSubdirs;
	private	boolean myDoStrongRebuild;
	private	boolean myDoWeakRebuild;

	public BookCollection() {
		PathOption = new ZLStringOption(ZLOption.CONFIG_CATEGORY, OPTIONS, "BookPath", "");
		ScanSubdirsOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, OPTIONS, "ScanSubdirs", false);
		myDoStrongRebuild = true;
		myDoWeakRebuild = false;
	}

	public ArrayList getAuthors() {
		synchronize();
		return myAuthors;
	}
	
	public ArrayList getBooks(Author author) {
		synchronize();
		return (ArrayList)(myCollection.get(author));
	}
	
	//public boolean isBookExternal(BookDescription description);

	public	void rebuild(boolean strong) {
		if (strong) {
			myDoStrongRebuild = true;
		} else {
			myDoWeakRebuild = true;
		}
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
			myAuthors.clear();
			myCollection.clear();
			myExternalBooks.clear();

			final HashSet fileNamesSet = collectBookFileNames();
			// TODO: !!!
			/*
			for (Iterator it = fileNamesSet.iterator(); it.hasNext();) {
				addDescription(BookDescription.getDescription((String)it.next()));
			}
			*/

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
			// something strange :(
			/*
			final BookList bookList = new BookList();
			final HashSet bookListSet = bookList.fileNames();
			final ArrayList fileNames = new ArrayList();
			for (Iterator it = myCollection.values().iterator(); it.hasNext();) {
				final ArrayList books = (ArrayList)it.next();
				final int numberOfBooks = books.size();
				for (int j = 0; j < numberOfBooks; ++j) {
					final BookDescription description = (BookDescription)books.get(j);
					final String fileName = description.getFileName();
					if (!myExternalBooks.contains(description) || bookListSet.contains(fileName)) {
						fileNames.add(fileName);
					}
				}
			}
			myCollection.clear();
			myAuthors.clear();
			final int fileNamesSize = fileNames.size();
			for (int i = 0; i < fileNamesSize; ++i) {
				addDescription(BookDescription.getDescription((String)fileNames.get(i), false));
			}
			*/
		}

		/*std::sort(myAuthors.begin(), myAuthors.end(), AuthorComparator());
		DescriptionComparator descriptionComparator;
		for (Iterator it = myCollection.entrySet().iterator(); it.hasNext();) {
			std::sort((*it).second.begin(), (*it).second.end(), descriptionComparator);
		}*/
		return true;
	}
		
	private ArrayList collectDirNames() {
		return new ArrayList();
		/*Queue nameQueue;// = new ArrayQueue();

		String path = myPath;
		int pos = path.find(ZLibrary.PathDelimiter);
		while (pos != -1) {
			nameQueue.add(path.substring(0, pos));
			path = path.substring(0, pos + 1);
			pos = path.find(ZLibrary.PathDelimiter);
		}
		if (path.length() != 0) {
			nameQueue.add(path);
		}

		while (!nameQueue.empty()) {
			String name = nameQueue.front();
			nameQueue.pop();
			if (nameSet.find(name) == nameSet.end()) {
				if (myScanSubdirs) {
					shared_ptr<ZLDir> dir = ZLFile(name).directory();
					if (!dir.isNull()) {
						std::vector<std::string> subdirs;
						dir->collectSubDirs(subdirs, false);
						for (std::vector<std::string>::const_iterator it = subdirs.begin(); it != subdirs.end(); ++it) {
							nameQueue.push(dir->itemPath(*it));
						}
					}
				}
				nameSet.insert(name);
			}
		}*/
	}
	
	private HashSet collectBookFileNames() {
		final HashSet bookFileNames = new HashSet();
		final ArrayList dirs = collectDirNames();
		final int numberOfDirs = dirs.size();
		for (int i = 0; i < numberOfDirs; ++i) {
			final String dirfile = (String)dirs.get(i);
			final ArrayList files = new ArrayList();
			final ZLDir dir = new ZLFile(dirfile).directory();
			if (dir == null) {
				continue;
			}

			//dir.collectFiles(files, false);

			final int numberOfFiles = files.size();
			for (int j = 0; i < numberOfFiles; ++j) {
				String fileName = dir.itemPath((String)files.get(j));
				ZLFile file = new ZLFile(fileName);
				if (PluginCollection.instance().plugin(file, true) != null) {
					bookFileNames.add(fileName);
				// TODO: zip -> any archive
				} else if (file.extension() == "zip") {
					if (!BookDescriptionUtil.checkInfo(file)) {
						BookDescriptionUtil.resetZipInfo(file);
						BookDescriptionUtil.saveInfo(file);
					}
					final ArrayList zipEntries = new ArrayList();
					BookDescriptionUtil.listZipEntries(file, zipEntries);
					final int numberOfZipEntries = zipEntries.size();
					for (int k = 0; k < numberOfZipEntries; ++k) {
						bookFileNames.add(zipEntries.get(i));
					}
				}
			}
		}
		return bookFileNames;
	}

	private void addDescription(BookDescription description) {
		if (description == null) {
			return;
		}

		final Author author = description.getAuthor();
		ArrayList books = (ArrayList)myCollection.get(author);
		if (books == null) {
			books = new ArrayList();
			myCollection.put(author, books);
			myAuthors.add(author);
		}
		books.add(description);
	}
}
