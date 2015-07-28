/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.app.SearchManager;
import android.os.Bundle;
import android.content.Intent;

import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.SearchCatalogTree;

import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;

public class NetworkSearchActivity extends Activity {
	private final ActivityNetworkContext myNetworkContext = new ActivityNetworkContext(this);

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final Bundle data = intent.getBundleExtra(SearchManager.APP_DATA);
			if (data != null) {
				final NetworkLibrary library = Util.networkLibrary(this);
				final NetworkTree.Key key =
					(NetworkTree.Key)data.getSerializable(NetworkLibraryActivity.TREE_KEY_KEY);
				final NetworkTree tree = library.getTreeByKey(key);
				if (tree instanceof SearchCatalogTree) {
					final SearchCatalogTree searchTree = (SearchCatalogTree)tree;
					final String pattern = intent.getStringExtra(SearchManager.QUERY);
					final MimeType mime = searchTree.getMimeType();
					if (MimeType.APP_ATOM_XML.weakEquals(mime)) {
						searchTree.startItemsLoader(myNetworkContext, pattern);
					} else if (MimeType.TEXT_HTML.weakEquals(mime)) {
						Util.openInBrowser(this, searchTree.getUrl(pattern));
					}
				}
			}
		}
		finish();
	}
}
