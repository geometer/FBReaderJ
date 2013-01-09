/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

class BookCollection {
	private final Map<ZLFile,Book> myBooks =
		Collections.synchronizedMap(new HashMap<ZLFile,Book>());

	public boolean addBook(Book book) {
		if (myBooks.containsKey(book.File)) {
			return false;
		}
		myBooks.put(book.File, book);
		return true;
	}

	public void removeBook(Book book) {
		myBooks.remove(book.File);
	}

	public List<Book> books() {
		synchronized (myBooks) {
			return new ArrayList<Book>(myBooks.values());
		}
	}
}
