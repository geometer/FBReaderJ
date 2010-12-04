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

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.Library;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

public class LibraryTopLevelActivity extends LibraryBaseActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "LIBRARY_NG");
		}
		if (Library == null) {
			Library = new Library();
			startService(new Intent(getApplicationContext(), InitializationService.class));
		}

		final ArrayList<FBTree> items = new ArrayList<FBTree>();
		items.add(new TopLevelTree(
			myResource.getResource("searchResults"),
			R.drawable.ic_list_library_books,
			new Runnable() {
				public void run() {
				}
			}
		));
		items.add(new TopLevelTree(
			myResource.getResource("favorites"),
			R.drawable.ic_list_library_favorites,
			new OpenTreeRunnable(LibraryTreeActivity.PATH_FAVORITES, mySelectedBookPath)
		));
		items.add(new TopLevelTree(
			myResource.getResource("recent"),
			R.drawable.ic_list_library_recent,
			new OpenTreeRunnable(LibraryTreeActivity.PATH_RECENT, mySelectedBookPath)
		));
		items.add(new TopLevelTree(
			myResource.getResource("byAuthor"),
			R.drawable.ic_list_library_authors,
			new OpenTreeRunnable(LibraryTreeActivity.PATH_BY_AUTHOR, mySelectedBookPath)
		));
		items.add(new TopLevelTree(
			myResource.getResource("byTag"),
			R.drawable.ic_list_library_tags,
			new OpenTreeRunnable(LibraryTreeActivity.PATH_BY_TAG, mySelectedBookPath)
		));
		items.add(new TopLevelTree(
			myResource.getResource("fileTree"),
			R.drawable.ic_list_library_folder,
			new Runnable() {
				public void run() {
				}
			}
		));
		setListAdapter(new LibraryAdapter(items));
	}

	@Override
	public void onDestroy() {
		Library = null;
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		TopLevelTree tree = (TopLevelTree)((LibraryAdapter)getListAdapter()).getItem(position);
		tree.getAction().run();
	}
}

class TopLevelTree extends FBTree implements ZLAndroidTree {
	private final ZLResource myResource;
	private final int myCoverResourceId;
	private final Runnable myAction;

	public TopLevelTree(ZLResource resource, int coverResourceId, Runnable action) {
		myResource = resource;
		myCoverResourceId = coverResourceId;
		myAction = action;
	}

	@Override
	public String getName() {
		return myResource.getValue();
	}

	@Override
	public String getSummary() {
		return myResource.getResource("summary").getValue();
	}

	public int getCoverResourceId() {
		return myCoverResourceId;
	}

	public Runnable getAction() {
		return myAction;
	}
}
