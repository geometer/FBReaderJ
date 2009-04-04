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

import org.geometerplus.zlibrary.core.tree.ZLTree;

import org.geometerplus.zlibrary.ui.android.R;

abstract class ZLTreeAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener, View.OnKeyListener {
	private final ListView myParent;
	private final ZLTree myTree;
	private final ZLTree[] myItems;
	private final HashSet<ZLTree> myOpenItems = new HashSet<ZLTree>();

	/*
	private static int fillTreeArray(ZLTree<?> tree, ZLTree[] array, int offset) {
		int pos = offset;
		for (ZLTree subtree : tree.subTrees()) {
			array[pos++] = subtree;
			if (subtree.hasChildren()) {
				pos += fillTreeArray(subtree, array, pos);
			}
		}
		return pos - offset;
	}
	*/

	ZLTreeAdapter(ListView parent, ZLTree tree) {
		myParent = parent;
		myTree = tree;
		myItems = new ZLTree[tree.getSize() - 1];
		//fillTreeArray(tree, myItems, 0);
		myOpenItems.add(tree);

		parent.setAdapter(this);
		parent.setOnKeyListener(this);
		parent.setOnItemClickListener(this);
		parent.setOnCreateContextMenuListener(this);
	}

	private int getCount(ZLTree<?> tree) {
		int count = 1;
		if (myOpenItems.contains(tree)) {
			for (ZLTree subtree : tree.subTrees()) {
				count += getCount(subtree);
			}
		}
		return count;
	}

	public final int getCount() {
		return getCount(myTree) - 1;
	}

	private final int indexByPosition(int position, ZLTree<?> tree) {
		if (position == 0) {
			return 0;
		}
		--position;
		int index = 1;
		for (ZLTree subtree : tree.subTrees()) {
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

	public final ZLTree getItem(int position) {
		final int index = indexByPosition(position + 1, myTree) - 1;
		ZLTree item = myItems[index];
		if (item == null) {
			item = myTree.getTree(index + 1);
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

	protected boolean runTreeItem(ZLTree tree) {
		if (!tree.hasChildren()) {
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

	public final void onItemClick(AdapterView parent, View view, int position, long id) {
		runTreeItem(getItem(position));
	}

	private boolean myContextMenuInProgress;

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		System.err.println("onCreateContextMenu");
		menu.add("Item 0");
		menu.add("Item 1");
		menu.add("Item 2");
	}

	public final boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		switch (keyEvent.getAction()) {
			case KeyEvent.ACTION_UP:
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						runTreeItem((ZLTree)((ListView)view).getSelectedItem());
						return true;
					case KeyEvent.KEYCODE_BACK:
						return true;
				}
				break;
			case KeyEvent.ACTION_DOWN:
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						return true;
				}
				break;
		}
		return false;
	}

	protected final void setIcon(ImageView imageView, ZLTree tree) {
		if (tree.hasChildren()) {
			if (myOpenItems.contains(tree)) {
				imageView.setImageResource(R.drawable.tree_icon_group_open);
			} else {
				imageView.setImageResource(R.drawable.tree_icon_group_closed);
			}
		} else {
			imageView.setImageResource(R.drawable.tree_icon_group_empty);
		}
		imageView.setPadding(25 * (tree.getLevel() - 1), imageView.getPaddingTop(), 0, imageView.getPaddingBottom());
	}
}
