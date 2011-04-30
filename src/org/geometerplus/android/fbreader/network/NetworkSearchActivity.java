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
import org.geometerplus.fbreader.network.tree.SearchItemTree;


public class NetworkSearchActivity extends Activity {
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
		public void onUpdateItems(List<NetworkItem> items) {
			SearchResult result = myTree.getSearchResult();
			for (NetworkItem item: items) {
				if (item instanceof NetworkBookItem) {
					result.addBook((NetworkBookItem)item);
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
				Set<NetworkItem> uncommitedItems) {
			if (interrupted) {
				myTree.setSearchResult(null);
			} else {
				myTree.updateSubTrees();
				afterUpdateCatalog(errorMessage, myTree.getSearchResult().isEmpty());
			}
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChangedAsync();
			}
		}

		private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			ZLResource boxResource = null;
			String msg;
			if (errorMessage != null) {
				boxResource = dialogResource.getResource("networkError");
				msg = errorMessage;
			} else if (childrenEmpty) {
				boxResource = dialogResource.getResource("emptySearchResults");
				msg = boxResource.getResource("message").getValue();
			} else {
				return;
			}

			final SearchItemTree tree = NetworkLibrary.Instance().getSearchItemTree();
			if (tree == null) {
				return;
			}

			final NetworkCatalogActivity activity = NetworkCatalogActivity.getByTree(tree);
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

		final SearchItemTree tree = library.getSearchItemTree();
		if (tree == null ||
			ItemsLoadingService.getRunnable(tree) != null) {
			return;
		}

		final String summary = ZLResource.resource("networkView").getResource("searchResults").getValue().replace("%s", pattern);
		final SearchResult result = new SearchResult(summary);

		tree.setSearchResult(result);
		NetworkView.Instance().fireModelChangedAsync();

		final SearchHandler handler = new SearchHandler(tree);
		ItemsLoadingService.start(
			this, tree, new SearchRunnable(handler, pattern)
		);
		Util.openTree(this, tree);
	}
}
