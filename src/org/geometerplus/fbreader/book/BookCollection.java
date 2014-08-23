/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.io.File;
import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.*;

public class BookCollection extends AbstractBookCollection {
	private final BooksDatabase myDatabase;
	public final List<String> BookDirectories;

	private final Map<ZLFile,Book> myBooksByFile =
		Collections.synchronizedMap(new LinkedHashMap<ZLFile,Book>());
	private final Map<Long,Book> myBooksById =
		Collections.synchronizedMap(new HashMap<Long,Book>());
	private final List<String> myFilesToRescan =
		Collections.synchronizedList(new LinkedList<String>());
	private final DuplicateResolver myDuplicateResolver = new DuplicateResolver();

	private volatile Status myStatus = Status.NotStarted;

	private final Map<Integer,HighlightingStyle> myStyles =
		Collections.synchronizedMap(new TreeMap<Integer,HighlightingStyle>());

	public BookCollection(BooksDatabase db, List<String> bookDirectories) {
		myDatabase = db;
		BookDirectories = Collections.unmodifiableList(new ArrayList<String>(bookDirectories));
	}

	public int size() {
		return myBooksByFile.size();
	}

	public Book getBookByFile(ZLFile bookFile) {
		if (bookFile == null) {
			return null;
		}
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(bookFile);
		if (plugin == null) {
			return null;
		}
		if (!(plugin instanceof BuiltinFormatPlugin) && bookFile != bookFile.getPhysicalFile()) {
			return null;
		}
		try {
			bookFile = plugin.realBookFile(bookFile);
		} catch (BookReadingException e) {
			return null;
		}

		Book book = myBooksByFile.get(bookFile);
		if (book != null) {
			return book;
		}

		final ZLFile otherFile = myDuplicateResolver.findDuplicate(bookFile);
		if (otherFile != null) {
			book = myBooksByFile.get(otherFile);
			if (book != null) {
				return book;
			}
		}

		final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
		if (physicalFile != null && !physicalFile.exists()) {
			return null;
		}

		final FileInfoSet fileInfos = new FileInfoSet(myDatabase, bookFile);

		book = myDatabase.loadBookByFile(fileInfos.getId(bookFile), bookFile);
		if (book != null) {
			book.loadLists(myDatabase);
		}

		if (book != null && fileInfos.check(physicalFile, physicalFile != bookFile)) {
			saveBook(book);
			return book;
		}
		fileInfos.save();

		try {
			if (book == null) {
				book = new Book(bookFile);
			} else {
				book.readMetainfo();
			}
		} catch (BookReadingException e) {
			return null;
		}

		saveBook(book);
		return book;
	}

	public Book getBookById(long id) {
		Book book = myBooksById.get(id);
		if (book != null) {
			return book;
		}

		book = myDatabase.loadBook(id);
		if (book == null || book.File == null || !book.File.exists()) {
			return null;
		}
		book.loadLists(myDatabase);

		final ZLFile bookFile = book.File;
		final ZLPhysicalFile physicalFile = bookFile.getPhysicalFile();
		if (physicalFile == null) {
			// loaded from db
			addBook(book, false);
			return book;
		}
		if (!physicalFile.exists()) {
			return null;
		}

		final FileInfoSet fileInfos = new FileInfoSet(myDatabase, physicalFile);
		if (fileInfos.check(physicalFile, physicalFile != bookFile)) {
			// loaded from db
			addBook(book, false);
			return book;
		}
		fileInfos.save();

		try {
			book.readMetainfo();
			// loaded from db
			addBook(book, false);
			return book;
		} catch (BookReadingException e) {
			return null;
		}
	}

	public Book getBookByUid(UID uid) {
		for (Book book : myBooksById.values()) {
			if (book.matchesUid(uid)) {
				return book;
			}
		}
		final Long bookId = myDatabase.bookIdByUid(uid);
		return bookId != null ? getBookById(bookId) : null;
	}

