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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

class Searcher extends NetworkItemsLoader {
	private final String myPattern;
	private volatile boolean myItemFound;

	Searcher(ZLNetworkContext nc, SearchCatalogTree tree, String pattern) {
		super(nc, tree);
		myPattern = pattern;
	}

	@Override
	public void doBefore() {
		Tree.Library.NetworkSearchPatternOption.setValue(myPattern);
	}

	@Override
	public void interrupt() {
		// Searcher is not interruptable at this moment
	}

	@Override
	public void load() throws ZLNetworkException {
		final SearchItem item = (SearchItem)Tree.Item;
		if (myPattern.equals(item.getPattern())) {
			if (Tree.hasChildren()) {
				myItemFound = true;
				Tree.Library.fireModelChangedEvent(
					NetworkLibrary.ChangeListener.Code.Found, Tree
				);
			} else {
				Tree.Library.fireModelChangedEvent(
					NetworkLibrary.ChangeListener.Code.NotFound
				);
			}
		} else {
			item.runSearch(NetworkContext, this, myPattern);
		}
	}

	@Override
	public synchronized void onNewItem(final NetworkItem item) {
		if (!myItemFound) {
			((SearchCatalogTree)Tree).setPattern(myPattern);
			Tree.clearCatalog();
			Tree.Library.fireModelChangedEvent(
				NetworkLibrary.ChangeListener.Code.Found, Tree
			);
			myItemFound = true;
		}
		super.onNewItem(item);
	}

	@Override
	protected void onFinish(ZLNetworkException exception, boolean interrupted) {
		if (!interrupted && !myItemFound) {
			Tree.Library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.NotFound);
		}
	}
}
