/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.tree.ZLTree;

public abstract class LibraryTree extends ZLTree<LibraryTree> implements Comparable<LibraryTree> {
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

	public abstract String getName();

	protected String getSortKey() {
		return getName();
	}

	private String myChildrenString;
	public String getSecondString() {
		if (myChildrenString == null) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (LibraryTree subtree : subTrees()) {
				if (count++ > 0) {
					builder.append(",  ");
				}
				builder.append(subtree.getName());
				if (count == 5) {
					break;
				}
			}
			myChildrenString = builder.toString();
		}
		return myChildrenString;
	}

	public int compareTo(LibraryTree ct) {
		final String key0 = getSortKey();
		final String key1 = ct.getSortKey();
		if (key0 == null) {
			return (key1 == null) ? 0 : -1;
		}
		if (key1 == null) {
			return 1;
		}
		return key0.toLowerCase().compareTo(key1.toLowerCase());
	}

	public final void sortAllChildren() {
		List<LibraryTree> children = subTrees();
		if (!children.isEmpty()) {
			Collections.sort(children);
			for (LibraryTree tree : children) {
				tree.sortAllChildren();
			}
		}
	}

	public boolean removeBook(Book book) {
		final LinkedList<LibraryTree> toRemove = new LinkedList<LibraryTree>();
		for (LibraryTree tree : this) {
			if ((tree instanceof BookTree) && ((BookTree)tree).Book.equals(book)) {
				toRemove.add(tree);
			}
		}
		for (LibraryTree tree : toRemove) {
			tree.removeSelf();
			LibraryTree parent = tree.Parent;
			for (; (parent != null) && !parent.hasChildren(); parent = parent.Parent) {
				parent.removeSelf();
			}
			for (; parent != null; parent = parent.Parent) {
				parent.myChildrenString = null;
			}
		}
		return !toRemove.isEmpty();
	}
}
