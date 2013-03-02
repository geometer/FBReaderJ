/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.Collections;
import java.util.List;

import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.sort.Title;

import android.widget.Toast;

public final class SeriesTree extends LibraryTree {
	public final Title Series;
	public final Author Author;

	SeriesTree(IBookCollection collection, String series, Author author) {
		super(collection);
		String language = "en";
		Series = new Title(series, language);
		Author = author;
	}

	SeriesTree(LibraryTree parent, String series, Author author, int position) {
		super(parent, position);
		String language = "en";
		Series = new Title(series, language);
		Author = author;
	}

	@Override
	public String getName() {
		return Series.getTitle();
	}

	@Override
	public String getSummary() {
		if (Author != null) {
			return MiscUtil.join(Collection.titlesForSeriesAndAuthor(Series.getTitle(), Author, 5), ", ");
		} else {
			return MiscUtil.join(Collection.titlesForSeries(Series.getTitle(), 5), ", ");
		}
	}

	@Override
	protected String getStringId() {
		return "@SeriesTree " + getName();
	}

	@Override
	public boolean containsBook(Book book) {
		if (book == null) {
			return false;
		}
		final SeriesInfo info = book.getSeriesInfo();
		return info != null && Series.getTitle().equals(info.Title);
	}

	@Override
	protected String getSortKey() {
		return " Series:" + super.getSortKey();
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		if (Author != null) {
			for (Book book : Collection.booksForSeriesAndAuthor(Series.getTitle(), Author)) {
				createBookInSeriesSubTree(book);
			}
		} else {
			for (Book book : Collection.booksForSeries(Series.getTitle())) {
				createBookInSeriesSubTree(book);
			}
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return containsBook(book) && createBookInSeriesSubTree(book);
			case Updated:
			{
				boolean changed = removeBook(book);
				changed |= containsBook(book) && createBookInSeriesSubTree(book);
				return changed;
			}
			case Removed:
			default:
				return super.onBookEvent(event, book);
		}
	}

	boolean createBookInSeriesSubTree(Book book) {
		final BookInSeriesTree temp = new BookInSeriesTree(Collection, book);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new BookInSeriesTree(this, book, - position - 1);
			return true;
		}
	}

	@Override
	public Title getTitle() {
		// TODO Auto-generated method stub
		//System.err.println(Series.getSortKey() + Series.getLanguage());
		return Series;
	}
}