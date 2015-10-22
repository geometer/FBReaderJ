/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.SystemInfo;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.formats.*;

public class BookCollection extends AbstractBookCollection<DbBook> {
	private static final String ZERO_HASH = String.format("%040d", 0);

	private final SystemInfo mySystemInfo;
	public final PluginCollection PluginCollection;
	private final BooksDatabase myDatabase;
	public final List<String> BookDirectories;
	private Set<String> myActiveFormats;

	private final Map<ZLFile,DbBook> myBooksByFile =
		Collections.synchronizedMap(new LinkedHashMap<ZLFile,DbBook>());
	private final Map<Long,DbBook> myBooksById =
		Collections.synchronizedMap(new HashMap<Long,DbBook>());
	private final List<String> myFilesToRescan =
		Collections.synchronizedList(new LinkedList<String>());
	private final DuplicateResolver myDuplicateResolver = new DuplicateResolver();

	private volatile Status myStatus = Status.NotStarted;

	private final Map<Integer,HighlightingStyle> myStyles =
		Collections.synchronizedMap(new TreeMap<Integer,HighlightingStyle>());

	public BookCollection(SystemInfo systemInfo, BooksDatabase db, List<String> bookDirectories) {
		mySystemInfo = systemInfo;
		PluginCollection = org.geometerplus.fbreader.formats.PluginCollection.Instance(systemInfo);
		myDatabase = db;
		BookDirectories = Collections.unmodifiableList(new ArrayList<String>(bookDirectories));

		final String formats = db.getOptionValue("formats");
		if (formats != null) {
			myActiveFormats = new HashSet<String>(Arrays.asList(formats.split("\000")));
		}
	}

	public int size() {
		return myBooksByFile.size();
	}

	public DbBook getBookByFile(String path) {
		return getBookByFile(ZLFile.createFileByPath(path));
	}

	private DbBook getBookByFile(ZLFile bookFile) {
		if (bookFile == null) {
			return null;
		}

		return getBookByFile(bookFile, PluginCollection.getPlugin(bookFile));
	}

	private DbBook getBookByFile(ZLFile bookFile, final FormatPlugin plugin) {
		if (plugin == null || !isFormatActive(plugin)) {
			return null;
		}

		try {
			bookFile = plugin.realBookFile(bookFile);
		} catch (BookReadingException e) {
			return null;
		}

		DbBook book = myBooksByFile.get(bookFile);
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
			book.loadLists(myDatabase, PluginCollection);
		}

		if (book != null && fileInfos.check(physicalFile, physicalFile != bookFile)) {
			saveBook(book);
			return book;
		}
		fileInfos.save();

		try {
			if (book == null) {
				book = new DbBook(bookFile, plugin);
			} else {
				BookUtil.readMetainfo(book, plugin);
			}
		} catch (BookReadingException e) {
			return null;
		}

