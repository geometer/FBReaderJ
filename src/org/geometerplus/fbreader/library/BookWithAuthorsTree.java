/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public class BookWithAuthorsTree extends BookTree {
	BookWithAuthorsTree(IBookCollection collection, PluginCollection pluginCollection, Book book) {
		super(collection, pluginCollection, book);
	}

	BookWithAuthorsTree(LibraryTree parent, Book book) {
		super(parent, book);
	}

	BookWithAuthorsTree(LibraryTree parent, Book book, int position) {
		super(parent, book, position);
	}

	@Override
	public String getSummary() {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Author author : Book.authors()) {
			if (count++ > 0) {
				builder.append(",  ");
			}
			builder.append(author.DisplayName);
			if (count == 5) {
				break;
			}
		}
		return builder.toString();
	}
}
