package org.fbreader.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.fbreader.description.Author;
import org.fbreader.description.BookDescription;
import org.fbreader.description.BookDescriptionUtil;
import org.fbreader.formats.FormatPlugin.PluginCollection;
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

	private final ArrayList myAuthors = new ArrayList();
	private	HashMap myCollection = new HashMap();
	private	final ArrayList myExternalBooks = new ArrayList();

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
	
	private void addExternalBook(BookDescription bookDescription) {
		if (!myExternalBooks.contains(bookDescription)) {
			myExternalBooks.add(bookDescription);
		}
	}

	public ArrayList authors() {
		synchronize();
		return myAuthors;
	}
	
	public ArrayList books(Author author) {
		synchronize();
		return (ArrayList)myCollection.get(author);
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

			final ArrayList fileNamesSet = collectBookFileNames();
			final int sizeOfSet = fileNamesSet.size();
			for (int i = 0; i < sizeOfSet; ++i) {
				addDescription(BookDescription.getDescription((String)fileNamesSet.get(i)));
			}
			

			final ArrayList bookListFileNames = new BookList().fileNames();
			final int sizeOfList = bookListFileNames.size();
			for (int i = 0; i < sizeOfList; ++i) {
				final String fileName = (String)bookListFileNames.get(i);
				if (!fileNamesSet.contains(fileName)) {
					BookDescription description = BookDescription.getDescription(fileName);
					if (description != null) {
						addDescription(description);
						addExternalBook(description);
					}
				}
			}
		} else {
			
			// something strange :(
			/*final BookList bookList = new BookList();
			final ArrayList bookListSet = bookList.fileNames();
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
		//return new ArrayList();
		ArrayList nameQueue = new ArrayList();
		ArrayList nameSet = new ArrayList();
		
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
						for (int i = 0; i <  subdirs.size(); ++i) {
							nameQueue.add(dir.getItemPath((String)subdirs.get(i)));
						}
					}
				}
				nameSet.add(name);
			}
		}
		return nameSet;
	}
	
	private ArrayList collectBookFileNames() {
		final ArrayList bookFileNames = new ArrayList();
		final ArrayList dirs = collectDirNames();
		final int numberOfDirs = dirs.size();
		for (int i = 0; i < numberOfDirs; ++i) {
			final String dirfile = (String)dirs.get(i);
			final ZLDir dir = new ZLFile(dirfile).getDirectory();
			if (dir == null) {
				continue;
			}

			final ArrayList files = dir.collectFiles();

			final int numberOfFiles = files.size();
			for (int j = 0; i < numberOfFiles; ++j) {
				String fileName = dir.getItemPath((String)files.get(j));
				ZLFile file = new ZLFile(fileName);
				if (PluginCollection.instance().getPlugin(file, true) != null) {
					if (!bookFileNames.contains(fileName)) {
						bookFileNames.add(fileName);
					}
				// TODO: zip -> any archive
				} else if (file.getExtension() == "zip") {
					if (!BookDescriptionUtil.checkInfo(file)) {
						BookDescriptionUtil.resetZipInfo(file);
						BookDescriptionUtil.saveInfo(file);
					}
					final ArrayList zipEntries = new ArrayList();
					BookDescriptionUtil.listZipEntries(file, zipEntries);
					final int numberOfZipEntries = zipEntries.size();
					for (int k = 0; k < numberOfZipEntries; ++k) {
						String str = (String)zipEntries.get(i);
						if (!bookFileNames.contains(str)) {
							bookFileNames.add(str);
						}
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
	
	/*private class DescriptionComparator implements Comparator {
			public int compare(Object descr1, Object descr2) {
				BookDescription d1 = (BookDescription)descr1;
				BookDescription d2 = (BookDescription)descr2;
				
				String sequenceName1 = d1.getSequenceName();
				String sequenceName2 = d2.getSequenceName();
				if ((sequenceName1.length() == 0) && (sequenceName2.length() == 0)) {
					return d1.getTitle()
					//return (d1.getTitle(). < d2.getTitle()) ? 0 : 1;
				}
				if (sequenceName1.empty()) {
					return d1.getTitle() < sequenceName2;
				}
				if (sequenceName2.empty()) {
					return sequenceName1 <= d2.getTitle();
				}
				if (!sequenceName1.equals(sequenceName2)) {
					return sequenceName1 < sequenceName2;
				}
				return (d1.getNumberInSequence() < d2.getNumberInSequence());
			}
	}*/
	
	/*class LastOpenedBooks {

		public ZLIntegerRangeOption MaxListSizeOption;

		public LastOpenedBooks();
		public	void addBook(String fileName);
		public	Books books() ;

		private:
			Books myBooks;
		};
	}*/
}
