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

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.os.Message;
import android.os.Handler;
import android.view.ContextMenu;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.SearchResultTree;

import org.geometerplus.android.fbreader.ZLTreeAdapter;


class NetworkSearchActions extends NetworkTreeActions {

	public static final int EXPAND_OR_COLLAPSE_TREE_ITEM_ID = 0;
	public static final int STOP_LOADING_ITEM_ID = 1;


	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof SearchResultTree;
	}

	@Override
	public void buildContextMenu(ContextMenu menu, NetworkTree tree) {
		final SearchResultTree searchTree = (SearchResultTree) tree;
		//final SearchResult result = searchTree.Result;
		menu.setHeaderTitle(tree.getName());

		final boolean isOpened = tree.hasChildren() && NetworkLibraryActivity.Instance.getAdapter().isOpen(tree);

		final ItemsLoadingRunnable searchRunnable = NetworkLibraryActivity.Instance.getItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		final boolean isLoading = searchRunnable != null;

		if (isLoading) {
			if (searchRunnable.InterruptFlag.get()) {
				addMenuItem(menu, TREE_NO_ACTION, "stoppingNetworkSearch");
			} else {
				addMenuItem(menu, STOP_LOADING_ITEM_ID, "stopSearching");
			}
		} else {
			final String expandOrCollapseTitle = isOpened ? "hideResults" : "showResults";
			addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID, expandOrCollapseTitle);
		}
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
		return EXPAND_OR_COLLAPSE_TREE_ITEM_ID;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		return null;
	}

	@Override
	public boolean runAction(NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case EXPAND_OR_COLLAPSE_TREE_ITEM_ID:
				doExpandCatalog((SearchResultTree)tree);
				return true;
			case STOP_LOADING_ITEM_ID:
				doStopLoading((SearchResultTree)tree);
				return true;
		}
		return false;
	}


	public void doExpandCatalog(final SearchResultTree tree) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		NetworkLibraryActivity.Instance.getAdapter().expandOrCollapseTree(tree);
	}

	private void doStopLoading(SearchResultTree tree) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		final ItemsLoadingRunnable runnable = NetworkLibraryActivity.Instance.getItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		if (runnable != null) {
			runnable.InterruptFlag.set(true);
		}
	}
}
