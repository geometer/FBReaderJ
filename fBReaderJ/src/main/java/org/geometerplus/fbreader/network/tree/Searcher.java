/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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
		NetworkLibrary.Instance().NetworkSearchPatternOption.setValue(myPattern);
	}

	@Override
	public void interrupt() {
		// Searcher is not interruptable at this moment
	}

	@Override
	public void load() throws ZLNetworkException {
		final SearchItem item = (SearchItem)getTree().Item;
		if (myPattern.equals(item.getPattern())) {
			if (getTree().hasChildren()) {
				myItemFound = true;
				NetworkLibrary.Instance().fireModelChangedEvent(
					NetworkLibrary.ChangeListener.Code.Found, getTree()
				);
			} else {
				NetworkLibrary.Instance().fireModelChangedEvent(
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
			((SearchCatalogTree)getTree()).setPattern(myPattern);
			getTree().clearCatalog();
			NetworkLibrary.Instance().fireModelChangedEvent(
				NetworkLibrary.ChangeListener.Code.Found, getTree()
			);
			myItemFound = true;
		}
		super.onNewItem(item);
	}

	@Override
	protected void onFinish(ZLNetworkException exception, boolean interrupted) {
		if (!interrupted && !myItemFound) {
			NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.NotFound);
		}
	}
}
