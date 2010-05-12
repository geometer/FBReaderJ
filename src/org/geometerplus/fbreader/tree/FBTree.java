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
import org.geometerplus.zlibrary.core.image.ZLImage;

public abstract class FBTree extends ZLTree<FBTree> implements Comparable<FBTree> {

	private ZLImage myCover;
	private boolean myCoverRequested;

	protected FBTree(int level) {
		super(level);
	}

	protected FBTree() {
		super();
	}

	protected FBTree(FBTree parent) {
		super(parent);
	}

	protected FBTree(FBTree parent, int position) {
		super(parent, position);
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

	private String mySecondString;

	public final void invalidateChildren() {
		mySecondString = null;
	}

	public final String getSecondString() {
		if (mySecondString == null) {
			mySecondString = getSummary();
			if (mySecondString == null) {
				mySecondString = "";
			}
		}
		return mySecondString;
	}

	protected String getSummary() {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (FBTree subtree : subTrees()) {
			if (count++ > 0) {
				builder.append(",  ");
			}
			builder.append(subtree.getName());
			if (count == 5) {
				break;
			}
		}
		return builder.toString();
	}

	protected ZLImage createCover() {
		return null;
	}

	public final ZLImage getCover() {
		if (!myCoverRequested) {
			myCover = createCover();
			if (myCover == null && Parent != null) {
				myCover = Parent.getCover();
			}
			myCoverRequested = true;
		}
		return myCover;
	}
}
