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

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;


public class SearchResultTree extends NetworkTree {

	public final SearchResult Result;

	public SearchResultTree(NetworkTree parent, SearchResult result) {
		super(parent);
		Result = result;
	}

	@Override
	public String getName() {
		return ZLResource.resource("networkView").getResource("searchResultNode").getValue();
	}

	@Override
	public String getSummary() {
		return Result.Summary;
	}

	@Override
	protected ZLImage createCover() {
		ZLResourceFile file = ZLResourceFile.createResourceFile("data/searchresult.png");
		return new ZLFileImage("image/png", file);
	}

	public void updateSubTrees() {
		final LinkedList<FBTree> toRemove = new LinkedList<FBTree>();

		ListIterator<FBTree> nodeIterator = subTrees().listIterator();
		FBTree currentNode = null;
		int nodeCount = 0;

		final ArrayList<NetworkBookItem.AuthorData> authors = new ArrayList<NetworkBookItem.AuthorData>();
		authors.addAll(Result.BooksMap.keySet());

		for (int i = 0; i < authors.size(); ++i) {
			NetworkBookItem.AuthorData currentItem = authors.get(i);
			boolean processed = false;
			while (currentNode != null || nodeIterator.hasNext()) {
				if (currentNode == null) {
					currentNode = nodeIterator.next();
				}
				if (!(currentNode instanceof NetworkAuthorTree)) {
					currentNode = null;
					++nodeCount;
					continue;
				}
				NetworkAuthorTree child = (NetworkAuthorTree) currentNode;
				if (child.Author.equals(currentItem)) {
					LinkedList<NetworkBookItem> authorBooks = Result.BooksMap.get(currentItem);
					if (child.BooksNumber != authorBooks.size()) { // TODO: implement by child's method
						//update author's books
						child.BooksNumber = authorBooks.size(); // TODO: move into child's update code
					}
					currentNode = null;
					++nodeCount;
					processed = true;
					break;
				} else {
					boolean found = false;
					for (int j = i + 1; j < authors.size(); ++j) {
						if (child.Author.equals(authors.get(j))) {
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
			if (!processed) {
				LinkedList<NetworkBookItem> authorBooks = Result.BooksMap.get(currentItem);
				if (authorBooks.size() != 0) {
					NetworkAuthorTree child = new NetworkAuthorTree(this, currentItem);
					//update author's books
					child.BooksNumber = authorBooks.size(); // TODO: move into child's update code
					++nodeCount;
					nodeIterator = subTrees().listIterator(nextIndex + 1);
				}
			}
		}

		while (currentNode != null || nodeIterator.hasNext()) {
			if (currentNode == null) {
				currentNode = nodeIterator.next();
			}
			toRemove.add(currentNode);
			currentNode = null;
		}

		for (FBTree tree: toRemove) {
			tree.removeSelf();
		}
	}
}
