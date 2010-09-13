/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public abstract class BooksDatabase {
	private static BooksDatabase ourInstance;

	static BooksDatabase Instance() {
		return ourInstance;
	}

	protected BooksDatabase() {
		ourInstance = this;
	}

	protected Book createBook(long id, long fileId, String title, String encoding, String language) {
		final FileInfoSet infos = new FileInfoSet(fileId);
		return createBook(id, infos.getFile(fileId), title, encoding, language);
	}
	protected Book createBook(long id, ZLFile file, String title, String encoding, String language) {
		return (file != null) ? new Book(id, file, title, encoding, language) : null;
	}
	protected void addAuthor(Book book, Author author) {
		book.addAuthorWithNoCheck(author);
	}
	protected void addTag(Book book, Tag tag) {
		book.addTagWithNoCheck(tag);
	}
	protected void setSeriesInfo(Book book, String series, long index) {
		book.setSeriesInfoWithNoCheck(series, index);
	}

	protected abstract void executeAsATransaction(Runnable actions);

	// returns map fileId -> book
	protected abstract Map<Long,Book> listBooks(FileInfoSet infos);
	protected abstract Book loadBook(long bookId);
	protected abstract Book loadBookByFile(long fileId, ZLFile file);

	protected abstract List<Author> loadAuthors(long bookId);
	protected abstract List<Tag> loadTags(long bookId);
	protected abstract SeriesInfo loadSeriesInfo(long bookId);
	protected abstract void updateBookInfo(long bookId, long fileId, String encoding, String language, String title);
	protected abstract long insertBookInfo(ZLFile file, String encoding, String language, String title);
	protected abstract void deleteAllBookAuthors(long bookId);
	protected abstract void saveBookAuthorInfo(long bookId, long index, Author author);
	protected abstract void deleteAllBookTags(long bookId);
	protected abstract void saveBookTagInfo(long bookId, Tag tag);
	protected abstract void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo);

	protected FileInfo createFileInfo(long id, String name, FileInfo parent) {
		return new FileInfo(name, parent, id);
	}

	protected abstract Collection<FileInfo> loadFileInfos();
	protected abstract Collection<FileInfo> loadFileInfos(ZLFile file);
	protected abstract Collection<FileInfo> loadFileInfos(long fileId);
	protected abstract void removeFileInfo(long fileId);
	protected abstract void saveFileInfo(FileInfo fileInfo);

	protected abstract List<Long> listRecentBookIds();
	protected abstract void saveRecentBookIds(final List<Long> ids);

	protected Bookmark createBookmark(long id, long bookId, String bookTitle, String text, Date creationDate, Date modificationDate, Date accessDate, int accessCounter, String modelId, int paragraphIndex, int wordIndex, int charIndex) {
		return new Bookmark(id, bookId, bookTitle, text, creationDate, modificationDate, accessDate, accessCounter, modelId, paragraphIndex, wordIndex, charIndex);
	}

	protected abstract List<Bookmark> listBookmarks(long bookId);
	protected abstract List<Bookmark> listAllBookmarks();
	protected abstract long saveBookmark(Bookmark bookmark);
	protected abstract void deleteBookmark(Bookmark bookmark);

	protected abstract ZLTextPosition getStoredPosition(long bookId);
	protected abstract void storePosition(long bookId, ZLTextPosition position);

	protected abstract boolean insertIntoBookList(long bookId);
	protected abstract boolean deleteFromBookList(long bookId);
	protected abstract boolean checkBookList(long bookId);
}
