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
		ListIterator<FBTree> nodeIterator = subTrees().listIterator();

		final Set<NetworkBookItem.AuthorData> authorsSet = Result.BooksMap.keySet();

		for (NetworkBookItem.AuthorData author: authorsSet) {
			if (nodeIterator != null) {
				if (nodeIterator.hasNext()) {
					FBTree currentNode = nodeIterator.next();
					if (!(currentNode instanceof NetworkAuthorTree)) {
						throw new RuntimeException("That's impossible!!!");
					}
					NetworkAuthorTree child = (NetworkAuthorTree) currentNode;
					if (!child.Author.equals(author)) {
						throw new RuntimeException("That's impossible!!!");
					}
					LinkedList<NetworkBookItem> authorBooks = Result.BooksMap.get(author);
					child.updateSubTrees(authorBooks);
					continue;
				}
				nodeIterator = null;
			}

			LinkedList<NetworkBookItem> authorBooks = Result.BooksMap.get(author);
			if (authorBooks.size() != 0) {
				NetworkAuthorTree child = new NetworkAuthorTree(this, author);
				child.updateSubTrees(authorBooks);
			}
		}
		if (nodeIterator != null && nodeIterator.hasNext()) {
			throw new RuntimeException("That's impossible!!!");
		}
	}
}
