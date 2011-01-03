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

import android.app.Activity;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.SearchResult;


class SearchItemActions extends NetworkTreeActions {

	public static final int RUN_SEARCH_ITEM_ID = 0;


	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof SearchItemTree;
	}

	@Override
	public String getTreeTitle(NetworkTree tree) {
		final SearchResult result = ((SearchItemTree) tree).getSearchResult();
		if (result != null) {
			return result.Summary;
		}
		return tree.getName();
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
		menu.setHeaderTitle(tree.getName());

		final boolean isLoading = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);

		if (!isLoading) {
			addMenuItem(menu, RUN_SEARCH_ITEM_ID, "search");
		} else {
			addMenuItem(menu, TREE_NO_ACTION, "stoppingNetworkSearch");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		final boolean isLoading = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		if (!isLoading) {
			return RUN_SEARCH_ITEM_ID;
		}
		return TREE_NO_ACTION;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		return null;
	}

	@Override
	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case RUN_SEARCH_ITEM_ID:
				activity.onSearchRequested();
				return true;
		}
		return false;
	}
}
