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

import java.util.*;

import org.geometerplus.zlibrary.core.network.QuietNetworkContext;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;

public class BasketCatalogTree extends NetworkCatalogTree {
	private long myGeneration = -1;

	public BasketCatalogTree(NetworkCatalogTree parent, BasketItem item, int position) {
		super(parent, parent.getLink(), item, position);
		if (!item.bookIds().isEmpty()) {
			startItemsLoader(new QuietNetworkContext(), false, false);
		}
	}

	public BasketCatalogTree(RootTree parent, BasketItem item) {
		super(parent, item.Link, item, 0);
		if (!item.bookIds().isEmpty()) {
			startItemsLoader(new QuietNetworkContext(), false, false);
		}
	}

	@Override
	protected boolean canUseParentCover() {
		return false;
	}

	@Override
	public synchronized List<FBTree> subtrees() {
		final BasketItem basketItem = (BasketItem)Item;
		final long generation = basketItem.getGeneration();
		if (generation != myGeneration) {
			myGeneration = generation;
			final List<FBTree> toRemove = new ArrayList<FBTree>();
			final Set<String> idsToAdd = new TreeSet(basketItem.bookIds());
			for (FBTree t : super.subtrees()) {
				if (!(t instanceof NetworkBookTree)) {
					continue;
				}
				final NetworkBookTree bookTree = (NetworkBookTree)t;
				if (basketItem.contains(bookTree.Book)) {
					idsToAdd.remove(bookTree.Book.Id);
				} else {
					toRemove.add(bookTree);
				}
			}
			for (FBTree t : toRemove) {
				t.removeSelf();
			}
			for (String id : idsToAdd) {
				final NetworkBookItem book = basketItem.getBook(id);
				if (book != null) {
					NetworkTreeFactory.createNetworkTree(this, book);
				}
			}
		}
		return super.subtrees();
	}

	@Override
	public synchronized void addItem(NetworkItem i) {
		if (!(i instanceof NetworkBookItem)) {
			return;
		}
		final NetworkBookItem bookItem = (NetworkBookItem)i;
		final String id = bookItem.getStringId();
		for (FBTree t : subtrees()) {
			if (t instanceof NetworkBookTree &&
				id.equals(((NetworkBookTree)t).Book.getStringId())) {
				return;
			}
		}

		final BasketItem basketItem = (BasketItem)Item;
		if (basketItem.contains(bookItem)) {
			super.addItem(bookItem);
			basketItem.addItem(bookItem);
		}
	}
}
