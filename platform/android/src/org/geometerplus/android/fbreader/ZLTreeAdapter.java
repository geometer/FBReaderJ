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

package org.geometerplus.android.fbreader;

import java.util.HashSet;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.tree.ZLTextTree;

import org.geometerplus.zlibrary.ui.android.R;

abstract class ZLTreeAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnKeyListener {
	private final ListView myParent;
	private final ZLTextTree myTree;
	private final ZLTreeItem[] myItems;
	private final HashSet<ZLTextTree> myOpenItems = new HashSet<ZLTextTree>();

	ZLTreeAdapter(ListView parent, ZLTextTree tree) {
		myParent = parent;
		myTree = tree;
		myItems = new ZLTreeItem[tree.getSize() - 1];
		myOpenItems.add(tree);

		parent.setAdapter(this);
		parent.setOnKeyListener(this);
		parent.setOnItemClickListener(this);
		parent.setOnItemLongClickListener(this);
	}

	private int getCount(ZLTextTree tree) {
		int count = 1;
		if (myOpenItems.contains(tree)) {
			for (ZLTextTree subtree : tree.subTrees()) {
				count += getCount(subtree);
			}
		}
		return count;
	}

	public final int getCount() {
		return getCount(myTree) - 1;
	}

	private final int indexByPosition(int position, ZLTextTree tree) {
		if (position == 0) {
			return 0;
		}
		--position;
		int index = 1;
		for (ZLTextTree subtree : tree.subTrees()) {
			int count = getCount(subtree);
			if (count <= position) {
				position -= count;
				index += subtree.getSize();
			} else {
				return index + indexByPosition(position, subtree);
			}
		}
		throw new RuntimeException("That's impossible!!!");
	}

	public final ZLTreeItem getItem(int position) {
		final int index = indexByPosition(position + 1, myTree) - 1;
		ZLTreeItem item = myItems[index];
		if (item == null) {
			item = new ZLTreeItem(myTree.getTree(index + 1));
			myItems[index] = item;
		}
		return item;
	}

	public final boolean areAllItemsEnabled() {
		return true;
	}

	public final boolean isEnabled(int position) {
		return true;
	}

	public final long getItemId(int position) {
		return indexByPosition(position + 1, myTree);
	}

	private void runTreeItem(ZLTreeItem item) {
		runTree(item.Tree);
	}

	protected boolean runTree(ZLTextTree tree) {
		if (tree.subTrees().isEmpty()) {
			return false;
		}
		if (myOpenItems.contains(tree)) {
			myOpenItems.remove(tree);
		} else {
			myOpenItems.add(tree);
		}
		myParent.invalidateViews();
		myParent.requestLayout();
		return true;
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runTreeItem((ZLTreeItem)getItem(position));
	}

	private boolean myContextMenuInProgress;

	public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (myContextMenuInProgress) {
			return false;
		}
		System.err.println("onItemLongClick");
		final ZLTextTree tree = ((ZLTreeItem)getItem(position)).Tree;
		if (!tree.subTrees().isEmpty()) {
			myContextMenuInProgress = true;
			view.showContextMenu();
			myContextMenuInProgress = false;
			return true;
		}
		return false;
	}

	public final boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					runTreeItem((ZLTreeItem)((ListView)view).getSelectedItem());
					return true;
				case KeyEvent.KEYCODE_BACK:
					return true;
			}
		}
		return false;
	}

	protected final void setIcon(ImageView imageView, ZLTextTree tree) {
		if (tree.subTrees().isEmpty()) {
			imageView.setImageResource(R.drawable.tree_icon_group_empty);
		} else {
			if (myOpenItems.contains(tree)) {
				imageView.setImageResource(R.drawable.tree_icon_group_open);
			} else {
				imageView.setImageResource(R.drawable.tree_icon_group_closed);
			}
		}
		imageView.setPadding(25 * (tree.getLevel() - 1), imageView.getPaddingTop(), 0, imageView.getPaddingBottom());
	}
}