	public Book getBookByHash(String hash) {
		for (long id : myDatabase.bookIdsByHash(hash)) {
			final Book book = getBookById(id);
			if (book != null && book.File.exists()) {
				return book;
			}
		}
		return null;
	}

	private boolean addBook(Book book, boolean force) {
		if (book == null) {
			return false;
		}

		synchronized (myBooksByFile) {
			final Book existing = myBooksByFile.get(book.File);
			if (existing == null) {
				if (book.getId() == -1 && !book.save(myDatabase, true)) {
					return false;
				}

				final ZLFile duplicate = myDuplicateResolver.findDuplicate(book.File);
				final Book original = duplicate != null ? myBooksByFile.get(duplicate) : null;
				if (original != null) {
					if (new BookMergeHelper(this).merge(original, book)) {
						fireBookEvent(BookEvent.Updated, original);
					}
				} else {
					myBooksByFile.put(book.File, book);
					myDuplicateResolver.addFile(book.File);
					myBooksById.put(book.getId(), book);
					fireBookEvent(BookEvent.Added, book);
				}
				return true;
			} else if (force) {
				existing.updateFrom(book);
				if (existing.save(myDatabase, false)) {
					fireBookEvent(BookEvent.Updated, existing);
					return true;
				}
			}
			return false;
		}
	}

	public synchronized boolean saveBook(Book book) {
		return addBook(book, true);
	}

	public void removeBook(Book book, boolean deleteFromDisk) {
		synchronized (myBooksByFile) {
			myBooksByFile.remove(book.File);
			myDuplicateResolver.removeFile(book.File);
			myBooksById.remove(book.getId());

			final List<Long> ids = myDatabase.loadRecentBookIds();
			if (ids.remove(book.getId())) {
				myDatabase.saveRecentBookIds(ids);
			}
			if (deleteFromDisk) {
				book.File.getPhysicalFile().delete();
			}
			myDatabase.deleteBook(book.getId());
		}
		fireBookEvent(BookEvent.Removed, book);
	}

	public Status status() {
		return myStatus;
	}

	public List<Book> books(BookQuery query) {
		final List<Book> allBooks;
		synchronized (myBooksByFile) {
			//allBooks = new ArrayList<Book>(new LinkedHashSet<Book>(myBooksByFile.values()));
			allBooks = new ArrayList<Book>(myBooksByFile.values());
		}
		final int start = query.Page * query.Limit;
		if (start >= allBooks.size()) {
			return Collections.emptyList();
		}
		final int end = start + query.Limit;
		if (query.Filter instanceof Filter.Empty) {
			return allBooks.subList(start, Math.min(end, allBooks.size()));
		} else {
			int count = 0;
			final List<Book> filtered = new ArrayList<Book>(query.Limit);
			for (Book b : allBooks) {
				if (query.Filter.matches(b)) {
					if (count >= start) {
						filtered.add(b);
					}
					if (++count == end) {
						break;
					}
				}
			}
			return filtered;
		}
	}

	public boolean hasBooks(Filter filter) {
		final List<Book> allBooks;
		synchronized (myBooksByFile) {
			allBooks = new ArrayList<Book>(myBooksByFile.values());
		}
		for (Book b : allBooks) {
			if (filter.matches(b)) {
				return true;
			}
		}
		return false;
	}

	public List<String> titles(BookQuery query) {
		final List<Book> books = books(query);
		final List<String> titles = new ArrayList<String>(books.size());
		for (Book b : books) {
			titles.add(b.getTitle());
		}
		return titles;
	}

	public List<Book> recentBooks() {
		return books(myDatabase.loadRecentBookIds());
	}

	private List<Book> books(List<Long> ids) {
		final List<Book> bookList = new ArrayList<Book>(ids.size());
		for (long id : ids) {
			final Book book = getBookById(id);
			if (book != null) {
				bookList.add(book);
			}
		}
		return bookList;
	}

