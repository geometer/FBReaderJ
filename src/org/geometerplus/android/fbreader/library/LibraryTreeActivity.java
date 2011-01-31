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

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BookTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class LibraryTreeActivity extends LibraryBaseActivity {
	private String myTreePathString;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (LibraryCommon.DatabaseInstance == null || LibraryCommon.LibraryInstance == null) {
			finish();
			return;
		}
		
		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			if (runSearch(intent)) {
				startActivity(intent
					.setAction(ACTION_FOUND)
					.setClass(getApplicationContext(), LibraryTopLevelActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
				);
			} else {
				showNotFoundToast();
				finish();
			}
			return;
		}

		myTreePathString = intent.getStringExtra(TREE_PATH_KEY);
        
		final String[] path = myTreePathString.split("\000");
        
		String title = null;
		if (path.length == 1) {
			title = myResource.getResource(path[0]).getResource("summary").getValue();
			final String parameter = intent.getStringExtra(PARAMETER_KEY);
			if (parameter != null) {
				title = title.replace("%s", parameter);
			}
		} else {
			title = path[path.length - 1];
		}
		setTitle(title);

		FBTree tree = null;
		if (PATH_RECENT.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.recentBooks();
		} else if (PATH_SEARCH_RESULTS.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.searchResults();
		} else if (PATH_BY_AUTHOR.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.byAuthor();
		} else if (PATH_BY_TITLE.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.byTitle();
		} else if (PATH_BY_TAG.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.byTag();
		} else if (PATH_FAVORITES.equals(path[0])) {
			tree = LibraryCommon.LibraryInstance.favorites();
		}
        
		for (int i = 1; i < path.length; ++i) {
			if (tree == null) {
				break;
			}
			tree = tree.getSubTreeByName(path[i]);
		}

		mySelectedBook = null;
		if (mySelectedBookPath != null) {
			final ZLFile file = ZLFile.createFileByPath(mySelectedBookPath);
			if (file != null) {
				mySelectedBook = Book.getByFile(file);
			}
		}
        
		if (tree != null) {
			final LibraryAdapter adapter = new LibraryAdapter(tree.subTrees());
			setListAdapter(adapter);
			getListView().setOnCreateContextMenuListener(adapter);
			System.err.println("SELECTED: " + adapter.getFirstSelectedItemIndex());
			setSelection(adapter.getFirstSelectedItemIndex());		// TODO
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
			GalleryLibraryTreeActivity.launchActivity(this, mySelectedBookPath, myTreePathString);
			finish();
			return;
		}
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		FBTree tree = ((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			showBookInfo(((BookTree)tree).Book);
		} else {
			new OpenTreeRunnable(LibraryCommon.LibraryInstance, myTreePathString + "\000" + tree.getName()).run();
		}
	}
	
    public static void launchActivity(Activity activity, String selectedBookPath, String treePath){
		Intent intent = new Intent(activity, LibraryTreeActivity.class)
			.putExtra(SELECTED_BOOK_PATH_KEY, selectedBookPath)
			.putExtra(TREE_PATH_KEY, treePath);
		activity.startActivityForResult(intent, CHILD_LIST_REQUEST);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	    	case 0:
				Log.v(FMCommon.LOG, "onOptionsItemSelected(MenuItem item) - LibraryTreeActivity");
	    		new LibraryTreeChanger(this, mySelectedBookPath, myTreePathString).show();
	    		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
}
