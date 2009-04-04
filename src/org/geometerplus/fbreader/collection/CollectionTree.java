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

package org.geometerplus.fbreader.collection;

import java.util.Collections;
import java.util.ArrayList;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.description.Author;

public abstract class CollectionTree extends ZLTree<CollectionTree> implements Comparable<CollectionTree> {
	protected CollectionTree() {
		super();
	}

	protected CollectionTree(CollectionTree parent) {
		super(parent);
	}

	TagTree createTagSubTree(String tag) {
		TagTree tree = new TagTree(this, tag);
		addSubTree(tree);
		return tree;
	}

	AuthorTree createAuthorSubTree(Author author) {
		AuthorTree tree = new AuthorTree(this, author);
		addSubTree(tree);
		return tree;
	}

	BookTree createBookSubTree(BookDescription book) {
		BookTree tree = new BookTree(this, book);
		addSubTree(tree);
		return tree;
	}

	public abstract String getName();

	protected String getSortKey() {
		return getName();
	}

	private String myChildrenString;

	public String getChildrenString() {
		if (myChildrenString == null) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (CollectionTree subtree : subTrees()) {
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

	public int compareTo(CollectionTree ct) {
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
		ArrayList<CollectionTree> children = subTrees();
		if (!children.isEmpty()) {
			Collections.sort(children);
			for (CollectionTree tree : children) {
				tree.sortAllChildren();
			}
		}
	}
}
