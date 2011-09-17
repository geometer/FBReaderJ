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

package org.geometerplus.fbreader.network.tree;

//import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

public class Searcher extends NetworkItemsLoader {
	private final String myPattern;
	private volatile boolean myItemFound;

	public Searcher(SearchCatalogTree tree, String pattern) {
		super(tree);
		myPattern = pattern;
	}

	@Override
	public void doBefore() {
	}

	@Override
	public void doLoading() {
		//try {
			//NetworkLibrary.Instance().simpleSearch(myPattern, this);
		//} catch (ZLNetworkException e) {
		//}
	}

	@Override
	public synchronized void onNewItem(final NetworkItem item) {
		if (!myItemFound) {
			((SearchCatalogTree)getTree()).setPattern(myPattern);
			getTree().clearCatalog();
			NetworkLibrary.Instance().fireModelChangedEvent(
				NetworkLibrary.ChangeListener.Code.Found, getTree().getUniqueKey()
			);
			myItemFound = true;
		}
		super.onNewItem(item);
	}

	@Override
	protected void onFinish(String errorMessage, boolean interrupted) {
		if (interrupted) {
			//getTree().setSearchResult(null);
		} else {
			//getTree().updateSubTrees();
			//afterUpdateCatalog(errorMessage, getTree().getSearchResult().isEmpty());
		}
		if (!myItemFound) {
			NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.NotFound);
		}
	}

	/*
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
	*/
}
