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

import org.geometerplus.android.fbreader.SQLiteBooksDatabase;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class GalleryLibraryTopLevelActivity extends GalleryLibraryBaseActivity {

	private LinkedList<FBTree> myItems;
	private TopLevelTree mySearchResultsItem;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		BaseActivity.DatabaseInstance = SQLiteBooksDatabase.Instance();
		if (BaseActivity.DatabaseInstance == null) {
			BaseActivity.DatabaseInstance = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (BaseActivity.LibraryInstance == null) {
			BaseActivity.LibraryInstance = new Library();
			startService(new Intent(getApplicationContext(), InitializationService.class));
		}

		myItems = new LinkedList<FBTree>();
		myItems.add(new TopLevelTree(
			myResource.getResource(PATH_FAVORITES),
			R.drawable.ic_list_library_favorites,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, new StartTreeActivityRunnable(PATH_FAVORITES, null) {
				public void run() {
					if (BaseActivity.LibraryInstance.favorites().hasChildren()) {
						super.run();
					} else {
						UIUtil.showErrorMessage(GalleryLibraryTopLevelActivity.this, "noFavorites");
					}
				}
			})
		));
		myItems.add(new TopLevelTree(
			myResource.getResource(PATH_RECENT),
			R.drawable.ic_list_library_recent,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, PATH_RECENT)
		));
		myItems.add(new TopLevelTree(
			myResource.getResource(PATH_BY_AUTHOR),
			R.drawable.ic_list_library_authors,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, PATH_BY_AUTHOR)
		));
		myItems.add(new TopLevelTree(
			myResource.getResource(PATH_BY_TITLE),
			R.drawable.ic_list_library_books,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, PATH_BY_TITLE)
		));
		myItems.add(new TopLevelTree(
			myResource.getResource(PATH_BY_TAG),
			R.drawable.ic_list_library_tags,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, PATH_BY_TAG)
		));
		myItems.add(new TopLevelTree(
			myResource.getResource("fileTree"),
			R.drawable.ic_list_library_folder,
			new Runnable() {
				public void run() {
					startActivity(
						new Intent(GalleryLibraryTopLevelActivity.this, FileManager.class)
							.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					);
				}
			}
		));
		// setListAdapter(new LibraryAdapter(myItems)); 		// TODO

		onNewIntent(getIntent());
	}

	@Override
	public void onDestroy() {
		BaseActivity.LibraryInstance = null;
		super.onDestroy();
	}

	// TODO
//	@Override
//	public void onListItemClick(ListView listView, View view, int position, long rowId) {
//		//TopLevelTree tree = (TopLevelTree)((LibraryAdapter)getListAdapter()).getItem(position);	// TODO
//		//tree.run();																				// TODO
//	}

	private void setSearchResults(Intent intent) {
		if (myItems.get(0) == mySearchResultsItem) {
			myItems.remove(0);
		}
		final String pattern = intent.getStringExtra(SearchManager.QUERY);
		mySearchResultsItem = new TopLevelTree(
			myResource.getResource(PATH_SEARCH_RESULTS),
			pattern,
			R.drawable.ic_list_library_books,
			new OpenTreeRunnable(BaseActivity.LibraryInstance, PATH_SEARCH_RESULTS, pattern)
		);
		myItems.add(0, mySearchResultsItem);
//		getListView().invalidateViews();			// TODO
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
