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

import java.util.List;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.SearchManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.SearchResultTree;

import org.geometerplus.android.fbreader.ZLTreeAdapter;


public class NetworkSearchActivity extends Activity {

	public static final String SEARCH_RUNNABLE_KEY = "org.geometerplus.android.fbreader.network.NetworkSearchActivity";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			runSearch(pattern);
		}
		finish();
	}

	private class SearchHandler extends ItemsLoadingHandler {

		private final SearchResult myResult;
		private boolean doExpand;

		public SearchHandler(SearchResult result) {
			myResult = result;
			doExpand = true;
		}

		public void onUpdateItems(List<NetworkLibraryItem> items) {
			for (NetworkLibraryItem item: items) {
				if (item instanceof NetworkBookItem) {
					myResult.addBook((NetworkBookItem) item);
				}
			}
		}

		public void afterUpdateItems() {
			final NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidate();
			library.synchronize();
			if (NetworkLibraryActivity.Instance != null) {
				NetworkLibraryActivity.Instance.resetTree();
			}

			if (doExpand && NetworkLibraryActivity.Instance != null) {
				final SearchResultTree tree = library.getSearchResultTree();
				if (tree != null) {
					ZLTreeAdapter adapter = NetworkLibraryActivity.Instance.getAdapter();
					if (adapter != null) {
						adapter.expandOrCollapseTree(tree);
					}
					doExpand = !tree.hasChildren();
				}
			}
		}

		public void onFinish(String errorMessage) {
			afterUpdateCatalog(errorMessage, myResult.empty());
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
				if (NetworkLibraryActivity.Instance != null) {
					final ZLResource buttonResource = dialogResource.getResource("button");
					new AlertDialog.Builder(NetworkLibraryActivity.Instance)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage(msg)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
						.create().show();
				}
				// TODO: else show notification???
			}
		}
	}

	private static class SearchRunnable extends ItemsLoadingRunnable {

		private final String myPattern;

		public SearchRunnable(ItemsLoadingHandler handler, String pattern) {
			super(handler, NETWORK_SEARCH);
			myPattern = pattern;
		}

		public int getNotificationId() {
			return NetworkNotifications.Instance().getNetworkSearchId();
		}

		public String getResourceKey() {
			return "searchingNetwork";
		}

		public String doBefore() {
			return null;
		}

		public String doLoading(NetworkOperationData.OnNewItemListener doWithListener) {
			return NetworkLibrary.Instance().simpleSearch(myPattern, doWithListener);
		}
	}

	protected void runSearch(final String pattern) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.NetworkSearchPatternOption.setValue(pattern);

		if (NetworkLibraryActivity.Instance == null) {
			return;
		}

		if (NetworkLibraryActivity.Instance.getItemsLoadingRunnable(SEARCH_RUNNABLE_KEY) != null) {
			return;
		}

		final String summary = ZLResource.resource("networkView").getResource("searchResults").getValue().replace("%s", pattern);

		final SearchResult result = new SearchResult(summary);

		library.setSearchResult(result);
		library.invalidate();
		library.synchronize();
		if (NetworkLibraryActivity.Instance != null) {
			NetworkLibraryActivity.Instance.resetTree();
		}

		final SearchHandler handler = new SearchHandler(result);
		NetworkLibraryActivity.Instance.startItemsLoading(
			SEARCH_RUNNABLE_KEY,
			new SearchRunnable(handler, pattern)
		);
	}

	protected Activity getParentActivity() {
		return NetworkLibraryActivity.Instance;
	}
}