	public List<Author> authors() {
		final Set<Author> authors = new TreeSet<Author>();
		synchronized (myBooksByFile) {
			for (Book book : myBooksByFile.values()) {
				final List<Author> bookAuthors = book.authors();
				if (bookAuthors.isEmpty()) {
					authors.add(Author.NULL);
				} else {
					authors.addAll(bookAuthors);
				}
			}
		}
		return new ArrayList<Author>(authors);
	}

	public List<Tag> tags() {
		final Set<Tag> tags = new HashSet<Tag>();
		synchronized (myBooksByFile) {
			for (Book book : myBooksByFile.values()) {
				final List<Tag> bookTags = book.tags();
				if (bookTags.isEmpty()) {
					tags.add(Tag.NULL);
				} else {
					for (Tag t : bookTags) {
						for (; t != null; t = t.Parent) {
							tags.add(t);
						}
					}
				}
			}
		}
		return new ArrayList<Tag>(tags);
	}

	public List<String> labels() {
		final Set<String> labels = new HashSet<String>();
		synchronized (myBooksByFile) {
			for (Book book : myBooksByFile.values()) {
				labels.addAll(book.labels());
			}
		}
		return new ArrayList<String>(labels);
	}

	public boolean hasSeries() {
		synchronized (myBooksByFile) {
			for (Book book : myBooksByFile.values()) {
				if (book.getSeriesInfo() != null) {
					return true;
				}
			}
		}
		return false;
	}

	public List<String> series() {
		final Set<String> series = new TreeSet<String>();
		synchronized (myBooksByFile) {
			for (Book book : myBooksByFile.values()) {
				final SeriesInfo info = book.getSeriesInfo();
				if (info != null) {
					series.add(info.Series.getTitle());
				}
			}
		}
		return new ArrayList<String>(series);
	}

	public List<String> firstTitleLetters() {
		synchronized (myBooksByFile) {
			final TreeSet<String> letters = new TreeSet<String>();
			for (Book book : myBooksByFile.values()) {
				final String l = book.firstTitleLetter();
				if (l != null) {
					letters.add(l);
				}
			}
			return new ArrayList<String>(letters);
		}
	}

	public Book getRecentBook(int index) {
		final List<Long> recentIds = myDatabase.loadRecentBookIds();
		return recentIds.size() > index ? getBookById(recentIds.get(index)) : null;
	}

	public void addBookToRecentList(Book book) {
		final List<Long> ids = myDatabase.loadRecentBookIds();
		final Long bookId = book.getId();
		ids.remove(bookId);
		ids.add(0, bookId);
		if (ids.size() > 12) {
			ids.remove(12);
		}
		myDatabase.saveRecentBookIds(ids);
		fireBookEvent(BookEvent.Opened, book);
	}

	private void setStatus(Status status) {
		myStatus = status;
		fireBuildEvent(status);
	}

	public synchronized void startBuild() {
		if (myStatus != Status.NotStarted) {
			return;
		}
		setStatus(Status.Started);

		final Thread builder = new Thread("Library.build") {
			public void run() {
				try {
					build();
					setStatus(Status.Succeeded);
				} catch (Throwable t) {
					setStatus(Status.Failed);
				} finally {
					synchronized (myFilesToRescan) {
						processFilesQueue();
					}
					for (Book book : new ArrayList<Book>(myBooksByFile.values())) {
						getHash(book, false);
					}
				}
			}
		};
		builder.setPriority(Thread.MIN_PRIORITY);
		builder.start();
	}

	public void rescan(String path) {
		synchronized (myFilesToRescan) {
			myFilesToRescan.add(path);
			processFilesQueue();
		}
	}

