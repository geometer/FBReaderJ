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

package org.geometerplus.android.fbreader.network;

import java.util.Set;
import java.util.LinkedList;
import java.util.ListIterator;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkAuthorTree;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.R;

public class SearchItemTree extends NetworkTree {

	private SearchResult myResult;

	public SearchItemTree() {
		super(1);
	}

	@Override
	public String getName() {
		return ZLResource.resource("networkView").getResource("search").getValue();
	}

	@Override
	public String getSummary() {
		return ZLResource.resource("networkView").getResource("searchSummary").getValue();
	}

	@Override
	protected ZLImage createCover() {
		ZLResourceFile file = ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).createDrawableFile(R.drawable.ic_list_searchresult);
		return new ZLFileImage("image/png", file);
	}

	public void setSearchResult(SearchResult result) {
		myResult = result;
		clear();
	}

	public SearchResult getSearchResult() {
		return myResult;
	}

	public void updateSubTrees() {
		ListIterator<FBTree> nodeIterator = subTrees().listIterator();

		final Set<NetworkBookItem.AuthorData> authorsSet = myResult.BooksMap.keySet();

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
					LinkedList<NetworkBookItem> authorBooks = myResult.BooksMap.get(author);
					child.updateSubTrees(authorBooks);
					continue;
				}
				nodeIterator = null;
			}

			LinkedList<NetworkBookItem> authorBooks = myResult.BooksMap.get(author);
			if (authorBooks.size() != 0) {
				NetworkAuthorTree child = new NetworkAuthorTree(this, author);
				child.updateSubTrees(authorBooks);
			}
		}
		if (nodeIterator != null && nodeIterator.hasNext()) {
			throw new RuntimeException("That's impossible!!!");
		}
	}

	@Override
	public NetworkLibraryItem getHoldedItem() {
		return null;
	}
}
