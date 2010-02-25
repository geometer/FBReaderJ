/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

//import org.geometerplus.zlibrary.core.tree.ZLTree;

import org.geometerplus.fbreader.tree.FBTree;

public abstract class LibraryTree extends FBTree {
	protected LibraryTree() {
		super();
	}

	protected LibraryTree(LibraryTree parent) {
		super(parent);
	}

	TagTree createTagSubTree(Tag tag) {
		return new TagTree(this, tag);
	}

	AuthorTree createAuthorSubTree(Author author) {
		return new AuthorTree(this, author);
	}

	BookTree createBookSubTree(Book book, boolean showAuthors) {
		return new BookTree(this, book, showAuthors);
	}

	public boolean removeBook(Book book) {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();
		for (FBTree tree : this) {
			if ((tree instanceof BookTree) && ((BookTree)tree).Book.equals(book)) {
				toRemove.add(tree);
			}
		}
		for (FBTree tree : toRemove) {
			tree.removeSelf();
			FBTree parent = tree.Parent;
			for (; (parent != null) && !parent.hasChildren(); parent = parent.Parent) {
				parent.removeSelf();
			}
			for (; parent != null; parent = parent.Parent) {
				((LibraryTree)parent).invalidateChildren();
			}
		}
		return !toRemove.isEmpty();
	}
}
