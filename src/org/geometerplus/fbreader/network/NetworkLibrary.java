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

package org.geometerplus.fbreader.network;

import java.util.*;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.tree.*;

public class NetworkLibrary {
	private static NetworkLibrary ourInstance;

	public static NetworkLibrary Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkLibrary();
		}
		return ourInstance;
	}


	private final ArrayList<NetworkLink> myLinks = new ArrayList<NetworkLink>();
	private final RootTree myRootTree = new RootTree();

	private boolean myUpdateChildren = true;


	public NetworkLibrary() {
	}

	public List<NetworkLink> links() {
		return Collections.unmodifiableList(myLinks);
	}

	public void invalidate() {
		myUpdateChildren = true;
	}

	private void makeUpToDate() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		Iterator<FBTree> nodeIterator = myRootTree.iterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		for (int i = 0; i < myLinks.size(); ++i) {
			NetworkLink link = myLinks.get(i);
			if (!link.OnOption.getValue()) {
				continue;
			}
			boolean processed = false;
			while (currentNode != null || nodeIterator.hasNext()) {
				if (currentNode == null) {
					currentNode = nodeIterator.next();
				}
				if (!(currentNode instanceof NetworkCatalogTree)) {
					currentNode = null;
					continue;
				}
				final NetworkLink nodeLink = ((NetworkCatalogTree)currentNode).Item.Link;
				if (nodeLink == link) {
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					for (int j = i + 1; j < myLinks.size(); ++j) {
						if (nodeLink == myLinks.get(j)) {
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
			if (!processed) {
				NetworkCatalogRootTree ptr = new NetworkCatalogRootTree(myRootTree, link, nodeCount++);
				//ptr.item().onDisplayItem();
			}
		}

		/*SearchResultNode srNode = null;
		while (nodeIterator.hasNext()) {
			FBTree node = nodeIterator.next();
			++nodeCount;
			if (node instanceof SearchResultNode) {
				srNode = (SearchResultNode) node;
			} else {
				toRemove.add(node);
			}
		}

		final SearchResult searchResult = SearchResult.lastSearchResult();
		NetworkBookCollection result = searchResult.collection();
		if (result.isNull()) {
			if (srNode != 0) {
				toRemove.add(srNode);
			}
		} else if (srNode == null || srNode->searchResult() != result) {
			if (srNode != null) {
				toRemove.add(srNode);
			}
			srNode = new SearchResultNode(myRootTree, result, searchResult.summary()); // at nodeCount ??? or not???
			NetworkNodesFactory::createSubnodes(srNode, result);
		}*/

		for (FBTree tree : toRemove) {
			tree.removeSelf();
		}

		/*if (srNode != null) {
			srNode->open(false);
			srNode->expandOrCollapseSubtree();
		}*/
	}

	public void synchronize() {
		if (myUpdateChildren) {
			myUpdateChildren = false;
			makeUpToDate();
		}
	}

	public NetworkTree getTree() {
		synchronize();
		return myRootTree;
	}

}
