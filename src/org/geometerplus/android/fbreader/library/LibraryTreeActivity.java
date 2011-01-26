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

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BookTree;
import org.geometerplus.fbreader.tree.FBTree;

public class LibraryTreeActivity extends LibraryBaseActivity {
	private String myTreePathString;
	public static ViewType myViewType;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (DatabaseInstance == null || LibraryInstance == null) {
			finish();
			return;
		}
		
		myViewType = LibraryViewChangeDialog.getOprionViewType();
		if (myViewType == ViewType.SKETCH){
			// TODO
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
			tree = LibraryInstance.recentBooks();
		} else if (PATH_SEARCH_RESULTS.equals(path[0])) {
			tree = LibraryInstance.searchResults();
		} else if (PATH_BY_AUTHOR.equals(path[0])) {
			tree = LibraryInstance.byAuthor();
		} else if (PATH_BY_TITLE.equals(path[0])) {
			tree = LibraryInstance.byTitle();
		} else if (PATH_BY_TAG.equals(path[0])) {
			tree = LibraryInstance.byTag();
		} else if (PATH_FAVORITES.equals(path[0])) {
			tree = LibraryInstance.favorites();
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
			setSelection(adapter.getFirstSelectedItemIndex());
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		FBTree tree = ((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			showBookInfo(((BookTree)tree).Book);
		} else {
			new OpenTreeRunnable(LibraryInstance, myTreePathString + "\000" + tree.getName()).run();
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	FileUtil.addMenuItem(menu, 0, myResource, "view", R.drawable.ic_menu_sorting);	
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	    	case 0:
	    		new LibraryViewChangeDialog(this, mySelectedBookPath, myTreePathString).show();
	    		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }

    public static void launchActivity(Activity activity, String selectedBookPath, String treePath){
		Intent intent = new Intent(activity, LibraryTreeActivity.class)
			.putExtra(SELECTED_BOOK_PATH_KEY, selectedBookPath)
			.putExtra(TREE_PATH_KEY, treePath);
		activity.startActivityForResult(intent, CHILD_LIST_REQUEST);
    }
}
