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

import android.os.Bundle;
import android.view.*;
import android.content.Intent;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.tree.FBTree;

public class NetworkCatalogActivity extends NetworkBaseActivity implements UserRegistrationConstants {
	private static final String ACTIVITY_BY_TREE_KEY = "ActivityByTree";

	static void setForTree(NetworkTree tree, NetworkCatalogActivity activity) {
		if (tree != null) {
			tree.setUserData(ACTIVITY_BY_TREE_KEY, activity);
		}
	}

	static NetworkCatalogActivity getByTree(NetworkTree tree) {
		return (NetworkCatalogActivity)tree.getUserData(ACTIVITY_BY_TREE_KEY);
	}

	private volatile boolean myInProgress;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setForTree((NetworkTree)getCurrentTree(), this);

		setProgressBarIndeterminateVisibility(myInProgress);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if ((item == null || item.getMenuInfo() == null) && getCurrentTree() instanceof NetworkCatalogTree) {
			final INetworkLink link = ((NetworkCatalogTree)getCurrentTree()).Item.Link;
			if (Util.isTopupSupported(this, link)) {
				final TopupActions actions = NetworkView.Instance().getTopupActions();
				if (actions != null && TopupActions.runAction(this, link, item.getItemId())) {
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onDestroy() {
		setForTree((NetworkTree)getCurrentTree(), null);
		super.onDestroy();
	}

	private static NetworkTree getLoadableNetworkTree(NetworkTree tree) {
		while (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			if (tree.Parent instanceof NetworkTree) {
				tree = (NetworkTree)tree.Parent;
			} else {
				return null;
			}
		}
		return tree;
	}

	@Override
	public void onModelChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				final NetworkTree tree = getLoadableNetworkTree((NetworkTree)getCurrentTree());
				myInProgress =
					tree != null &&
					ItemsLoadingService.getRunnable(tree) != null;
				getListView().invalidateViews();
            
				/*
				 * getListAdapter() always returns CatalogAdapter because onModelChanged() 
				 * can be called only after Activity's onStart() method (where NetworkView's 
				 * addEventListener() is called). Therefore CatalogAdapter will be set as 
				 * adapter in onCreate() method before any calls to onModelChanged().
				 */
				((NetworkLibraryAdapter)getListAdapter()).replaceAll(getCurrentTree().subTrees());
				for (FBTree child : getCurrentTree().subTrees()) {
					if (child instanceof TopUpTree) {
						child.invalidateChildren();
					}
				}
            
				setProgressBarIndeterminateVisibility(myInProgress);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return NetworkView.Instance().createOptionsMenu(menu, (NetworkTree)getCurrentTree());
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return NetworkView.Instance().prepareOptionsMenu(this, menu, (NetworkTree)getCurrentTree());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (NetworkView.Instance().runOptionsMenu(this, item, (NetworkTree)getCurrentTree())) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
