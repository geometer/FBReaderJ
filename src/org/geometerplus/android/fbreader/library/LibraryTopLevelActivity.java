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

import java.util.Collections;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.RootTree;

import org.geometerplus.android.util.UIUtil;

public class LibraryTopLevelActivity extends LibraryBaseActivity {
	private RootTree mySearchResultsItem;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final ListAdapter adapter = new ListAdapter(this, Collections.<FBTree>emptyList());

		addTopLevelTree(Library.ROOT_FAVORITES, R.drawable.ic_list_library_favorites);
		addTopLevelTree(Library.ROOT_RECENT, R.drawable.ic_list_library_recent);
		addTopLevelTree(Library.ROOT_BY_AUTHOR, R.drawable.ic_list_library_authors);
		addTopLevelTree(Library.ROOT_BY_TITLE, R.drawable.ic_list_library_books);
		addTopLevelTree(Library.ROOT_BY_TAG, R.drawable.ic_list_library_tags);
		addTopLevelTree(Library.ROOT_FILE_TREE, R.drawable.ic_list_library_folder);

		onNewIntent(getIntent());
	}

	private void addTopLevelTree(String key, int imageId) {
		addFBTreeWithIcon(LibraryInstance.getRootTree(key), imageId);
	}

	@Override
	public void onDestroy() {
		LibraryInstance = null;
		super.onDestroy();
	}

	private void setSearchResults(Intent intent) {
		final ListAdapter adapter = getListAdapter();
		adapter.remove(mySearchResultsItem);
		mySearchResultsItem = LibraryInstance.getRootTree(Library.ROOT_SEARCH_RESULTS);
		adapter.add(0, mySearchResultsItem);
		getListView().invalidateViews();
		adapter.notifyDataSetChanged();
		new OpenTreeRunnable(mySearchResultsItem).run();
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
