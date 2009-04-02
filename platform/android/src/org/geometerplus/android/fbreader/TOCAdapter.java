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
import org.geometerplus.fbreader.bookmodel.ContentsTree;

import org.geometerplus.zlibrary.ui.android.R;

final class TOCAdapter extends ZLTreeAdapter {
	private final ContentsTree myContentsTree;

	TOCAdapter(ListView parent, ContentsTree tree) {
		super(parent, tree);
		myContentsTree = tree;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ? convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
		final ZLTextTree tree = getItem(position).Tree;
		setIcon((ImageView)view.findViewById(R.id.toc_tree_item_icon), tree);
		((TextView)view.findViewById(R.id.toc_tree_item_text)).setText(tree.getText());
		return view;
	}

	protected boolean runTree(ZLTextTree tree) {
		if (super.runTree(tree)) {
			return true;
		}
		final ContentsTree.Reference reference = myContentsTree.getReference(tree);
		return true;
	}
}
