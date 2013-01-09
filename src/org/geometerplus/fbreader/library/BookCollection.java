/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public class BookCollection {
	private final List<ChangeListener> myListeners = Collections.synchronizedList(new LinkedList<ChangeListener>());

	public interface ChangeListener {
		public enum Code {
			BookAdded,
			BookRemoved,
			BuildStarted,
			BuildNotStarted,
			BuildSucceeded,
			BuildCompleted
		}

		void onCollectionChanged(Code code, Book book);
	}

	public void addChangeListener(ChangeListener listener) {
		myListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		myListeners.remove(listener);
	}

	protected void fireModelChangedEvent(ChangeListener.Code code, Book book) {
		synchronized (myListeners) {
			for (ChangeListener l : myListeners) {
				l.onCollectionChanged(code, book);
			}
		}
	}
	private final BooksDatabase myDatabase;
	private final Map<ZLFile,Book> myBooks =
		Collections.synchronizedMap(new HashMap<ZLFile,Book>());
	private volatile boolean myBuildStarted = false;

	public BookCollection(BooksDatabase db) {
		myDatabase = db;
	}

	public int size() {
		return myBooks.size();
	}

	private void addBook(Book book) {
		if (book == null || myBooks.containsKey(book.File)) {
			return;
		}
		myBooks.put(book.File, book);
		fireModelChangedEvent(ChangeListener.Code.BookAdded, book);
	}

	public void removeBook(Book book) {
		myBooks.remove(book.File);
	}

	public List<Book> books() {
		synchronized (myBooks) {
			return new ArrayList<Book>(myBooks.values());
		}
	}

	public synchronized void startBuild() {
		if (myBuildStarted) {
			fireModelChangedEvent(ChangeListener.Code.BuildNotStarted, null);
			return;
		}
		myBuildStarted = true;

		final Thread builder = new Thread("Library.build") {
			public void run() {
				try {
					fireModelChangedEvent(ChangeListener.Code.BuildStarted, null);
					build();
					fireModelChangedEvent(ChangeListener.Code.BuildSucceeded, null);
				} finally {
					fireModelChangedEvent(ChangeListener.Code.BuildCompleted, null);
					myBuildStarted = false;
				}
			}
		};
		builder.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		builder.start();
	}

	private void build() {
		// Step 0: get database books marked as "existing"
		final FileInfoSet fileInfos = new FileInfoSet();
		final Map<Long,Book> savedBooksByFileId = myDatabase.loadBooks(fileInfos, true);
		final Map<Long,Book> savedBooksByBookId = new HashMap<Long,Book>();
		for (Book b : savedBooksByFileId.values()) {
			savedBooksByBookId.put(b.getId(), b);
		}

		// Step 1: set myDoGroupTitlesByFirstLetter value,
		// add "existing" books into recent and favorites lists
		//if (savedBooksByFileId.size() > 10) {
		//	final HashSet<String> letterSet = new HashSet<String>();
		//	for (Book book : savedBooksByFileId.values()) {
		//		final String letter = TitleTree.firstTitleLetter(book);
		//		if (letter != null) {
		//			letterSet.add(letter);
		//		}
		//	}
		//	myDoGroupTitlesByFirstLetter = savedBooksByFileId.values().size() > letterSet.size() * 5 / 4;
		//}

		for (long id : myDatabase.loadRecentBookIds()) {
			Book book = savedBooksByBookId.get(id);
			if (book == null) {
				book = Book.getById(id);
				if (book != null && !book.File.exists()) {
					book = null;
				}
			}
			addBook(book);
			//if (book != null) {
			//	new BookTree(getFirstLevelTree(ROOT_RECENT), book, true);
			//}
		}

		for (long id : myDatabase.loadFavoritesIds()) {
			Book book = savedBooksByBookId.get(id);
			if (book == null) {
				book = Book.getById(id);
				if (book != null && !book.File.exists()) {
					book = null;
				}
			}
			addBook(book);
			//if (book != null) {
			//	getFirstLevelTree(ROOT_FAVORITES).getBookSubTree(book, true);
			//}
		}

		// Step 2: check if files corresponding to "existing" books really exists;
		//         add books to library if yes (and reload book info if needed);
		//         remove from recent/favorites list if no;
		//         collect newly "orphaned" books
		final Set<Book> orphanedBooks = new HashSet<Book>();
		final Set<ZLPhysicalFile> physicalFiles = new HashSet<ZLPhysicalFile>();
		int count = 0;
		for (Book book : savedBooksByFileId.values()) {
			synchronized (this) {
				final ZLPhysicalFile file = book.File.getPhysicalFile();
				if (file != null) {
					physicalFiles.add(file);
				}
				if (file != book.File && file != null && file.getPath().endsWith(".epub")) {
					myDatabase.deleteFromBookList(book.getId());
					continue;
				}
				if (book.File.exists()) {
					boolean doAdd = true;
					if (file == null) {
						continue;
					}
					if (!fileInfos.check(file, true)) {
						try {
							book.readMetaInfo();
							book.save();
						} catch (BookReadingException e) {
							doAdd = false;
						}
						file.setCached(false);
					}
					if (doAdd) {
						addBook(book);
						//addBookToLibrary(book);
					}
				} else {
					//myRootTree.removeBook(book, true);
					//fireModelChangedEvent(ChangeListener.Code.BookRemoved);
					orphanedBooks.add(book);
				}
			}
		}
		myDatabase.setExistingFlag(orphanedBooks, false);

		// Step 3: collect books from physical files; add new, update already added,
		//         unmark orphaned as existing again, collect newly added
		final Map<Long,Book> orphanedBooksByFileId = myDatabase.loadBooks(fileInfos, false);
		final Set<Book> newBooks = new HashSet<Book>();

		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles();
		for (ZLPhysicalFile file : physicalFilesList) {
			if (physicalFiles.contains(file)) {
				continue;
			}
			collectBooks(
				file, fileInfos,
				savedBooksByFileId, orphanedBooksByFileId,
				newBooks,
				!fileInfos.check(file, true)
			);
			file.setCached(false);
		}
		
		// Step 4: add help file
		try {
			final ZLFile helpFile = Library.getHelpFile();
			Book helpBook = savedBooksByFileId.get(fileInfos.getId(helpFile));
			if (helpBook == null) {
				helpBook = new Book(helpFile);
			}
			//addBookToLibrary(helpBook);
			addBook(helpBook);
		} catch (BookReadingException e) {
			// that's impossible
			e.printStackTrace();
		}

		// Step 5: save changes into database
		fileInfos.save();

		myDatabase.executeAsATransaction(new Runnable() {
			public void run() {
				for (Book book : newBooks) {
					book.save();
				}
			}
		});
		myDatabase.setExistingFlag(newBooks, true);
	}

	private List<ZLPhysicalFile> collectPhysicalFiles() {
		final Queue<ZLFile> dirQueue = new LinkedList<ZLFile>();
		final HashSet<ZLFile> dirSet = new HashSet<ZLFile>();
		final LinkedList<ZLPhysicalFile> fileList = new LinkedList<ZLPhysicalFile>();

		dirQueue.offer(new ZLPhysicalFile(new File(Paths.BooksDirectoryOption().getValue())));

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

	private void collectBooks(
		ZLFile file, FileInfoSet fileInfos,
		Map<Long,Book> savedBooksByFileId, Map<Long,Book> orphanedBooksByFileId,
		Set<Book> newBooks,
		boolean doReadMetaInfo
	) {
		final long fileId = fileInfos.getId(file);
		if (savedBooksByFileId.get(fileId) != null) {
			return;
		}

		try {
			final Book book = orphanedBooksByFileId.get(fileId);
			if (book != null) {
				if (doReadMetaInfo) {
					book.readMetaInfo();
				}
				//addBookToLibrary(book);
				addBook(book);
				newBooks.add(book);
				return;
			}
		} catch (BookReadingException e) {
			// ignore
		}

		try {
			final Book book = new Book(file);
			//addBookToLibrary(book);
			addBook(book);
			newBooks.add(book);
			return;
		} catch (BookReadingException e) {
			// ignore
		}

		if (file.isArchive()) {
			for (ZLFile entry : fileInfos.archiveEntries(file)) {
				collectBooks(
					entry, fileInfos,
					savedBooksByFileId, orphanedBooksByFileId,
					newBooks,
					doReadMetaInfo
				);
			}
		}
	}
}
