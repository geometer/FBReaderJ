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

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

public final class Bookmark extends ZLTextFixedPosition {
	public final static int CREATION = 0;
	public final static int MODIFICATION = 1;
	public final static int ACCESS = 2;
	public final static int LATEST = 3;

	public static List<Bookmark> bookmarks() {
		return BooksDatabase.Instance().listAllBookmarks();
	}

	private long myId;
	private final long myBookId;
	private final String myBookTitle;
	private String myText;
	private final Date myCreationDate;
	private Date myModificationDate;
	private Date myAccessDate;
	private int myAccessCount;
	private Date myLatestDate;
	private final String myModelId;

	private boolean myIsChanged;

	Bookmark(long id, long bookId, String bookTitle, String text, Date creationDate, Date modificationDate, Date accessDate, int accessCount, String modelId, int paragraphIndex, int elementIndex, int charIndex) {
		super(paragraphIndex, elementIndex, charIndex);

		myId = id;
		myBookId = bookId;
		myBookTitle = bookTitle;
		myText = text;
		myCreationDate = creationDate;
		myModificationDate = modificationDate;
		myLatestDate = (modificationDate != null) ? modificationDate : creationDate;
		if (accessDate != null) {
			myAccessDate = accessDate;
			if (myLatestDate.compareTo(accessDate) < 0) {
				myLatestDate = accessDate;
			}
		}
		myAccessCount = accessCount;
		myModelId = modelId;
		myIsChanged = false;
	}

	public Bookmark(Book book, String text, String modelId, ZLTextWordCursor cursor) {
		super(cursor);

		myId = -1;
		myBookId = book.getId();
		myBookTitle = book.getTitle();
		myText = text;
		myCreationDate = new Date();
		myModelId = modelId;
		myIsChanged = true;
	}

	public long getId() {
		return myId;
	}

	public long getBookId() {
		return myBookId;
	}

	public String getText() {
		return myText;
	}

	public String getBookTitle() {
		return myBookTitle;
	}

	public String getModelId() {
		return myModelId;
	}

	public Date getTime(int timeStamp) {
		switch (timeStamp) {
			default:
			case CREATION:
				return myCreationDate;
			case MODIFICATION:
				return myModificationDate;
			case ACCESS:
				return myAccessDate;
			case LATEST:
				return myLatestDate;
		}
	}

	public int getAccessCount() {
		return myAccessCount;
	}

	public void setText(String text) {
		if (!text.equals(myText)) {
			myText = text;
			myModificationDate = new Date();
			myLatestDate = myModificationDate;
			myIsChanged = true;
		}
	}

	public void onOpen() {
		myAccessDate = new Date();
		++myAccessCount;
		myLatestDate = myAccessDate;
		myIsChanged = true;
	}

	public void save() {
		if (myIsChanged) {
			myId = BooksDatabase.Instance().saveBookmark(this);
			myIsChanged = false;
		}
	}

	public void delete() {
		if (myId != -1) {
			BooksDatabase.Instance().deleteBookmark(this);
		}
	}

	public static class ByTimeComparator implements Comparator<Bookmark> {
		public int compare(Bookmark bm0, Bookmark bm1) {
			return bm1.getTime(LATEST).compareTo(bm0.getTime(LATEST));
		}
	}
}
