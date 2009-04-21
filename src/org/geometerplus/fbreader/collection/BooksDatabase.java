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

package org.geometerplus.fbreader.collection;

import java.util.Map;
import java.util.ArrayList;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class BooksDatabase {
	private static BooksDatabase ourInstance;

	static BooksDatabase Instance() {
		return ourInstance;
	}

	protected BooksDatabase() {
		ourInstance = this;
	}

	protected BookDescription createDescription(long bookId, String filePath, String title, String encoding, String language) {
		return new BookDescription(bookId, ZLFile.createFile(filePath), title, encoding, language);
	}

	protected void addAuthor(BookDescription description, Author author) {
		description.addAuthorWithNoCheck(author);
	}

	protected void addTag(BookDescription description, Tag tag) {
		description.addTagWithNoCheck(tag);
	}

	protected void setSeriesInfo(BookDescription description, String series, long index) {
		description.setSeriesInfoWithNoCheck(series, index);
	}

	protected abstract Map<String,BookDescription> listBooks();
	protected abstract void executeAsATransaction(Runnable actions);
	protected abstract long loadBook(BookDescription description);
	protected abstract ArrayList<Author> loadAuthors(long bookId);
	protected abstract ArrayList<Tag> loadTags(long bookId);
	protected abstract SeriesInfo loadSeriesInfo(long bookId);
	protected abstract void updateBookInfo(long bookId, String encoding, String language, String title);
	protected abstract long insertBookInfo(String fileName, String encoding, String language, String title);
	protected abstract void deleteAllBookAuthors(long bookId);
	protected abstract void saveBookAuthorInfo(long bookId, long index, Author author);
	protected abstract void deleteAllBookTags(long bookId);
	protected abstract void saveBookTagInfo(long bookId, Tag tag);
	protected abstract void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo);
	protected abstract void resetBookInfo(String fileName);
}
