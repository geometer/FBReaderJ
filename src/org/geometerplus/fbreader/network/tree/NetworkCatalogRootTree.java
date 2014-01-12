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

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;

public class NetworkCatalogRootTree extends NetworkCatalogTree {
	public NetworkCatalogRootTree(RootTree parent, INetworkLink link, int position) {
		super(parent, link, (NetworkCatalogItem)link.libraryItem(), position);
	}

	public NetworkCatalogRootTree(RootTree parent, INetworkLink link) {
		this(parent, link, -1);
	}

	@Override
	public String getTreeTitle() {
		return getName();
	}

	@Override
	protected void addSpecialTrees() {
		super.addSpecialTrees();
		final BasketItem basketItem = getLink().getBasketItem();
		if (basketItem != null) {
			myChildrenItems.add(basketItem);
			new BasketCatalogTree(this, basketItem, -1);
		}
	}

	@Override
	public int compareTo(FBTree tree) {
		if (!(tree instanceof NetworkCatalogRootTree)) {
			return 1;
		}
		return getLink().compareTo(((NetworkCatalogRootTree)tree).getLink());
	}
}
