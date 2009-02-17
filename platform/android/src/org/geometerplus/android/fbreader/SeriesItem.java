/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import android.widget.ListView;

import org.geometerplus.fbreader.description.Author;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.fbreader.FBReader;

class SeriesItem implements LibraryListItem {
	private final ListView myView;
	private final Author myAuthor;
	private final String mySeries;
	private String myBookList = "";

	SeriesItem(ListView view, Author author, String series) {
		myView = view;
		myAuthor = author;
		mySeries = series;
	}

	void addBook(BookDescription book) {
		if (myBookList.length() > 0) {
			myBookList += ",  ";
		}
		myBookList += book.getTitle();
	}

	public String getTopText() {
		return mySeries;
	}

	public String getBottomText() {
		return myBookList;
	}

	public void run() {
		LibraryTabUtil.setSeriesBookList(myView, myAuthor, mySeries);
		myView.invalidate();
	}
}
