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

package org.geometerplus.android.fbreader.network;

import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.SearchResult;


class SearchItemActions extends NetworkTreeActions {

	public static final int OPEN_RESULTS_ITEM_ID = 0;
	public static final int RUN_SEARCH_ITEM_ID = 2;


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
	public void buildContextMenu(NetworkBaseActivity activity, ContextMenu menu, NetworkTree tree) {
		final SearchItemTree searchTree = (SearchItemTree) tree;
		final SearchResult result = searchTree.getSearchResult();
		menu.setHeaderTitle(tree.getName());

		final boolean isLoading = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);

		if (!isLoading) {
			addMenuItem(menu, RUN_SEARCH_ITEM_ID, "search");
		}
		if (isLoading || tree.hasChildren()) {
			addMenuItem(menu, OPEN_RESULTS_ITEM_ID, "showResults");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
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
	public boolean prepareOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case OPEN_RESULTS_ITEM_ID:
				doExpandCatalog(activity, (SearchItemTree)tree);
				return true;
			case RUN_SEARCH_ITEM_ID:
				activity.onSearchRequested();
				return true;
		}
		return false;
	}


	public void doExpandCatalog(final NetworkBaseActivity activity, final SearchItemTree tree) {
		if (!NetworkView.Instance().isInitialized()) {
			return;
		}
		NetworkView.Instance().tryResumeLoading(activity, tree, NetworkSearchActivity.SEARCH_RUNNABLE_KEY, new Runnable() {
			public void run() {
				final boolean isLoading = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
				if (isLoading || tree.hasChildren()) {
					NetworkView.Instance().openTree(activity, tree, NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
				}
			}
		});
	}
}
