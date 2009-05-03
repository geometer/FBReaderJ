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

import android.app.TabActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.library.*;

public class LibraryTabActivity extends TabActivity implements MenuItem.OnMenuItemClickListener {
	static LibraryTabActivity Instance;

	final ZLStringOption mySelectedTabOption = new ZLStringOption("TabActivity", "SelectedTab", "");
	private final ZLResource myResource = ZLResource.resource("libraryView");

	private ListView createTab(String tag, int id) {
		final TabHost host = getTabHost();
		final String label = myResource.getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label).setContent(id));
		return (ListView)findViewById(id);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.library, host.getTabContentView(), true);

		new LibraryAdapter(createTab("byAuthor", R.id.by_author), Library.Instance().byAuthor());
		new LibraryAdapter(createTab("byTag", R.id.by_tag), Library.Instance().byTag());
		new LibraryAdapter(createTab("recent", R.id.recent), Library.Instance().recentBooks());
		findViewById(R.id.search_results).setVisibility(View.GONE);

		host.setCurrentTabByTag(mySelectedTabOption.getValue());
	}

	private LibraryAdapter mySearchResultsAdapter;
	void showSearchResultsTab(LibraryTree tree) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter =
				new LibraryAdapter(createTab("searchResults", R.id.search_results), tree);
		} else {
			mySearchResultsAdapter.resetTree(tree);
		}
		getTabHost().setCurrentTabByTag("searchResults");
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
		mySelectedTabOption.setValue(getTabHost().getCurrentTabTag());
		Instance = null;
		Library.Instance().clear();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, 1, "localSearch");
		addMenuItem(menu, 2, "networkSearch").setEnabled(false);
		return true;
	}

	private MenuItem addMenuItem(Menu menu, int index, String resourceKey) {
		final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, index, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		return item;
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			default:
				return true;
		}
	}

	@Override
	public boolean onSearchRequested() {
		final FBReader fbreader = (FBReader)FBReader.Instance();
		startSearch(fbreader.BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	private final class LibraryAdapter extends ZLTreeAdapter {
		private final LibraryTree myLibraryTree;

		LibraryAdapter(ListView view, LibraryTree tree) {
			super(view, tree);
			myLibraryTree = tree;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
			final LibraryTree tree = (LibraryTree)getItem(position);
			setIcon((ImageView)view.findViewById(R.id.library_tree_item_icon), tree);
			((TextView)view.findViewById(R.id.library_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(tree.getSecondString());
			return view;
		}

		protected boolean runTreeItem(ZLTree tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			finish();
			final FBReader fbreader = (FBReader)FBReader.Instance();
			fbreader.openBook(((BookTree)tree).Book, null);
			return true;
		}
	}
}
