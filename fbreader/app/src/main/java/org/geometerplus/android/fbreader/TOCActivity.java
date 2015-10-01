/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.android.util.ViewUtil;

public class TOCActivity extends ListActivity {
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		final TOCTree root = fbreader.Model.TOCTree;
		myAdapter = new TOCAdapter(root);
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();
		myAdapter.selectItem(treeToSelect);
		mySelectedItem = treeToSelect;
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final TOCTree tree = (TOCTree)myAdapter.getItem(position);
		switch (item.getItemId()) {
			case PROCESS_TREE_ITEM_ID:
				myAdapter.runTreeItem(tree);
				return true;
			case READ_BOOK_ITEM_ID:
				myAdapter.openBookText(tree);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private final class TOCAdapter extends ZLTreeAdapter {
		TOCAdapter(TOCTree root) {
			super(getListView(), root);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final TOCTree tree = (TOCTree)getItem(position);
			if (tree.hasChildren()) {
				menu.setHeaderTitle(tree.getText());
				final ZLResource resource = ZLResource.resource("tocView");
				menu.add(0, PROCESS_TREE_ITEM_ID, 0, resource.getResource(isOpen(tree) ? "collapseTree" : "expandTree").getValue());
				menu.add(0, READ_BOOK_ITEM_ID, 0, resource.getResource("readText").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree)getItem(position);
			view.setBackgroundColor(tree == mySelectedItem ? 0xff808080 : 0);
			setIcon(ViewUtil.findImageView(view, R.id.toc_tree_item_icon), tree);
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
			return view;
		}

		void openBookText(TOCTree tree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				finish();
				final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView();
				fbreader.storePosition();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree)tree);
			return true;
		}
	}
}
