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

import java.util.*;

import org.geometerplus.fbreader.tree.FBTree;

public abstract class LibraryTree extends FBTree {
	protected LibraryTree() {
		super();
	}

	protected LibraryTree(LibraryTree parent) {
		super(parent);
	}

	protected LibraryTree(LibraryTree parent, int position) {
		super(parent, position);
	}

	public Book getBook() {
		return null;
	}

	public boolean containsBook(Book book) {
		return false;
	}

	public boolean isSelectable() {
		return true;
	}

	TagTree getTagSubTree(Tag tag) {
		final TagTree temp = new TagTree(tag);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (TagTree)subTrees().get(position);
		} else {
			return new TagTree(this, tag, - position - 1);
		}
	}

	TitleTree getTitleSubTree(String title) {
		final TitleTree temp = new TitleTree(title);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (TitleTree)subTrees().get(position);
		} else {
			return new TitleTree(this, title, - position - 1);
		}
	}

	AuthorTree getAuthorSubTree(Author author) {
		final AuthorTree temp = new AuthorTree(author);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (AuthorTree)subTrees().get(position);
		} else {
			return new AuthorTree(this, author, - position - 1);
		}
	}

	BookTree getBookSubTree(Book book, boolean showAuthors) {
		final BookTree temp = new BookTree(book, showAuthors);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (BookTree)subTrees().get(position);
		} else {
			return new BookTree(this, book, showAuthors, - position - 1);
		}
	}

	SeriesTree getSeriesSubTree(String series) {
		final SeriesTree temp = new SeriesTree(series);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return (SeriesTree)subTrees().get(position);
		} else {
			return new SeriesTree(this, series, - position - 1);
		}
	}

	public boolean removeBook(Book book, boolean recursively) {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();
		for (FBTree tree : this) {
			if (tree instanceof BookTree && ((BookTree)tree).Book.equals(book)) {
				toRemove.add(tree);
			}
		}
		for (FBTree tree : toRemove) {
			tree.removeSelf();
			FBTree parent = tree.Parent;
			if (recursively) {
				for (; parent != null && !parent.hasChildren(); parent = parent.Parent) {
					parent.removeSelf();
				}
			}
		}
		return !toRemove.isEmpty();
	}

	@Override
	public int compareTo(FBTree tree) {
		final int cmp = super.compareTo(tree);
		if (cmp == 0) {
			return getClass().getSimpleName().compareTo(tree.getClass().getSimpleName());
		}
		return cmp;
	}
}
