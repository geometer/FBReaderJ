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

import java.util.*;

import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public class NetworkCatalogTree extends NetworkTree {
	public final NetworkCatalogItem Item;
	private final ArrayList<NetworkItem> myChildrenItems = new ArrayList<NetworkItem>();

	private long myLoadedTime = -1;

	public NetworkCatalogTree(RootTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
		addSearchTree();
	}

	NetworkCatalogTree(NetworkCatalogTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
		addSearchTree();
	}

	private void addSearchTree() {
		if ((Item.getFlags() & NetworkCatalogItem.FLAG_ADD_SEARCH_ITEM) != 0) {
			if (Item.Link.getUrl(UrlInfo.Type.Search) != null) {
				final SearchItem item = new SearchItem(Item.Link);
				myChildrenItems.add(item);
				new SearchCatalogTree(this, item, 0);
			}
		}
	}

	void addItem(final NetworkItem item) {
		myUnconfirmedItems.add(item);
		myChildrenItems.add(item);
		NetworkTreeFactory.createNetworkTree(this, item);
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	@Override
	public String getName() {
		return Item.Title.toString();
	}

	@Override
	public String getSummary() {
		final CharSequence summary = Item.getSummary();
		return summary != null ? summary.toString() : "";
	}

	@Override
	public String getTreeTitle() {
		return getName() + " - " + Item.Link.getSiteName();
	}

	@Override
	protected ZLImage createCover() {
		return createCover(Item);
	}

	public boolean isContentValid() {
		if (myLoadedTime < 0) {
			return false;
		}
		final int reloadTime = 15 * 60 * 1000; // 15 minutes in milliseconds
		return System.currentTimeMillis() - myLoadedTime < reloadTime;
	}

	public void updateLoadedTime() {
		myLoadedTime = System.currentTimeMillis();
	}

	public void updateVisibility() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		for (int i = 0; i < myChildrenItems.size(); ++i) {
			NetworkItem currentItem = myChildrenItems.get(i);
			if (!(currentItem instanceof NetworkCatalogItem)) {
				continue;
			}
			boolean processed = false;
			while (currentNode != null || nodeIterator.hasNext()) {
				if (currentNode == null) {
					currentNode = nodeIterator.next();
				}
				if (!(currentNode instanceof NetworkCatalogTree)) {
					currentNode = null;
					++nodeCount;
					continue;
				}
				NetworkCatalogTree child = (NetworkCatalogTree) currentNode;
				if (child.Item == currentItem) {
					switch (child.Item.getVisibility()) {
						case B3_TRUE:
							child.updateVisibility();
							break;
						case B3_FALSE:
							toRemove.add(child);
							break;
						case B3_UNDEFINED:
							child.clearCatalog();
							break;
					}
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					for (int j = i + 1; j < myChildrenItems.size(); ++j) {
						if (child.Item == myChildrenItems.get(j)) {
							found = true;
							break;
						}
					}
					if (!found) {
						toRemove.add(currentNode);
						currentNode = null;
						++nodeCount;
					} else {
						break;
					}
				}
			}
			final int nextIndex = nodeIterator.nextIndex();
			if (!processed && NetworkTreeFactory.createNetworkTree(this, currentItem, nodeCount) != null) {
				++nodeCount;
				nodeIterator = subTrees().listIterator(nextIndex + 1);
			}
		}

		while (currentNode != null || nodeIterator.hasNext()) {
			if (currentNode == null) {
				currentNode = nodeIterator.next();
			}
			if (currentNode instanceof NetworkCatalogTree) {
				toRemove.add(currentNode);
			}
			currentNode = null;
		}

		for (FBTree tree: toRemove) {
			tree.removeSelf();
		}
	}

	@Override
	public NetworkItem getHoldedItem() {
		return Item;
	}

	@Override
	public void removeItems(Set<NetworkItem> items) {
		myChildrenItems.removeAll(items);
		super.removeItems(items);
	}

	@Override
	protected String getStringId() {
		return Item.getStringId();
	}

	public void clearCatalog() {
		myChildrenItems.clear();
		clear();
		addSearchTree();
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	private final List<NetworkItem> myUnconfirmedItems =
		Collections.synchronizedList(new LinkedList<NetworkItem>());

	public final void confirmAllItems() {
		myUnconfirmedItems.clear();
	}

	public final void removeUnconfirmedItems() {
		final Set<NetworkItem> unconfirmedItems;
		synchronized (myUnconfirmedItems) {
			unconfirmedItems = new HashSet<NetworkItem>(myUnconfirmedItems);
		}
		removeItems(unconfirmedItems);
	}
}
