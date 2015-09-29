/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

public final class BookmarkQuery {
	public final AbstractBook Book;
	public final boolean Visible;
	public final int Limit;
	public final int Page;

	public BookmarkQuery(int limit) {
		this(null, limit);
	}

	public BookmarkQuery(AbstractBook book, int limit) {
		this(book, true, limit);
	}

	public BookmarkQuery(AbstractBook book, boolean visible, int limit) {
		this(book, visible, limit, 0);
	}

	BookmarkQuery(AbstractBook book, boolean visible, int limit, int page) {
		Book = book;
		Visible = visible;
		Limit = limit;
		Page = page;
	}

	public BookmarkQuery next() {
		return new BookmarkQuery(Book, Visible, Limit, Page + 1);
	}
}
