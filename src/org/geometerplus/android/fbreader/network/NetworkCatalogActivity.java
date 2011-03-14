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

import java.util.ArrayList;

import android.os.Bundle;
import android.view.*;
import android.widget.BaseAdapter;
import android.content.Intent;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.tree.FBTree;

public class NetworkCatalogActivity extends NetworkBaseActivity implements UserRegistrationConstants {
	private NetworkTree myTree;
	private volatile boolean myInProgress;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		final NetworkView networkView = NetworkView.Instance();
		if (!networkView.isInitialized()) {
			finish();
			return;
		}

		myTree = Util.getTreeFromIntent(getIntent());

		if (myTree == null) {
			finish();
			return;
		}

		networkView.setOpenedActivity(myTree.getUniqueKey(), this);

		setListAdapter(new CatalogAdapter());
		getListView().invalidateViews();
		setupTitle();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (menuInfo == null && myTree instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)myTree).Item.Link;
			if (Util.isAccountRefillingSupported(this, link)) {
				final RefillAccountActions actions = NetworkView.Instance().getTopUpActions();
				if (actions != null) {
					actions.buildContextMenu(this, menu, link);
					return;
				}
			}
		}
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if ((item == null || item.getMenuInfo() == null) && myTree instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)myTree).Item.Link;
			if (Util.isAccountRefillingSupported(this, link)) {
				final RefillAccountActions actions = NetworkView.Instance().getTopUpActions();
				if (actions != null && actions.runAction(this, link, item.getItemId())) {
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case USER_REGISTRATION_REQUEST_CODE:
				if (myTree instanceof NetworkCatalogTree &&
					resultCode == RESULT_OK &&
					data != null) {
					try {
						Util.runAfterRegistration(
							((NetworkCatalogTree)myTree).Item.Link.authenticationManager(),
							data
						);
					} catch (ZLNetworkException e) {
						// TODO: show an error message
					}
				}
				break;
		}
	}

	private final void setupTitle() {
		String title = null;
		final NetworkView networkView = NetworkView.Instance();
		if (networkView.isInitialized()) {
			final NetworkTreeActions actions = networkView.getActions(myTree);
			if (actions != null) {
				title = actions.getTreeTitle(myTree);
			}
		}
		if (title == null) {
			title = myTree.getName();
		}
		setTitle(title);
		setProgressBarIndeterminateVisibility(myInProgress);
	}

	@Override
	public void onDestroy() {
		if (myTree != null && NetworkView.Instance().isInitialized()) {
			NetworkView.Instance().setOpenedActivity(myTree.getUniqueKey(), null);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private final class CatalogAdapter extends BaseAdapter {
		public final int getCount() {
			return myTree.subTrees().size();
		}

		public final NetworkTree getItem(int position) {
			if (position < 0 || position >= myTree.subTrees().size()) {
				return null;
			}
			return (NetworkTree)myTree.subTrees().get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final NetworkTree tree = getItem(position);
			return setupNetworkTreeItemView(convertView, parent, tree);
		}

		void onModelChanged() {
			notifyDataSetChanged();
			for (FBTree child : myTree.subTrees()) {
				if (child instanceof TopUpTree) {
					child.invalidateChildren();
				}
			}
		}
	}

	private static NetworkTree.Key getLoadableNetworkTreeKey(NetworkTree tree) {
		if ((tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree)
				&& tree.Parent instanceof NetworkTree) {
			return getLoadableNetworkTreeKey((NetworkTree)tree.Parent);
		}
		return tree.getUniqueKey();
	}

	@Override
	public void onModelChanged() {
		final NetworkView networkView = NetworkView.Instance();
		final NetworkTree.Key key = getLoadableNetworkTreeKey(myTree);
		myInProgress = key != null && networkView.isInitialized() && networkView.containsItemsLoadingRunnable(key);
		getListView().invalidateViews();

		/*
		 * getListAdapter() always returns CatalogAdapter because onModelChanged() 
		 * can be called only after Activity's onStart() method (where NetworkView's 
		 * addEventListener() is called). Therefore CatalogAdapter will be set as 
		 * adapter in onCreate() method before any calls to onModelChanged().
		 */
		((CatalogAdapter) getListAdapter()).onModelChanged();

		setupTitle();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doStopLoading();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doStopLoading() {
		if (NetworkView.Instance().isInitialized()) {
			final ItemsLoadingRunnable runnable =
				NetworkView.Instance().getItemsLoadingRunnable(myTree.getUniqueKey());
			if (runnable != null) {
				runnable.interruptLoading();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return NetworkView.Instance().createOptionsMenu(menu, myTree);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return NetworkView.Instance().prepareOptionsMenu(this, menu, myTree);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (NetworkView.Instance().runOptionsMenu(this, item, myTree)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
