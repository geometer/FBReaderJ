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

package org.geometerplus.android.fbreader.network.action;

import java.util.*;

import android.app.Activity;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.fbreader.network.BuyBooksActivity;

public class BuyBasketBooksAction extends CatalogAction {
	public BuyBasketBooksAction(Activity activity) {
		super(activity, ActionCode.BASKET_BUY_ALL_BOOKS, "buyAllBooks");
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		return tree instanceof BasketCatalogTree && ((BasketCatalogTree)tree).canBeOpened();
	}

	@Override
	public boolean isEnabled(NetworkTree tree) {
		if (myLibrary.getStoredLoader(tree) != null) {
			return false;
		}
		final Set<String> bookIds = new HashSet<String>();
		for (FBTree t : tree.subtrees()) {
			if (t instanceof NetworkBookTree) {
				bookIds.add(((NetworkBookTree)t).Book.Id);
			}
		}
		final BasketItem item = (BasketItem)((BasketCatalogTree)tree).Item;
		return bookIds.equals(new HashSet(item.bookIds()));
	}

	@Override
	public void run(NetworkTree tree) {
		final ArrayList<NetworkBookTree> bookTrees = new ArrayList<NetworkBookTree>();
		for (FBTree t : tree.subtrees()) {
			if (t instanceof NetworkBookTree) {
				bookTrees.add((NetworkBookTree)t);
			}
		}
		BuyBooksActivity.run(myActivity, bookTrees);
	}
}
