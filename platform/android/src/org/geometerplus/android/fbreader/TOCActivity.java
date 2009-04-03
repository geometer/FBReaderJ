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

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Context;
import android.app.ListActivity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.tree.ZLStringTree;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.bookmodel.ContentsTree;
import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.BookTextView;

public class TOCActivity extends ListActivity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final FBReader fbreader = (FBReader)ZLApplication.Instance();
		ZLTreeAdapter adapter = new TOCAdapter(this, fbreader.Model.ContentsTree);
		/*
		int selectedIndex = adapter.getSelectedIndex();
		if (selectedIndex >= 0) {
			view.setSelection(selectedIndex);
		}
		*/
	}

	private final class TOCAdapter extends ZLTreeAdapter {
		private final ContentsTree myContentsTree;

		TOCAdapter(TOCActivity activity, ContentsTree tree) {
			super(activity.getListView(), tree);
			myContentsTree = tree;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final ZLStringTree tree = (ZLStringTree)getItem(position);
			setIcon((ImageView)view.findViewById(R.id.toc_tree_item_icon), tree);
			((TextView)view.findViewById(R.id.toc_tree_item_text)).setText(tree.getText());
			return view;
		}

		protected boolean runTreeItem(ZLTree tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			final ContentsTree.Reference reference = myContentsTree.getReference((ZLStringTree)tree);
			final FBReader fbreader = (FBReader)ZLApplication.Instance();
			fbreader.BookTextView.gotoParagraphSafe(reference.Model, reference.ParagraphIndex);
			finish();
			return true;
		}
	}
}