	private void processFilesQueue() {
		synchronized (myFilesToRescan) {
			if (!myStatus.IsCompleted) {
				return;
			}

			final Set<ZLFile> filesToRemove = new HashSet<ZLFile>();
			for (String path : myFilesToRescan) {
				path = new ZLPhysicalFile(new File(path)).getPath();
				synchronized (myBooksByFile) {
					for (ZLFile f : myBooksByFile.keySet()) {
						if (f.getPath().startsWith(path)) {
							filesToRemove.add(f);
						}
					}
				}
			}

			for (ZLFile file : collectPhysicalFiles(myFilesToRescan)) {
				// TODO:
				// collect books from archives
				// rescan files and check book id
				filesToRemove.remove(file);
				final Book book = getBookByFile(file);
				if (book != null) {
					saveBook(book);
					getHash(book, false);
				}
			}

			for (ZLFile f : filesToRemove) {
				synchronized (myBooksByFile) {
					final Book book = myBooksByFile.remove(f);
					myDuplicateResolver.removeFile(f);
					if (book != null) {
						myBooksById.remove(book.getId());
						fireBookEvent(BookEvent.Removed, book);
					}
				}
			}

			myFilesToRescan.clear();
		}
	}

	private void build() {
		// Step 0: get database books marked as "existing"
		final FileInfoSet fileInfos = new FileInfoSet(myDatabase);
		final Map<Long,Book> savedBooksByFileId = myDatabase.loadBooks(fileInfos, true);
		final Map<Long,Book> savedBooksByBookId = new HashMap<Long,Book>();
		for (Book b : savedBooksByFileId.values()) {
			savedBooksByBookId.put(b.getId(), b);
		}

		// Step 1: check if files corresponding to "existing" books really exists;
		//         add books to library if yes (and reload book info if needed);
		//         remove from recent/favorites list if no;
		//         collect newly "orphaned" books
		final Set<Book> orphanedBooks = new HashSet<Book>();
		final Set<ZLPhysicalFile> physicalFiles = new HashSet<ZLPhysicalFile>();
		int count = 0;
		for (Book book : savedBooksByFileId.values()) {
			final ZLPhysicalFile file = book.File.getPhysicalFile();
			if (file != null) {
				physicalFiles.add(file);
			}
			if (file != book.File && file != null && file.getPath().endsWith(".epub")) {
				continue;
			}
			if (book.File.exists()) {
				boolean doAdd = true;
				if (file == null) {
					continue;
				}
				if (!fileInfos.check(file, true)) {
					try {
						book.readMetainfo();
						saveBook(book);
					} catch (BookReadingException e) {
						doAdd = false;
					}
					file.setCached(false);
				}
				if (doAdd) {
					// loaded from db
					addBook(book, false);
				}
			} else {
				orphanedBooks.add(book);
			}
		}
		myDatabase.setExistingFlag(orphanedBooks, false);

		// Step 2: collect books from physical files; add new, update already added,
		//         unmark orphaned as existing again, collect newly added
		final Map<Long,Book> orphanedBooksByFileId = myDatabase.loadBooks(fileInfos, false);
		final Set<Book> newBooks = new HashSet<Book>();

		final List<ZLPhysicalFile> physicalFilesList = collectPhysicalFiles(BookDirectories);
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

		// Step 3: add help file
		final ZLFile helpFile = BookUtil.getHelpFile();
		Book helpBook = savedBooksByFileId.get(fileInfos.getId(helpFile));
		if (helpBook == null) {
			helpBook = getBookByFile(helpFile);
		}
		saveBook(helpBook);

		// Step 4: save changes into database
		fileInfos.save();

		myDatabase.executeAsTransaction(new Runnable() {
			public void run() {
				for (Book book : newBooks) {
					saveBook(book);
				}
			}
		});
		myDatabase.setExistingFlag(newBooks, true);
	}

