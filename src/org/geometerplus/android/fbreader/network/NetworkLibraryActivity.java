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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.ZLTreeAdapter;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener {
	static NetworkLibraryActivity Instance;

	private final ZLResource myResource = ZLResource.resource("networkView");

	private final ArrayList<NetworkTreeActions> myActions = new ArrayList<NetworkTreeActions>();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		LibraryAdapter adapter = new LibraryAdapter(getListView(), NetworkLibrary.Instance().getTree());

		myActions.add(new NetworkBookActions(this));
		myActions.add(new NetworkCatalogActions(this, adapter));
		myActions.trimToSize();
	}

	private NetworkTreeActions chooseActions(ZLTree tree) {
		NetworkTree networkTree = (NetworkTree) tree;
		for (NetworkTreeActions actions: myActions) {
			if (actions.canHandleTree(networkTree)) {
				return actions;
			}
		}
		return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		Instance = this;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		Instance = null;
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private final class LibraryAdapter extends ZLTreeAdapter {

		LibraryAdapter(ListView view, NetworkTree tree) {
			super(view, tree);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree) getItem(position);

			/*if (tree instanceof NetworkCatalogTree || tree instanceof NetworkBookTree) {
				menu.add(0, DBG_PRINT_ENTRY_ITEM_ID, 0, "dbg - Dump Entry");
			}*/

			final NetworkTreeActions actions = chooseActions(tree);
			if (actions != null) {
				actions.buildContextMenu(menu, tree);
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

			final NetworkTree tree = (NetworkTree)getItem(position);

			final ImageView iconView = (ImageView)view.findViewById(R.id.network_tree_item_icon);

			setIcon(iconView, tree);

			((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());
			return view;
		}

		@Override
		protected boolean runTreeItem(ZLTree tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			final NetworkTree networkTree = (NetworkTree) tree;
			final NetworkTreeActions actions = chooseActions(networkTree);
			if (actions == null) {
				return false;
			}
			final int actionCode = actions.getDefaultActionCode(networkTree);
			final String confirm = actions.getConfirmText(networkTree, actionCode);
			if (actionCode < 0) {
				return false;
			}
			if (confirm != null) {
				final ZLResource resource = myResource.getResource("confirmQuestions");
				final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
				new AlertDialog.Builder(NetworkLibraryActivity.this)
					.setTitle(networkTree.getName())
					.setMessage(confirm)
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							actions.runAction(networkTree, actionCode);
						}
					})
					.setNegativeButton(buttonResource.getResource("no").getValue(), null)
					.create().show();
			} else {
				actions.runAction(networkTree, actionCode);
			}
			return true;
		}
	}

	//private static final int DBG_PRINT_ENTRY_ITEM_ID = 32000;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final LibraryAdapter adapter = (LibraryAdapter) getListView().getAdapter();
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree) adapter.getItem(position);

		/*if (actionCode == DBG_PRINT_ENTRY_ITEM_ID) {
			String msg = null;
			if (tree instanceof NetworkCatalogTree) {
				msg = ((NetworkCatalogTree) tree).Item.dbgEntry.toString();
			} else if (tree instanceof NetworkBookTree) {
				msg = ((NetworkBookTree) tree).Book.dbgEntry.toString();
			}
			new AlertDialog.Builder(this).setTitle("dbg entry").setMessage(msg).setIcon(0).setPositiveButton("ok", null).create().show();
			return true;
		}*/

		final NetworkTreeActions actions = chooseActions(tree);
		if (actions != null &&
				actions.runAction(tree, item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void openInBrowser(String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
}