		saveBook(book);
		return book;
	}

	public DbBook getBookById(long id) {
		DbBook book = myBooksById.get(id);
		if (book != null) {
			return book;
		}

		book = myDatabase.loadBook(id);
		if (book == null || book.File == null || !book.File.exists() || !isBookFormatActive(book)) {
			return null;
		}
		book.loadLists(myDatabase, PluginCollection);

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
			BookUtil.readMetainfo(book, PluginCollection);
			// loaded from db
			addBook(book, false);
			return book;
		} catch (BookReadingException e) {
			return null;
		}
	}

	public DbBook getBookByUid(UID uid) {
		for (DbBook book : myBooksById.values()) {
			if (book.matchesUid(uid)) {
				return book;
			}
		}
		final Long bookId = myDatabase.bookIdByUid(uid);
		return bookId != null ? getBookById(bookId) : null;
	}

	public DbBook getBookByHash(String hash) {
		if (ZERO_HASH.equals(hash)) {
			return getBookByFile(BookUtil.getHelpFile());
		}

		for (long id : myDatabase.bookIdsByHash(hash)) {
			final DbBook book = getBookById(id);
			if (book != null && book.File.exists()) {
				return book;
			}
		}
		return null;
	}

	private boolean addBook(DbBook book, boolean force) {
		if (book == null) {
			return false;
		}

		synchronized (myBooksByFile) {
			final DbBook existing = myBooksByFile.get(book.File);
			if (existing == null) {
				if (book.getId() == -1 && book.save(myDatabase, true) == DbBook.WhatIsSaved.Nothing) {
					return false;
				}

				final ZLFile duplicate = myDuplicateResolver.findDuplicate(book.File);
				final DbBook original = duplicate != null ? myBooksByFile.get(duplicate) : null;
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
				switch (existing.save(myDatabase, false)) {
					case Everything:
						fireBookEvent(BookEvent.Updated, existing);
						return true;
					case Progress:
						fireBookEvent(BookEvent.ProgressUpdated, existing);
						return true;
				}
			}
			return false;
		}
	}

	public synchronized boolean saveBook(DbBook book) {
		return addBook(book, true);
	}

	public void removeBook(DbBook book, boolean deleteFromDisk) {
		synchronized (myBooksByFile) {
			myBooksByFile.remove(book.File);
			myDuplicateResolver.removeFile(book.File);
			myBooksById.remove(book.getId());

			if (deleteFromDisk) {
				book.File.getPhysicalFile().delete();
			}
			myDatabase.deleteBook(book.getId());
		}
		fireBookEvent(BookEvent.Removed, book);
	}

	public boolean canRemoveBook(DbBook book, boolean deleteFromDisk) {
		if (deleteFromDisk) {
			ZLFile file = book.File;
			if (file.getPhysicalFile() == null) {
				return false;
			}
			while (file instanceof ZLArchiveEntryFile) {
				file = file.getParent();
				if (file.children().size() != 1) {
					return false;
				}
			}
			return true;
		} else {
			// TODO: implement
			return false;
		}
	}

	public Status status() {
		return myStatus;
	}

	public List<DbBook> books(BookQuery query) {
		if (query == null) {
			return Collections.emptyList();
		}

		final List<DbBook> allBooks;
		synchronized (myBooksByFile) {
			//allBooks = new ArrayList<DbBook>(new LinkedHashSet<DbBook>(myBooksByFile.values()));
			allBooks = new ArrayList<DbBook>(myBooksByFile.values());
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
			final List<DbBook> filtered = new ArrayList<DbBook>(query.Limit);
			for (DbBook b : allBooks) {
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
		final List<DbBook> allBooks;
		synchronized (myBooksByFile) {
			allBooks = new ArrayList<DbBook>(myBooksByFile.values());
		}
		for (DbBook b : allBooks) {
			if (filter.matches(b)) {
				return true;
			}
		}
		return false;
	}

	public List<String> titles(BookQuery query) {
		final List<DbBook> books = books(query);
		final List<String> titles = new ArrayList<String>(books.size());
		for (DbBook b : books) {
			titles.add(b.getTitle());
		}
		return titles;
	}

	public List<DbBook> recentlyAddedBooks(int count) {
		return books(myDatabase.loadRecentBookIds(BooksDatabase.HistoryEvent.Added, count));
	}

	public List<DbBook> recentlyOpenedBooks(int count) {
		return books(myDatabase.loadRecentBookIds(BooksDatabase.HistoryEvent.Opened, count));
	}

	private List<DbBook> books(List<Long> ids) {
		final List<DbBook> bookList = new ArrayList<DbBook>(ids.size());
		for (long id : ids) {
			final DbBook book = getBookById(id);
			if (book != null) {
				bookList.add(book);
			}
		}
		return bookList;
	}

	public List<Author> authors() {
		final Set<Author> authors = new TreeSet<Author>();
		synchronized (myBooksByFile) {
			for (DbBook book : myBooksByFile.values()) {
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
			for (DbBook book : myBooksByFile.values()) {
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
		return myDatabase.listLabels();
	}

	public boolean hasSeries() {
		synchronized (myBooksByFile) {
			for (DbBook book : myBooksByFile.values()) {
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
			for (DbBook book : myBooksByFile.values()) {
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
			for (DbBook book : myBooksByFile.values()) {
				final String l = book.firstTitleLetter();
				if (l != null) {
					letters.add(l);
				}
			}
			return new ArrayList<String>(letters);
		}
	}

	public DbBook getRecentBook(int index) {
		final List<Long> recentIds = myDatabase.loadRecentBookIds(BooksDatabase.HistoryEvent.Opened, index + 1);
		return recentIds.size() > index ? getBookById(recentIds.get(index)) : null;
	}

	public void addToRecentlyOpened(DbBook book) {
		myDatabase.addBookHistoryEvent(book.getId(), BooksDatabase.HistoryEvent.Opened);
		fireBookEvent(BookEvent.Opened, book);
	}

	public void removeFromRecentlyOpened(DbBook book) {
		myDatabase.removeBookHistoryEvents(book.getId(), BooksDatabase.HistoryEvent.Opened);
		fireBookEvent(BookEvent.Updated, book);
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
					t.printStackTrace();
				} finally {
					synchronized (myFilesToRescan) {
						processFilesQueue();
					}
					for (DbBook book : new ArrayList<DbBook>(myBooksByFile.values())) {
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
			if (!myStatus.IsComplete) {
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
				final DbBook book = getBookByFile(file);
				if (book != null) {
					saveBook(book);
					getHash(book, false);
				}
			}

			for (ZLFile f : filesToRemove) {
				synchronized (myBooksByFile) {
					final DbBook book = myBooksByFile.remove(f);
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
		final Map<Long,DbBook> savedBooksByFileId = myDatabase.loadBooks(fileInfos, true);

		// Step 1: check if files corresponding to "existing" books really exists;
		//         add books to library if yes (and reload book info if needed);
		//         collect newly "orphaned" books
		final Set<DbBook> orphanedBooks = new HashSet<DbBook>();
		final Set<ZLPhysicalFile> physicalFiles = new HashSet<ZLPhysicalFile>();
		int count = 0;
		for (DbBook book : savedBooksByFileId.values()) {
			final ZLPhysicalFile file = book.File.getPhysicalFile();
			if (file != null) {
				physicalFiles.add(file);

				// yes, we do add the file to physicalFiles set
				//      for not testing it again later,
				// but we do not store the book
				if (!isBookFormatActive(book)) {
					continue;
				}

				// a hack to skip obsolete *.epub:*.opf entries
				if (file != book.File && file.getPath().endsWith(".epub")) {
					continue;
				}
			}
			if (book.File.exists()) {
				boolean doAdd = true;
				if (file == null) {
					continue;
				}
				if (!fileInfos.check(file, true)) {
					try {
						BookUtil.readMetainfo(book, PluginCollection);
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
		final Map<Long,DbBook> orphanedBooksByFileId = myDatabase.loadBooks(fileInfos, false);
		final Set<DbBook> newBooks = new HashSet<DbBook>();

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
		DbBook helpBook = savedBooksByFileId.get(fileInfos.getId(helpFile));
		if (helpBook == null) {
			helpBook = getBookByFile(helpFile);
		}
		saveBook(helpBook);

		// Step 4: save changes into database
		fileInfos.save();

		myDatabase.executeAsTransaction(new Runnable() {
			public void run() {
				for (DbBook book : newBooks) {
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
		Map<Long,DbBook> savedBooksByFileId, Map<Long,DbBook> orphanedBooksByFileId,
		Set<DbBook> newBooks,
		boolean doReadMetaInfo
	) {
		final long fileId = fileInfos.getId(file);
		if (savedBooksByFileId.get(fileId) != null) {
			return;
		}

		final FormatPlugin plugin = PluginCollection.getPlugin(file);
		if (plugin != null && !isFormatActive(plugin)) {
			return;
		}

		try {
			final DbBook book = orphanedBooksByFileId.get(fileId);
			if (book != null) {
				if (doReadMetaInfo) {
					BookUtil.readMetainfo(book, PluginCollection);
				}
				newBooks.add(book);
				return;
			}
		} catch (BookReadingException e) {
			// ignore
		}

		final DbBook book = getBookByFile(file, plugin);
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
	public String getCoverUrl(DbBook book) {
		// not implemented in non-shadow collection
		return null;
	}

	@Override
	public String getDescription(DbBook book) {
		// not implemented in non-shadow collection
		return null;
	}

	public List<Bookmark> bookmarks(BookmarkQuery query) {
		return myDatabase.loadBookmarks(query);
	}

	public void saveBookmark(Bookmark bookmark) {
		if (bookmark != null) {
			bookmark.setId(myDatabase.saveBookmark(bookmark));
			if (bookmark.IsVisible) {
				final DbBook book = getBookById(bookmark.BookId);
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
				final DbBook book = getBookById(bookmark.BookId);
				if (book != null) {
					book.HasBookmark = myDatabase.hasVisibleBookmark(bookmark.BookId);
					fireBookEvent(BookEvent.BookmarksUpdated, book);
				}
			}
		}
	}

	public List<String> deletedBookmarkUids() {
		return myDatabase.deletedBookmarkUids();
	}

	public void purgeBookmarks(List<String> uids) {
		myDatabase.purgeBookmarks(uids);
	}

	public ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		return myDatabase.getStoredPosition(bookId);
	}

	public void storePosition(long bookId, ZLTextPosition position) {
		if (bookId != -1) {
			myDatabase.storePosition(bookId, position);
		}
	}

	public boolean isHyperlinkVisited(DbBook book, String linkId) {
		return book.isHyperlinkVisited(myDatabase, linkId);
	}

	public void markHyperlinkAsVisited(DbBook book, String linkId) {
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

	public synchronized void saveHighlightingStyle(HighlightingStyle style) {
		myDatabase.saveStyle(style);
		myStyles.clear();
		fireBookEvent(BookEvent.BookmarkStyleChanged, null);
	}

	private final static String DEFAULT_STYLE_ID_KEY = "defaultStyle";
	public int getDefaultHighlightingStyleId() {
		try {
			return Integer.parseInt(myDatabase.getOptionValue(DEFAULT_STYLE_ID_KEY));
		} catch (Throwable t) {
			return 1;
		}
	}

	public void setDefaultHighlightingStyleId(int styleId) {
		myDatabase.setOptionValue(DEFAULT_STYLE_ID_KEY, String.valueOf(styleId));
	}

	public String getHash(DbBook book, boolean force) {
		final ZLPhysicalFile file = book.File.getPhysicalFile();
		if (file == null) {
			return ZERO_HASH;
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

	public void setHash(DbBook book, String hash) {
		try {
			myDatabase.setHash(book.getId(), hash);
		} catch (BooksDatabase.NotAvailable e) {
			// ignore
		}
	}

	public List<FormatDescriptor> formats() {
		final List<FormatPlugin> plugins = PluginCollection.plugins();
		final List<FormatDescriptor> descriptors = new ArrayList<FormatDescriptor>(plugins.size());
		for (FormatPlugin p : plugins) {
			final FormatDescriptor d = new FormatDescriptor();
			d.Id = p.supportedFileType();
			d.Name = p.name();
			d.IsActive = myActiveFormats == null || myActiveFormats.contains(d.Id);
			descriptors.add(d);
		}
		return descriptors;
	}

	public boolean setActiveFormats(List<String> formatIds) {
		final Set<String> activeFormats = new HashSet<String>(formatIds);
		if (activeFormats.equals(myActiveFormats)) {
			return false;
		}

		myActiveFormats = activeFormats;
		myDatabase.setOptionValue("formats", MiscUtil.join(formatIds, "\000"));
		return true;
	}

	private boolean isFormatActive(FormatPlugin plugin) {
		return myActiveFormats == null || myActiveFormats.contains(plugin.supportedFileType());
	}

	private boolean isBookFormatActive(DbBook book) {
		try {
			return isFormatActive(BookUtil.getPlugin(PluginCollection, book));
		} catch (BookReadingException e) {
			return false;
		}
	}

	public DbBook createBook(long id, String url, String title, String encoding, String language) {
		return new DbBook(id, ZLFile.createFileByUrl(url), title, encoding, language);
	}
}
