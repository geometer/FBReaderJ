/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.io.Serializable;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

public abstract class FBTree extends ZLTree<FBTree> implements Comparable<FBTree> {
	public static class Key implements Serializable {
		private static final long serialVersionUID = -6500763093522202052L;

		public final Key Parent;
		public final String Id;

		private Key(Key parent, String id) {
			if (id == null) {
				throw new IllegalArgumentException("FBTree.Key string id must be non-null");
			}
			Parent = parent;
			Id = id;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof Key)) {
				return false;
			}
			final Key key = (Key)other;
			return Id.equals(key.Id) && ZLMiscUtil.equals(Parent, key.Parent);
		}

		@Override
		public int hashCode() {
			return Id.hashCode();
		}

		@Override
		public String toString() {
			return Parent == null ? Id : Parent.toString() + " :: " + Id;
		}
	}

	public static enum Status {
		READY_TO_OPEN,
		WAIT_FOR_OPEN,
		ALWAYS_RELOAD_BEFORE_OPENING,
		CANNOT_OPEN
	};

	private ZLImage myCover;
	private boolean myCoverRequested;
	private Key myKey;

	protected FBTree() {
		super();
	}

	protected FBTree(FBTree parent) {
		super(parent);
	}

	protected FBTree(FBTree parent, int position) {
		super(parent, position);
	}

	public final Key getUniqueKey() {
		if (myKey == null) {
			myKey = new Key(Parent != null ? Parent.getUniqueKey() : null, getStringId());
		}
		return myKey;
	}

	/**
	 * Returns id used as a part of unique key above. This string must be not null
     * and be different for all children of same tree
	 */
	protected abstract String getStringId();

	public FBTree getSubTree(String id) {
		for (FBTree tree : subTrees()) {
			if (id.equals(tree.getStringId())) {
				return tree;
			}
		}
		return null;
	}

	public int indexOf(FBTree tree) {
		return subTrees().indexOf(tree);
	}

	public abstract String getName();

	public String getTreeTitle() {
		return getName();
	}

	protected String getSortKey() {
		final String sortKey = getName();
		if (sortKey == null ||
			sortKey.length() <= 1 ||
			Character.isLetterOrDigit(sortKey.charAt(0))) {
			return sortKey;
		}

		for (int i = 1; i < sortKey.length(); ++i) {
			if (Character.isLetterOrDigit(sortKey.charAt(i))) {
				return sortKey.substring(i);
			}
		}
		return sortKey;
	}

	public int compareTo(FBTree tree) {
		final String key0 = getSortKey();
		final String key1 = tree.getSortKey();
		if (key0 == null) {
			return (key1 == null) ? 0 : -1;
		}
		if (key1 == null) {
			return 1;
		}
		return key0.toLowerCase().compareTo(key1.toLowerCase());
	}

	public String getSummary() {
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

	protected void setCover(ZLImage cover) {
		myCoverRequested = true;
		myCover = cover;
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

	public Status getOpeningStatus() {
		return Status.READY_TO_OPEN;
	}

	public String getOpeningStatusMessage() {
		return null;
	}

	public void waitForOpening() {
	}
}
