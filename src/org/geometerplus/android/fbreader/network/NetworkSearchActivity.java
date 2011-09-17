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
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.SearchCatalogTree;

public class NetworkSearchActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			SearchCatalogTree searchTree = null;
			final Bundle data = intent.getBundleExtra(SearchManager.APP_DATA);
			if (data != null) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				final NetworkTree.Key key =
					(NetworkTree.Key)data.getSerializable(NetworkLibraryActivity.TREE_KEY_KEY);
				final NetworkTree tree = library.getTreeByKey(key);
				if (tree instanceof SearchCatalogTree) {
					searchTree = (SearchCatalogTree)tree;
				}
			}
			if (searchTree != null) {
				runSearch(searchTree, intent.getStringExtra(SearchManager.QUERY));
			}
		}
		finish();
	}

	private static class Searcher extends ItemsLoader {
		private final String myPattern;

		public Searcher(Activity activity, SearchCatalogTree tree, String pattern) {
			super(activity, tree);
			myPattern = pattern;
		}

		@Override
		public void doBefore() {
		}

		@Override
		public void doLoading() {
			try {
				NetworkLibrary.Instance().simpleSearch(myPattern, this);
			} catch (ZLNetworkException e) {
			}
		}

			/*
		@Override
		protected void addItem(NetworkItem item) {
			SearchResult result = getTree().getSearchResult();
			if (item instanceof NetworkBookItem) {
				result.addBook((NetworkBookItem)item);
				getTree().updateSubTrees();
				NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
			}
		}
			*/

		@Override
		protected void onFinish(String errorMessage, boolean interrupted) {
			if (interrupted) {
				//getTree().setSearchResult(null);
			} else {
				//getTree().updateSubTrees();
				//afterUpdateCatalog(errorMessage, getTree().getSearchResult().isEmpty());
			}
			NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
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

			final SearchCatalogTree tree = null;//NetworkLibrary.Instance().getSearchCatalogTree();
			if (tree == null) {
				return;
			}

			final ZLResource buttonResource = dialogResource.getResource("button");
			new AlertDialog.Builder(myActivity)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(msg)
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
				.create().show();
		}
	}

	protected void runSearch(SearchCatalogTree tree, String pattern) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.NetworkSearchPatternOption.setValue(pattern);

		/*
		final String summary = NetworkLibrary.resource().getResource("searchResults").getValue().replace("%s", pattern);
		final SearchResult result = new SearchResult(summary);

		//tree.setSearchResult(result);
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);

		new Searcher(this, tree, pattern).start();
		Util.openTree(this, tree);
		*/
	}
}
