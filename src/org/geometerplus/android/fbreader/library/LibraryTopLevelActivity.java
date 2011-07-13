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

package org.geometerplus.android.fbreader.library;

import java.util.LinkedList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.RootTree;

import org.geometerplus.android.util.UIUtil;

public class LibraryTopLevelActivity extends LibraryBaseActivity {
	private TopLevelTree mySearchResultsItem;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final ListAdapter adapter = new ListAdapter(this, new LinkedList<FBTree>());

		final RootTree rootFavorites = LibraryInstance.getRootTree(Library.ROOT_FAVORITES);
		adapter.add(new TopLevelTree(
			rootFavorites,
			R.drawable.ic_list_library_favorites,
			new OpenTreeRunnable(
				LibraryInstance,
				new StartTreeActivityRunnable(rootFavorites.getUniqueKey()) {
					public void run() {
						if (LibraryInstance.favorites().hasChildren()) {
							super.run();
						} else {
							UIUtil.showErrorMessage(LibraryTopLevelActivity.this, "noFavorites");
						}
					}
				}
			)
		));
		addTopLevelTree(Library.ROOT_RECENT, R.drawable.ic_list_library_recent);
		addTopLevelTree(Library.ROOT_BY_AUTHOR, R.drawable.ic_list_library_authors);
		addTopLevelTree(Library.ROOT_BY_TITLE, R.drawable.ic_list_library_books);
		addTopLevelTree(Library.ROOT_BY_TAG, R.drawable.ic_list_library_tags);
		adapter.add(new TopLevelTree(
			LibraryInstance.getRootTree(Library.ROOT_FILE_TREE),
			R.drawable.ic_list_library_folder,
			new Runnable() {
				public void run() {
					startActivity(
						new Intent(LibraryTopLevelActivity.this, FileManager.class)
							.putExtra(TREE_KEY_KEY, FBTree.Key.createRootKey(Library.ROOT_FILE_TREE))
							.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					);
				}
			}
		));

		onNewIntent(getIntent());
	}

	private void addTopLevelTree(String key, int imageId) {
		final RootTree root = LibraryInstance.getRootTree(key);
		getListAdapter().add(new TopLevelTree(
			root,
			imageId,
			new OpenTreeRunnable(LibraryInstance, root.getUniqueKey())
		));
	}

	@Override
	public void onDestroy() {
		LibraryInstance = null;
		super.onDestroy();
	}

	private void setSearchResults(Intent intent) {
		final ListAdapter adapter = getListAdapter();
		adapter.remove(mySearchResultsItem);
		final RootTree searchRoot = LibraryInstance.getRootTree(Library.ROOT_SEARCH_RESULTS);
		mySearchResultsItem = new TopLevelTree(
			searchRoot,
			R.drawable.ic_list_library_books,
			new OpenTreeRunnable(LibraryInstance, searchRoot.getUniqueKey())
		);
		adapter.add(0, mySearchResultsItem);
		getListView().invalidateViews();
		adapter.notifyDataSetChanged();
		mySearchResultsItem.run();
	}

	public void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (runSearch(intent)) {
				setSearchResults(intent);
			} else {
				showNotFoundToast();
			}
		} else if (ACTION_FOUND.equals(intent.getAction())) {
			setSearchResults(intent);
		}
	}
}
