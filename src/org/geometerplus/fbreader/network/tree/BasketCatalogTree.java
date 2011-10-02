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

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.BasketItem;

public class BasketCatalogTree extends NetworkCatalogTree {
	public BasketCatalogTree(NetworkCatalogTree parent, BasketItem item, int position) {
		super(parent, item, position);
		item.setTree(this);
		setCover(null);
		if (!item.bookIds().isEmpty()) {
			startItemsLoader(false, false);
		}
	}

	@Override
	public synchronized void addItem(NetworkItem i) {
		if (!(i instanceof NetworkBookItem)) {
			return;
		}

		final BasketItem basketItem = (BasketItem)Item;
		final NetworkBookItem bookItem = (NetworkBookItem)i;
		if (basketItem.contains(bookItem)) {
			super.addItem(bookItem);
			basketItem.addItem(bookItem);
		}
	}

	public synchronized void removeItem(NetworkBookItem i) {
		for (FBTree t : subTrees()) {
			if (i.equals(((NetworkBookTree)t).Book)) {
				t.removeSelf();
				break;
			}
		}
	}
}
