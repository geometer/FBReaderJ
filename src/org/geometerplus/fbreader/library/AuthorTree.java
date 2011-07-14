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

public class AuthorTree extends LibraryTree {
	public final Author Author;

	AuthorTree(LibraryTree parent, Author author) {
		super(parent);
		Author = author;
	}

	SeriesTree createSeriesSubTree(String series) {
		return new SeriesTree(this, series);
	}

	@Override
	public String getName() {
		return
			(Author != null) ?
				Author.DisplayName :
				Library.resource().getResource("unknownAuthor").getValue();
	}

	@Override
	protected String getStringId() {
		return getName();
	}

	protected String getSortKey() {
		return (Author != null) ? Author.SortKey : null;
	}

	@Override
	public boolean containsBook(Book book) {
		return book != null && book.authors().contains(Author);
	}
}
