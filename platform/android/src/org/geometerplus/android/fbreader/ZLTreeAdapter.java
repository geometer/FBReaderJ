/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

final class ZLTreeAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnKeyListener {
	private final ListView myParent;
	private final ZLTextTree myTree;
	private final ZLTreeItem[] myItems;
	private final HashSet<ZLTextTree> myOpenItems = new HashSet<ZLTextTree>();

	ZLTreeAdapter(ListView parent, ZLTextTree tree) {
		myParent = parent;
		myTree = tree;
		myItems = new ZLTreeItem[tree.getSize() - 1];
		myOpenItems.add(tree);
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

	public int getCount() {
		return getCount(myTree) - 1;
	}

	private int indexByPosition(int position, ZLTextTree tree) {
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

	public ZLTreeItem getItem(int position) {
		final int index = indexByPosition(position + 1, myTree) - 1;
		ZLTreeItem item = myItems[index];
		if (item == null) {
			item = new ZLTreeItem(myTree.getTree(index + 1));
			myItems[index] = item;
		}
		return item;
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public long getItemId(int position) {
		return indexByPosition(position + 1, myTree);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(myParent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
		final ZLTextTree tree = getItem(position).Tree;
		ImageView image = (ImageView)view.findViewById(R.id.toc_tree_item_icon);
		if (tree.subTrees().isEmpty()) {
			image.setImageResource(R.drawable.tree_icon_group_empty);
		} else {
			if (myOpenItems.contains(tree)) {
				image.setImageResource(R.drawable.tree_icon_group_open);
			} else {
				image.setImageResource(R.drawable.tree_icon_group_closed);
			}
		}
		image.setPadding(25 * (tree.getLevel() - 1), image.getPaddingTop(), 0, image.getPaddingBottom());
		TextView text = (TextView)view.findViewById(R.id.toc_tree_item_text);
		text.setText(tree.getText());
		return view;
	}

	private boolean runTreeItem(ZLTreeItem item) {
		return runTree(item.Tree);
	}

	private boolean runTree(ZLTextTree tree) {
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

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runTreeItem((ZLTreeItem)getItem(position));
	}

	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
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
}
