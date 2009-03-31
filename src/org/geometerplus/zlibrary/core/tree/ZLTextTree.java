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

public class ZLTextTree {
	private static final ArrayList<ZLTextTree> ourEmptyList = new ArrayList<ZLTextTree>();

	private int mySize = 1;
	private String myText;
	private final ZLTextTree myParent;
	private final int myLevel;
	private ArrayList<ZLTextTree> mySubTrees;

	protected ZLTextTree() {
		myParent = null;
		myLevel = 0;
	}

	private ZLTextTree(ZLTextTree parent) {
		myParent = parent;
		myLevel = parent.myLevel + 1;
	}

	public final ZLTextTree getParent() {
		return myParent;
	}

	public final int getLevel() {
		return myLevel;
	}

	public final int getSize() {
		return mySize;
	}

	public final String getText() {
		return myText;
	}

	public final ArrayList<ZLTextTree> subTrees() {
		return (mySubTrees != null) ? mySubTrees : ourEmptyList;
	}

	public final ZLTextTree getTree(int index) {
		if ((index < 0) || (index >= mySize)) {
			// TODO: throw exception?
			return null;
		}
		if (index == 0) {
			return this;
		}
		--index;
		for (ZLTextTree subtree : mySubTrees) {
			if (subtree.mySize <= index) {
				index -= subtree.mySize;
			} else {
				return subtree.getTree(index);
			}
		}
		return null;
	}

	public final void setText(String text) {
		myText = text;
	}

	public final ZLTextTree createSubTree() {
		if (mySubTrees == null) {
			mySubTrees = new ArrayList<ZLTextTree>();
		}
		ZLTextTree tree = new ZLTextTree(this);
		mySubTrees.add(tree);
		for (ZLTextTree parent = this; parent != null; parent = parent.myParent) {
			++parent.mySize;
		}
		return tree;
	}
}
