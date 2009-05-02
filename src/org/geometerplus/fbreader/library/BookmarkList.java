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

import java.util.LinkedList;

import org.geometerplus.zlibrary.text.view.impl.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.impl.ZLTextViewImpl;

public class BookmarkList extends LinkedList<Bookmark> {
	private final Book myBook;
	private boolean myIsChanged;

	public BookmarkList(Book book) {
		myBook = book;
		BooksDatabase.Instance().listBookmarks(book.getId(), this);
	}

	public void addNewBookmark(String text, ZLTextPosition position) {
		add(0, new Bookmark(text, position));
		myIsChanged = true;
	}

	public void removeBookmark(int index) {
		remove(index);
		myIsChanged = true;
	}

	public void setBookmarkText(int index, String text) {
		if (get(index).setText(text)) {
			myIsChanged = true;
		}
	}

	public void gotoBookmark(int index, ZLTextViewImpl view) {
		myIsChanged = true;
		final Bookmark bookmark = get(index);
		bookmark.onAccess();
		view.gotoPosition(bookmark.getPosition());
	}

	public void save() {
		if (!myIsChanged) {
			return;
		}
		BooksDatabase.Instance().saveBookmarks(myBook.getId(), this);
		myIsChanged = false;
	}
}
