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
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.ZLTreeAdapter;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener {
	static NetworkLibraryActivity Instance;

	private final ZLResource myResource = ZLResource.resource("networkView");

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		new LibraryAdapter(getListView(), NetworkLibrary.Instance().getTree());
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
			/*final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final LibraryTree tree = (LibraryTree)getItem(position);
			if (tree instanceof BookTree) {
				menu.setHeaderTitle(tree.getName());
				final ZLResource resource = ZLResource.resource("libraryView");
				menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
				if (Library.Instance().canDeleteBook(((BookTree)tree).Book)) {
					menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
				}
			}*/
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
			if (tree instanceof NetworkCatalogTree) {
				NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
				if (!catalogTree.hasChildren()) {
					updateCatalogChildren(catalogTree);
					resetTree();
				}
				super.runTreeItem(tree);
				return true;
			} else if (tree instanceof NetworkBookTree) {
				NetworkBookTree bookTree = (NetworkBookTree) tree;
				NetworkBookItem book = bookTree.Book;
				// TODO: handle book item
			}
			return false;
		}
	}


	private void updateCatalogChildren(NetworkCatalogTree tree) {
		tree.clear();

		ArrayList<NetworkLibraryItem> children = new ArrayList<NetworkLibraryItem>();

		LoadSubCatalogRunnable loader = new LoadSubCatalogRunnable(tree.Item, children);
		loader.executeWithUI();
		if (loader.hasErrors()) {
			loader.showErrorMessage(this);
		} else if (children.isEmpty()) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			final ZLResource buttonResource = dialogResource.getResource("button");
			final ZLResource boxResource = dialogResource.getResource("emptyCatalogBox");
			new AlertDialog.Builder(this)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(boxResource.getResource("message").getValue())
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
				.create().show();
		}

		boolean hasSubcatalogs = false;
		for (NetworkLibraryItem child: children) {
			if (child instanceof NetworkCatalogItem) {
				hasSubcatalogs = true;
				break;
			}
		}

		if (hasSubcatalogs) {
			for (NetworkLibraryItem child: children) {
				NetworkTreeFactory.createNetworkTree(tree, child);
			}
		} else {
			NetworkTreeFactory.fillAuthorNode(tree, children);
		}
		//NetworkLibrary.invalidateAccountDependents();
		//NetworkLibrary.synchronize();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
}