	private List<ZLPhysicalFile> collectPhysicalFiles(List<String> paths) {
		final Queue<ZLPhysicalFile> fileQueue = new LinkedList<ZLPhysicalFile>();
		final HashSet<ZLPhysicalFile> dirSet = new HashSet<ZLPhysicalFile>();
		final LinkedList<ZLPhysicalFile> fileList = new LinkedList<ZLPhysicalFile>();

		for (String p : paths) {
			fileQueue.offer(new ZLPhysicalFile(new File(p)));
		}

		while (!fileQueue.isEmpty()) {
			final ZLPhysicalFile entry = fileQueue.poll();
			if (!entry.exists()) {
				continue;
			}
			if (entry.isDirectory()) {
				if (dirSet.contains(entry)) {
					continue;
				}
				dirSet.add(entry);
				for (ZLFile file : entry.children()) {
					fileQueue.add((ZLPhysicalFile)file);
				}
			} else {
				entry.setCached(true);
				fileList.add(entry);
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
					book.readMetainfo();
				}
				newBooks.add(book);
				return;
			}
		} catch (BookReadingException e) {
			// ignore
		}

		final Book book = getBookByFile(file);
		if (book != null) {
			newBooks.add(book);
		} else if (file.isArchive()) {
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

	@Override
	public ZLImage getCover(Book book, int maxWidth, int maxHeight) {
		return BookUtil.getCover(book);
	}

	public List<Bookmark> bookmarks(BookmarkQuery query) {
		return myDatabase.loadBookmarks(query);
	}

	public void saveBookmark(Bookmark bookmark) {
		if (bookmark != null) {
			bookmark.setId(myDatabase.saveBookmark(bookmark));
			if (bookmark.IsVisible) {
				final Book book = getBookById(bookmark.getBookId());
				if (book != null) {
					book.HasBookmark = true;
					fireBookEvent(BookEvent.BookmarksUpdated, book);
				}
			}
		}
	}

	public void deleteBookmark(Bookmark bookmark) {
		if (bookmark != null && bookmark.getId() != -1) {
			myDatabase.deleteBookmark(bookmark);
			if (bookmark.IsVisible) {
				final Book book = getBookById(bookmark.getBookId());
				if (book != null) {
					book.HasBookmark = myDatabase.hasVisibleBookmark(bookmark.getBookId());
					fireBookEvent(BookEvent.BookmarksUpdated, book);
				}
			}
		}
	}

	public ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		return myDatabase.getStoredPosition(bookId);
	}

	public void storePosition(long bookId, ZLTextPosition position) {
		if (bookId != -1) {
			myDatabase.storePosition(bookId, position);
		}
	}

	public boolean isHyperlinkVisited(Book book, String linkId) {
		return book.isHyperlinkVisited(myDatabase, linkId);
	}

	public void markHyperlinkAsVisited(Book book, String linkId) {
		book.markHyperlinkAsVisited(myDatabase, linkId);
	}

	private synchronized void initStylesTable() {
		if (myStyles.isEmpty()) {
			for (HighlightingStyle style : myDatabase.loadStyles()) {
				myStyles.put(style.Id, style);
			}
		}
	}

	public HighlightingStyle getHighlightingStyle(int styleId) {
		initStylesTable();
		return myStyles.get(styleId);
	}

	public List<HighlightingStyle> highlightingStyles() {
		initStylesTable();
		return new ArrayList<HighlightingStyle>(myStyles.values());
	}

	public void saveHighlightingStyle(HighlightingStyle style) {
		myStyles.put(style.Id, style);
		myDatabase.saveStyle(style);
		fireBookEvent(BookEvent.BookmarkStyleChanged, null);
	}

	public String getHash(Book book, boolean force) {
		final ZLPhysicalFile file = book.File.getPhysicalFile();
		if (file == null) {
			return null;
		}
		String hash = null;
		try {
			hash = myDatabase.getHash(book.getId(), file.javaFile().lastModified());
		} catch (BooksDatabase.NotAvailable e) {
			if (!force) {
				return null;
			}
		}
		if (hash == null) {
			final UID uid = BookUtil.createUid(book.File, "SHA-1");
			if (uid == null) {
				return null;
			}
			hash = uid.Id.toLowerCase();
			try {
				myDatabase.setHash(book.getId(), hash);
			} catch (BooksDatabase.NotAvailable e) {
				// ignore
			}
		}
		return hash;
	}
}
