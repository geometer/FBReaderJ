/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

public final class SeriesTree extends LibraryTree {
	public final String Series;

	SeriesTree(String series) {
		Series = series;
	}

	SeriesTree(LibraryTree parent, String series, int position) {
		super(parent, position);
		Series = series;
	}

	@Override
	public String getName() {
		return Series;
	}

	@Override
	protected String getStringId() {
		return "@SeriesTree " + getName();
	}

	BookTree getBookInSeriesSubTree(Book book) {
		final BookInSeriesTree temp = new BookInSeriesTree(book);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (BookInSeriesTree)subTrees().get(position);
		} else {
			return new BookInSeriesTree(this, book, - position - 1);
		}
	}

	@Override
	public boolean containsBook(Book book) {
		if (book == null) {
			return false;
		}
		final SeriesInfo info = book.getSeriesInfo();
		return info != null && Series.equals(info.Name);
	}
}
