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

import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.sort.TitledEntity;
import org.geometerplus.fbreader.tree.FBTree;

public abstract class TitledEntityTree extends LibraryTree {
	TitledEntityTree(IBookCollection collection) {
		super(collection);
	}

	TitledEntityTree(LibraryTree parent) {
		super(parent);
	}

	TitledEntityTree(LibraryTree parent, int position) {
		super(parent, position);
	}

	protected abstract TitledEntity getTitledEntity();

	@Override
	public int compareTo(FBTree tree) {
		if (tree instanceof TitledEntityTree) {
			return getTitledEntity().compareTo(((TitledEntityTree)tree).getTitledEntity());
		}
		return super.compareTo(tree);
	}
}
