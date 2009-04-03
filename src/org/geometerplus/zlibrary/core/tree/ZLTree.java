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

package org.geometerplus.zlibrary.core.tree;

import java.util.ArrayList;

public abstract class ZLTree<T extends ZLTree> {
	private static final ArrayList ourEmptyList = new ArrayList();

	private int mySize = 1;
	private final T myParent;
	private final int myLevel;
	private ArrayList<T> mySubTrees;

	protected ZLTree() {
		myParent = null;
		myLevel = 0;
	}

	protected ZLTree(T parent) {
		myParent = parent;
		myLevel = parent.myLevel + 1;
	}

	public final T getParent() {
		return myParent;
	}

	public final int getLevel() {
		return myLevel;
	}

	public final int getSize() {
		return mySize;
	}

	public final boolean hasChildren() {
		return mySubTrees != null;
	}

	public final ArrayList<T> subTrees() {
		return (mySubTrees != null) ? mySubTrees : (ArrayList<T>)ourEmptyList;
	}

	public final T getTree(int index) {
		if ((index < 0) || (index >= mySize)) {
			// TODO: throw exception?
			return null;
		}
		if (index == 0) {
			return (T)this;
		}
		--index;
		for (T subtree : mySubTrees) {
			if (subtree.mySize <= index) {
				index -= subtree.mySize;
			} else {
				return (T)subtree.getTree(index);
			}
		}
		throw new RuntimeException("That's impossible!!!");
	}

	protected abstract T createChild();

	public final T createSubTree() {
		if (mySubTrees == null) {
			mySubTrees = new ArrayList<T>();
		}
		T tree = createChild();
		mySubTrees.add(tree);
		for (ZLTree parent = this; parent != null; parent = parent.myParent) {
			++parent.mySize;
		}
		return tree;
	}
}
