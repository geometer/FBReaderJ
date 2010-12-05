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

package org.geometerplus.android.fbreader.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.BookTree;

import org.geometerplus.android.fbreader.FBReader;

public class LibraryTreeActivity extends LibraryBaseActivity {
	static final String TREE_PATH_KEY = "TreePath";

	static final String PATH_FAVORITES = "favorites";
	static final String PATH_SEARCH_RESULTS = "searchResults";
	static final String PATH_RECENT = "recent";
	static final String PATH_BY_AUTHOR = "author";
	static final String PATH_BY_TAG = "tag";

	private String myTreePathString;
	private String mySelectedBookPath;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// TODO: implement
			finish();
		} else {
			myTreePathString = getIntent().getStringExtra(TREE_PATH_KEY);
			mySelectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
        
			final String[] path = myTreePathString.split("\000");
        
			FBTree tree = null;
			if (PATH_RECENT.equals(path[0])) {
				tree = Library.recentBooks();
			} else if (PATH_SEARCH_RESULTS.equals(path[0])) {
				tree = Library.searchResults();
			} else if (PATH_BY_AUTHOR.equals(path[0])) {
				tree = Library.byAuthor();
			} else if (PATH_BY_TAG.equals(path[0])) {
				tree = Library.byTag();
			} else if (PATH_FAVORITES.equals(path[0])) {
			}
        
			for (int i = 1; i < path.length; ++i) {
				if (tree == null) {
					break;
				}
				tree = tree.getSubTreeByName(path[i]);
			}
        
			if (tree != null) {
				setListAdapter(new LibraryAdapter(tree.subTrees()));
			}
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		FBTree tree = ((LibraryAdapter)getListAdapter()).getItem(position);
		if (tree instanceof BookTree) {
			startActivity(
				new Intent(getApplicationContext(), FBReader.class)
					.setAction(Intent.ACTION_VIEW)
					.putExtra(FBReader.BOOK_PATH_KEY, ((BookTree)tree).Book.File.getPath())
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
			);
		} else {
			new OpenTreeRunnable(
				myTreePathString + "\000" + tree.getName(),
				mySelectedBookPath
			).run();
		}
	}
}
