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

import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;


public class NetworkSearchActivity extends Activity {

	public static final String SEARCH_RUNNABLE_KEY = "org.geometerplus.android.fbreader.network.NetworkSearchActivity";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		if (!NetworkView.Instance().isInitialized()) {
			finish();
		}

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			runSearch(pattern);
		}
		finish();
	}

	private class SearchHandler extends ItemsLoadingHandler {

		private final SearchItemTree myTree;

		public SearchHandler(SearchItemTree tree) {
			myTree = tree;
		}

		@Override
		public void onUpdateItems(List<NetworkLibraryItem> items) {
			SearchResult result = myTree.getSearchResult();
			for (NetworkLibraryItem item: items) {
				if (item instanceof NetworkBookItem) {
					result.addBook((NetworkBookItem) item);
				}
			}
		}

		@Override
		public void afterUpdateItems() {
			myTree.updateSubTrees();
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChangedAsync();
			}
		}

		@Override
		public void onFinish(String errorMessage, boolean interrupted,
				Set<NetworkLibraryItem> uncommitedItems) {
			if (interrupted) {
				myTree.setSearchResult(null);
			} else {
				myTree.updateSubTrees();
				afterUpdateCatalog(errorMessage, myTree.getSearchResult().empty());
			}
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChangedAsync();
			}
		}

		private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			ZLResource boxResource = null;
			String msg = null;
			if (errorMessage != null) {
				boxResource = dialogResource.getResource("networkError");
				msg = errorMessage;
			} else if (childrenEmpty) {
				boxResource = dialogResource.getResource("emptySearchResults");
				msg = boxResource.getResource("message").getValue();
			}
			if (msg != null) {
				if (NetworkView.Instance().isInitialized()) {
					final NetworkCatalogActivity activity = NetworkView.Instance().getOpenedActivity(SEARCH_RUNNABLE_KEY);
					if (activity != null) {
						final ZLResource buttonResource = dialogResource.getResource("button");
						new AlertDialog.Builder(activity)
							.setTitle(boxResource.getResource("title").getValue())
							.setMessage(msg)
							.setIcon(0)
							.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
							.create().show();
					}
				}
			}
		}
	}

	private static class SearchRunnable extends ItemsLoadingRunnable {
		private final String myPattern;

		public SearchRunnable(ItemsLoadingHandler handler, String pattern) {
			super(handler);
			myPattern = pattern;
		}

		public String getResourceKey() {
			return "searchingNetwork";
		}

		public void doBefore() {
		}

		public void doLoading(NetworkOperationData.OnNewItemListener doWithListener) throws ZLNetworkException {
			NetworkLibrary.Instance().simpleSearch(myPattern, doWithListener);
		}
	}

	protected void runSearch(final String pattern) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.NetworkSearchPatternOption.setValue(pattern);

		if (NetworkView.Instance().containsItemsLoadingRunnable(SEARCH_RUNNABLE_KEY)) {
			return;
		}

		final String summary = ZLResource.resource("networkView").getResource("searchResults").getValue().replace("%s", pattern);
		final SearchResult result = new SearchResult(summary);

		final SearchItemTree tree = NetworkView.Instance().getSearchItemTree();

		tree.setSearchResult(result);
		NetworkView.Instance().fireModelChangedAsync();

		final SearchHandler handler = new SearchHandler(tree);
		NetworkView.Instance().startItemsLoading(
			this,
			SEARCH_RUNNABLE_KEY,
			new SearchRunnable(handler, pattern)
		);
		NetworkView.Instance().openTree(this, tree, SEARCH_RUNNABLE_KEY);
	}
}
