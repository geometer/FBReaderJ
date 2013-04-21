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

package org.geometerplus.fbreader.book;

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.view.ZLTextPosition;

public interface IBookCollection {
	public enum Status {
		NotStarted(false),
		Started(false),
		Succeeded(true),
		Failed(true);

		public final Boolean IsCompleted;

		Status(boolean completed) {
			IsCompleted = completed;
		}
	}

	public interface Listener {
		void onBookEvent(BookEvent event, Book book);
		void onBuildEvent(Status status);
	}

	public void addListener(Listener listener);
	public void removeListener(Listener listener);

	Status status();

	int size();

	List<Book> books(Query query);
	boolean hasBooks(Query query);
	List<String> titles(Query query);

	List<Book> booksForLabel(String label);
	List<Book> booksForTag(Tag tag);

	List<String> labels();
	List<String> labels(Book book);
	void setLabel(Book book, String label);
	void removeLabel(Book book, String label);

	List<Book> recentBooks();
	Book getRecentBook(int index);
	void addBookToRecentList(Book book);

	Book getBookByFile(ZLFile file);
	Book getBookById(long id);
	Book getBookByUid(UID uid);

	List<Author> authors();
	boolean hasSeries();
	List<String> series();
	List<Tag> tags();
	List<String> firstTitleLetters();
	List<String> titlesForTag(Tag tag, int limit);

	boolean saveBook(Book book, boolean force);
	void removeBook(Book book, boolean deleteFromDisk);

	ZLTextPosition getStoredPosition(long bookId);
	void storePosition(long bookId, ZLTextPosition position);

	boolean isHyperlinkVisited(Book book, String linkId);
	void markHyperlinkAsVisited(Book book, String linkId);

	List<Bookmark> bookmarks(long fromId, int limitCount);
	List<Bookmark> bookmarksForBook(Book book, long fromId, int limitCount);
	List<Bookmark> invisibleBookmarks(Book book);
	void saveBookmark(Bookmark bookmark);
	void deleteBookmark(Bookmark bookmark);
}
