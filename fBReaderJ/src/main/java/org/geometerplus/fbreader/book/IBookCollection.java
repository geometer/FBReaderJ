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

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
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

	List<Book> books(BookQuery query);
	boolean hasBooks(Filter filter);
	List<String> titles(BookQuery query);

	List<Book> recentlyOpenedBooks(int count);
	List<Book> recentlyAddedBooks(int count);
	Book getRecentBook(int index);
	void addToRecentlyOpened(Book book);
	void removeFromRecentlyOpened(Book book);

	Book getBookByFile(String path);
	Book getBookByFile(ZLFile file);
	Book getBookById(long id);
	Book getBookByUid(UID uid);
	Book getBookByHash(String hash);

	List<String> labels();
	List<Author> authors();
	boolean hasSeries();
	List<String> series();
	List<Tag> tags();
	List<String> firstTitleLetters();

	boolean saveBook(Book book);
	boolean canRemoveBook(Book book, boolean deleteFromDisk);
	void removeBook(Book book, boolean deleteFromDisk);

	String getHash(Book book, boolean force);
	void setHash(Book book, String hash);

	ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId);
	void storePosition(long bookId, ZLTextPosition position);

	boolean isHyperlinkVisited(Book book, String linkId);
	void markHyperlinkAsVisited(Book book, String linkId);

	ZLImage getCover(Book book, int maxWidth, int maxHeight);
	String getCoverUrl(Book book);
	String getDescription(Book book);

	List<Bookmark> bookmarks(BookmarkQuery query);
	void saveBookmark(Bookmark bookmark);
	void deleteBookmark(Bookmark bookmark);

	HighlightingStyle getHighlightingStyle(int styleId);
	List<HighlightingStyle> highlightingStyles();
	void saveHighlightingStyle(HighlightingStyle style);

	public class FormatDescriptor {
		public String Id;
		public String Name;
		public boolean IsActive;
	}
	List<FormatDescriptor> formats();
	// returns true iff active format set is changed
	boolean setActiveFormats(List<String> formatIds);

	void rescan(String path);
}
