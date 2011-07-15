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

package org.geometerplus.android.fbreader.network;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.NetworkTree;

import org.geometerplus.android.fbreader.tree.BaseActivity;

abstract class NetworkBaseActivity extends BaseActivity implements NetworkView.EventListener {
	protected static final int BASIC_AUTHENTICATION_CODE = 1;
	protected static final int CUSTOM_AUTHENTICATION_CODE = 2;
	protected static final int SIGNUP_CODE = 3;

	public BookDownloaderServiceConnection Connection;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		OLD_STYLE_FLAG = true;

		SQLiteCookieDatabase.init(this);

		Connection = new BookDownloaderServiceConnection();
		bindService(
			new Intent(getApplicationContext(), BookDownloaderService.class),
			Connection,
			BIND_AUTO_CREATE
		);
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * Set listener in onStart() to give descendants initialize itself in
		 * onCreate methods before onModelChanged() will be called.
		 */
		NetworkView.Instance().addEventListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnCreateContextMenuListener(this);
		onModelChanged(); // do the same update actions as upon onModelChanged
	}

	@Override
	protected void onStop() {
		NetworkView.Instance().removeEventListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (Connection != null) {
			unbindService(Connection);
			Connection = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		return false;
	}

	// method from NetworkView.EventListener
	public void onModelChanged() {
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (menuInfo != null) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
			if (tree != null) {
				final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
				if (actions != null) {
					actions.buildContextMenu(this, menu, tree);
					return;
				}
			}
		}
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item != null && item.getMenuInfo() != null) {
			final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
			final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
			if (tree != null) {
				final NetworkTreeActions actions = NetworkView.Instance().getActions(tree);
				if (actions != null && actions.runAction(this, tree, item.getItemId())) {
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		final NetworkTree networkTree = (NetworkTree)getListAdapter().getItem(position);
		final NetworkView networkView = NetworkView.Instance();
		final NetworkTreeActions actions = networkView.getActions(networkTree);
		if (actions == null) {
			return;
		}
		final int actionCode = actions.getDefaultActionCode(this, networkTree);
		if (actionCode == NetworkTreeActions.TREE_SHOW_CONTEXT_MENU) {
			listView.showContextMenuForChild(view);
			return;
		}
		if (actionCode < 0) {
			return;
		}
		actions.runAction(this, networkTree, actionCode);
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}
}
