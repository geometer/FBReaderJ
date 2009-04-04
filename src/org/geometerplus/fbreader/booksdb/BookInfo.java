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

package org.geometerplus.fbreader.booksdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class BookInfo {
	private static boolean equals(Object o0, Object o1) {
		if (o0 == null) {
			return o1 == null;
		}
		return o0.equals(o1);
	}

	private final SQLiteDatabase myDatabase = BooksDatabase.Instance().Database;

	private String myBookId;

	public final String FileName;

	private String myTitle;
	private String myAuthorName;
	private String myAuthorSortKey;
	private String myLanguage;
	private String myEncoding;
	private String mySeries;
	private int myNumberInSeries;

	private boolean myIsSaved;
	private boolean myIsChanged;

	public BookInfo(String fileName) {
		FileName = fileName;
		// TODO: implement
		myTitle = "";
		myLanguage = "";
		myEncoding = "";
		myAuthorName = "";
		myAuthorSortKey = "";
		myIsSaved = false;
	}

	public boolean isSaved() {
		return myIsSaved;
	}

	public void save() {
		if (myIsChanged) {
			// TODO: implement
			myIsChanged = false;
			//myIsSaved = true;
		}
	}

	public void reset() {
		final String whereClause = "book_id = ?";
		final String[] parameters = new String[] { myBookId };
		myDatabase.delete("Books", whereClause, parameters);
		myDatabase.delete("BookAuthor", whereClause, parameters);
		myDatabase.delete("BookSeries", whereClause, parameters);
		myDatabase.delete("BookTag", whereClause, parameters);
		myTitle = "";
		myLanguage = "";
		myEncoding = "";
		myAuthorName = "";
		myAuthorSortKey = "";
		mySeries = null;
		myNumberInSeries = 0;
		myIsChanged = false;
		myIsSaved = false;
	}

	public String getTitle() {
		return myTitle;
	}

	public void setTitle(String title) {
		if (!myTitle.equals(title)) {
			myTitle = title;
			myIsChanged = true;
		}
	}

	public String getAuthorName() {
		return myAuthorName;
	}

	public void setAuthorName(String authorName) {
		if (!myAuthorName.equals(authorName)) {
			myAuthorName = authorName;
			myIsChanged = true;
		}
	}

	public String getAuthorSortKey() {
		return myAuthorSortKey;
	}

	public void setAuthorSortKey(String authorSortKey) {
		if (!myAuthorSortKey.equals(authorSortKey)) {
			myAuthorSortKey = authorSortKey;
			myIsChanged = true;
		}
	}

	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!myLanguage.equals(language)) {
			myLanguage = language;
			myIsChanged = true;
		}
	}

	public String getEncoding() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!myEncoding.equals(encoding)) {
			myEncoding = encoding;
			myIsChanged = true;
		}
	}

	public String getSeries() {
		return mySeries;
	}

	public void setSeries(String series) {
		if (!equals(mySeries, series)) {
			mySeries = series;
			myIsChanged = true;
		}
	}

	public int getNumberInSeries() {
		return myNumberInSeries;
	}

	public void setNumberInSeries(int numberInSeries) {
		if (myNumberInSeries != numberInSeries) {
			myNumberInSeries = numberInSeries;
			myIsChanged = true;
		}
	}
}
