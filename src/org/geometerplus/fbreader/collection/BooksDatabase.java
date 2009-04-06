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

import java.util.ArrayList;

public abstract class BooksDatabase {
	private static BooksDatabase ourInstance;

	static BooksDatabase Instance() {
		return ourInstance;
	}

	protected BooksDatabase() {
		ourInstance = this;
	}

	public abstract void executeAsATransaction(Runnable actions);
	public abstract long loadBook(BookDescription description);
	public abstract ArrayList<Author> loadAuthors(long bookId);
	public abstract ArrayList<Tag> loadTags(long bookId);
	public abstract SeriesInfo loadSeriesInfo(long bookId);
	public abstract void updateBookInfo(long bookId, String encoding, String language, String title);
	public abstract long insertBookInfo(String fileName, String encoding, String language, String title);
	public abstract void saveBookAuthorInfo(long bookId, long index, Author author);
	public abstract void saveBookTagInfo(long bookId, Tag tag);
	public abstract void saveBookSeriesInfo(long bookId, SeriesInfo seriesInfo);
	public abstract void resetBookInfo(String fileName);
}
