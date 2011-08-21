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

public class NetworkCatalogTree extends NetworkTree {
	public final NetworkCatalogItem Item;
	public final ArrayList<NetworkItem> ChildrenItems = new ArrayList<NetworkItem>();

	private long myLoadedTime = -1;

	public NetworkCatalogTree(RootTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
	}

	NetworkCatalogTree(NetworkCatalogTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
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
		FBTree tree = Parent;
		while (tree instanceof NetworkCatalogTree) {
			((NetworkCatalogTree) tree).myLoadedTime = myLoadedTime;
			tree = tree.Parent;
		}
	}

	public void updateVisibility() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		for (int i = 0; i < ChildrenItems.size(); ++i) {
			NetworkItem currentItem = ChildrenItems.get(i);
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
							child.clear();
							child.ChildrenItems.clear();
							break;
					}
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					for (int j = i + 1; j < ChildrenItems.size(); ++j) {
						if (child.Item == ChildrenItems.get(j)) {
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
		ChildrenItems.removeAll(items);
		super.removeItems(items);
	}

	@Override
	protected String getStringId() {
		return Item.getStringId();
	}

	public void clearCatalog() {
		ChildrenItems.clear();
		clear();
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}
}
