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

package org.geometerplus.zlibrary.core.tree;

import java.util.*;

public abstract class ZLTree<T extends ZLTree> implements Iterable<T> {
	private int mySize = 1;
	public final T Parent;
	public final int Level;
	private ArrayList<T> mySubTrees;

	protected ZLTree(int level) {
		this(level, null, 0);
	}

	protected ZLTree() {
		this(null);
	}

	protected ZLTree(T parent) {
		this(parent, (parent == null) ? 0 : parent.subTrees().size());
	}

	protected ZLTree(T parent, int position) {
		this(0, parent, position);
	}

	private ZLTree(int nullLevel, T parent, int position) {
		if (parent != null && (position < 0 || position > parent.subTrees().size())) {
			throw new IndexOutOfBoundsException("`position` value equals " + position + " but must be in range [0; " + parent.subTrees().size() + "]");
		}
		Parent = parent;
		if (parent != null) {
			Level = parent.Level + 1;
			parent.addSubTree(this, position);
		} else {
			Level = nullLevel;
		}
	}

	public final int getSize() {
		return mySize;
	}

	public final boolean hasChildren() {
		return mySubTrees != null;
	}

	public final List<T> subTrees() {
		if (mySubTrees == null) {
			return Collections.emptyList();
		}
		return mySubTrees;
	}

	public final T getTreeByParagraphNumber(int index) {
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
				return (T)subtree.getTreeByParagraphNumber(index);
			}
		}
		throw new RuntimeException("That's impossible!!!");
	}

	private void addSubTree(T subtree, int position) {
		if (mySubTrees == null) {
			mySubTrees = new ArrayList<T>();
		}
		final int subTreeSize = subtree.getSize();
		final int thisSubTreesSize = mySubTrees.size();
		while (position < thisSubTreesSize) {
			subtree = mySubTrees.set(position++, subtree);
		}
		mySubTrees.add(subtree);
		for (ZLTree parent = this; parent != null; parent = parent.Parent) {
			parent.mySize += subTreeSize;
		}
	}

	public void removeSelf() {
		final int subTreeSize = getSize();
		ZLTree parent = Parent;
		if (parent != null) {
			parent.mySubTrees.remove(this);
			if (parent.mySubTrees.isEmpty()) {
				parent.mySubTrees = null;
			}
			for (; parent != null; parent = parent.Parent) {
				parent.mySize -= subTreeSize;
			}
		}
	}

	public final void clear() {
		final int subTreesSize = mySize - 1;
		mySubTrees = null;
		mySize = 1;
		if (subTreesSize > 0) {
			for (ZLTree parent = Parent; parent != null; parent = parent.Parent) {
				parent.mySize -= subTreesSize;
			}
		}
	}

	public final Iterator<T> iterator() {
		return new TreeIterator();
	}

	private class TreeIterator implements Iterator<T> {
		private T myCurrentElement = (T)ZLTree.this;
		private final LinkedList<Integer> myIndexStack = new LinkedList<Integer>();

		public boolean hasNext() {
			return myCurrentElement != null;
		}

		public T next() {
			final T element = myCurrentElement;
			if (element.hasChildren()) {
				myCurrentElement = (T)element.mySubTrees.get(0);
				myIndexStack.add(0);
			} else {
				ZLTree<T> parent = element;
				while (!myIndexStack.isEmpty()) {
					final int index = myIndexStack.removeLast() + 1;
					parent = parent.Parent;
					if (parent.mySubTrees.size() > index) {
						myCurrentElement = parent.mySubTrees.get(index);
						myIndexStack.add(index);
						break;
					}
				}
				if (myIndexStack.isEmpty()) {
					myCurrentElement = null;
				}
			}
			return element;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
