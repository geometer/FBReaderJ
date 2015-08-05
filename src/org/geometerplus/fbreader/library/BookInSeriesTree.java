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

import java.math.BigDecimal;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tree.FBTree;

public final class BookInSeriesTree extends BookTree {
	BookInSeriesTree(IBookCollection collection, PluginCollection pluginCollection, Book book) {
		super(collection, pluginCollection, book);
	}

	BookInSeriesTree(LibraryTree parent, Book book, int position) {
		super(parent, book, position);
	}

	@Override
	public int compareTo(FBTree tree) {
		if (tree instanceof BookInSeriesTree) {
			final BigDecimal index0 = Book.getSeriesInfo().Index;
			final BigDecimal index1 = ((BookTree)tree).Book.getSeriesInfo().Index;
			final int cmp;
			if (index0 == null) {
				cmp = index1 == null ? 0 : 1;
			} else {
				cmp = index1 == null ? -1 : index0.compareTo(index1);
			}
			if (cmp != 0) {
				return cmp;
			}
		}
		return super.compareTo(tree);
	}
}
