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


public class NetworkSearchActivity extends Activity {

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

		public SearchHandler(SearchResult result) {
			myResult = result;
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
			super(handler);
			myPattern = pattern;
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

		final String summary = ZLResource.resource("networkView").getResource("searchResults").getValue().replace("%s", pattern);

		final SearchResult result = new SearchResult(summary);

		library.setSearchResult(result);
		library.invalidate();
		library.synchronize();

		final SearchHandler handler = new SearchHandler(result);
		NetworkLibraryActivity.Instance.startItemsLoading(
			"org.geometerplus.android.fbreader.network.NetworkSearchActivity",
			new SearchRunnable(handler, pattern)
		);
	}

	protected Activity getParentActivity() {
		return NetworkLibraryActivity.Instance;
	}
}
