package org.fbreader.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

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

			HashSet fileNamesSet = new HashSet();
			collectBookFileNames(fileNamesSet);
			for (Iterator it = fileNamesSet.iterator(); it.hasNext();) {
				addDescription(BookDescription.getDescription((String)it.next()));
			}

			BookList bookList = new BookList();
			HashSet bookListSet = bookList.fileNames();
			for (Iterator it = bookListSet.iterator(); it.hasNext();) {
				String itValue = (String)it.next();
				if (!fileNamesSet.contains(itValue)) {
					BookDescription description = BookDescription.getDescription(itValue);
					if (description != null) {
						addDescription(description);
						myExternalBooks.add(description);
					}
				}
			}
		} else {
			BookList bookList = new BookList();
			HashSet bookListSet = bookList.fileNames();
			ArrayList fileNames = new ArrayList();
			for (Iterator it = myCollection.entrySet().iterator(); it.hasNext();) {
				final Map.Entry en =  (Map.Entry)it.next();
				final ArrayList books = (ArrayList)en.getValue();
				
				for (Iterator jt = books.iterator(); jt.hasNext(); ) {
					BookDescription jtValue = (BookDescription)jt.next();
					if ((!myExternalBooks.contains(jtValue)) || 
							(bookListSet.contains(jtValue.getFileName()))) {
						fileNames.add(jtValue.getFileName());
					}
				}
			}
			myCollection.clear();
			myAuthors.clear();
			for (Iterator it = fileNames.iterator(); it.hasNext(); ) {
				addDescription(BookDescription.getDescription((String)it.next(), false));
			}
		}

		/*std::sort(myAuthors.begin(), myAuthors.end(), AuthorComparator());
		DescriptionComparator descriptionComparator;
		for (Iterator it = myCollection.entrySet().iterator(); it.hasNext();) {
			std::sort((*it).second.begin(), (*it).second.end(), descriptionComparator);
		}*/
		return true;
	}
		
	private void collectDirNames(HashSet names) {
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
	
	private void collectBookFileNames(HashSet bookFileNames) {
		HashSet dirs = new HashSet();
		collectDirNames(dirs);

		for (Iterator it = dirs.iterator(); it.hasNext();) {
			String dirfile = (String)it.next();
			ArrayList files = new ArrayList();
			ZLDir dir = new ZLFile(dirfile).directory();
			if (dir == null) {
				continue;
			}
			
			//dir.collectFiles(files, false);
			
			if (!files.isEmpty()) {
				for (Iterator jt = files.iterator(); jt.hasNext();) {
					String jtValue = (String)jt.next();
					String fileName = dir.itemPath(jtValue);
					ZLFile file = new ZLFile(fileName);
					if (PluginCollection.instance().plugin(file, true) != null) {
						bookFileNames.add(fileName);
					// TODO: zip -> any archive
					} else if (file.extension() == "zip") {
						if (!BookDescriptionUtil.checkInfo(file)) {
							BookDescriptionUtil.resetZipInfo(file);
							BookDescriptionUtil.saveInfo(file);
						}
						ArrayList zipEntries = new ArrayList();
						BookDescriptionUtil.listZipEntries(file, zipEntries);
						for (Iterator zit = zipEntries.iterator(); zit.hasNext(); ) {
							bookFileNames.add(zit.next());
						}
					}
				}
			}
		}
	}

	private void addDescription(BookDescription description) {
		if (description == null) {
			return;
		}

		Author author = description.getAuthor();
		String displayName = author.getDisplayName();
		String sortKey = author.getSortKey();

		Iterator it = myCollection.entrySet().iterator();
		
		for (; it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			Author author1 = (Author)entry.getKey();
			if ((author1.getSortKey().equals(sortKey)) && (author1.getDisplayName().equals(displayName))) {
				break;
			}
		}
		
		if (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			((ArrayList)entry.getValue()).add(description);
		} else {
			ArrayList books = new ArrayList();
			books.add(description);
			myCollection.put(author, books);
			myAuthors.add(author);
		}
	}


}
