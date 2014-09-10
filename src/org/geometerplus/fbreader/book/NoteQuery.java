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

public final class NoteQuery {
	public final Book Book;
	//public final boolean Visible;
	public final int Limit;
	public final int Page;

	public NoteQuery(int limit) {
		this(null, limit);
	}

	public NoteQuery(Book book, int limit) {
		this(book, limit, 0);
	}

	/*public NoteQuery(Book book, boolean visible, int limit) {
		this(book, visible, limit, 0);
	}*/

	NoteQuery(Book book, /*boolean visible,*/ int limit, int page) {
		Book = book;
		//Visible = visible;
		Limit = limit;
		Page = page;
	}

	public NoteQuery next() {
		return new NoteQuery(Book, /*Visible,*/ Limit, Page + 1);
	}
}
