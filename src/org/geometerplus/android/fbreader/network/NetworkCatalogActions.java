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

import java.util.*;

import android.app.Activity;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.opds.BasketItem;

import org.geometerplus.android.fbreader.network.action.ActionCode;

public class NetworkCatalogActions {
	public boolean runAction(final NetworkLibraryActivity activity, NetworkTree tree, int actionCode) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;

		final NetworkCatalogItem item = catalogTree.Item;
		switch (actionCode) {
			case ActionCode.BASKET_CLEAR:
				item.Link.basket().clear();
				return true;
			case ActionCode.BASKET_BUY_ALL_BOOKS:
				return true;
		}
		return false;
	}

	public static void clearTree(Activity activity, final NetworkCatalogTree tree) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				tree.ChildrenItems.clear();
				tree.clear();
				NetworkView.Instance().fireModelChanged();
			}
		});
	}
}
