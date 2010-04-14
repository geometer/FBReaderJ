/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;

public class NetworkCatalogTree extends NetworkTree {

	public final NetworkCatalogItem Item;
	public final ArrayList<NetworkLibraryItem> ChildrenItems = new ArrayList<NetworkLibraryItem>();

	NetworkCatalogTree(RootTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
	}

	NetworkCatalogTree(NetworkCatalogTree parent, NetworkCatalogItem item, int position) {
		super(parent, position);
		Item = item;
	}

	@Override
	public String getName() {
		return Item.Title;
	}

	@Override
	public String getSummary() {
		if (Item.Summary == null) {
			return "";
		}
		return Item.Summary;
	}

	@Override
	protected ZLImage createCover() {
		return createCover(Item);
	}

	static boolean processAccountDependent(NetworkCatalogItem item) {
		if (item.Visibility == NetworkCatalogItem.VISIBLE_ALWAYS) {
			return true;
		}
		final NetworkLink link = item.Link;
		if (link.authenticationManager() == null) {
			return false;
		}
		return link.authenticationManager().isAuthorised(false).Status == ZLBoolean3.B3_TRUE;
	}

	public void updateAccountDependents() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		for (int i = 0; i < ChildrenItems.size(); ++i) {
			NetworkLibraryItem currentItem = ChildrenItems.get(i);
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
					if (processAccountDependent(child.Item)) {
						child.updateAccountDependents();
					} else {
						toRemove.add(child);
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
}
