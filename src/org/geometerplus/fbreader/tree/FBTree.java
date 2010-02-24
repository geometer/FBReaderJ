/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.tree;

import java.util.*;

import org.geometerplus.zlibrary.core.tree.ZLTree;

public abstract class FBTree extends ZLTree<FBTree> implements Comparable<FBTree> {
	protected FBTree() {
		super();
	}

	protected FBTree(FBTree parent) {
		super(parent);
	}

	public abstract String getName();

	protected String getSortKey() {
		return getName();
	}

	public int compareTo(FBTree ct) {
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
		List<FBTree> children = subTrees();
		if (!children.isEmpty()) {
			Collections.sort(children);
			for (FBTree tree : children) {
				tree.sortAllChildren();
			}
		}
	}
}
