/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

public abstract class BooksDatabase {
	private static BooksDatabase ourInstance;

	static BooksDatabase Instance() {
		return ourInstance;
	}

	protected BooksDatabase() {
		ourInstance = this;
	}

	protected Book createBook(long id, String filePath, String title, String encoding, String language) {
		return new Book(id, ZLFile.createFileByPath(filePath), title, encoding, language);
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

	protected abstract Map<String,Book> listBooks();
	protected abstract void executeAsATransaction(Runnable actions);
	protected abstract long loadBook(Book book);
	protected abstract List<Author> loadAuthors(long bookId);
	protected abstract List<Tag> loadTags(long bookId);
	protected abstract SeriesInfo loadSeriesInfo(long bookId);
	protected abstract void updateBookInfo(long bookId, String encoding, String language, String title);
	protected abstract long insertBookInfo(String fileName, String encoding, String language, String title);
	protected abstract void deleteAllBookAuthors(long bookId);
	protected abstract void saveBookAuthorInfo(long bookId, long index, Author author);
	protected abstract void deleteAllBookTags(long bookId);
	protected abstract void saveBookTagInfo(long bookId, Tag tag);
	protected abstract void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo);
	protected abstract void resetBookInfo(String fileName);

	protected FileInfo createFileInfo(long id, String name, FileInfo parent) {
		return new FileInfo(name, parent, id);
	}

	protected abstract Collection<FileInfo> loadFileInfos();
	protected abstract Collection<FileInfo> loadFileInfos(ZLFile file);
	protected abstract void removeFileInfo(long fileId);
	protected abstract void saveFileInfo(FileInfo fileInfo);

	protected abstract List<Long> listRecentBookIds();
	protected abstract void saveRecentBookIds(final List<Long> ids);

	protected Bookmark createBookmark(String text, Date creationDate, Date modificationDate, Date accessDate, int accessCounter, int paragraphIndex, int wordIndex, int charIndex) {
		return new Bookmark(text, creationDate, modificationDate, accessDate, accessCounter, paragraphIndex, wordIndex, charIndex);
	}

	protected abstract void listBookmarks(long bookId, List<Bookmark> list);
	protected abstract void saveBookmarks(long bookId, List<Bookmark> list);
}
