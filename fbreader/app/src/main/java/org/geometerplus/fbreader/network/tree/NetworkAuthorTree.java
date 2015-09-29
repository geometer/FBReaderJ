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

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.*;


public class NetworkAuthorTree extends NetworkTree {

	public final NetworkBookItem.AuthorData Author;

	private int myBooksNumber;
	private HashMap<String,Integer> mySeriesMap;

	NetworkAuthorTree(NetworkTree parent, NetworkBookItem.AuthorData author) {
		super(parent);
		Author = author;
	}

	@Override
	public String getName() {
		return Author.DisplayName;
	}

	@Override
	protected String getSortKey() {
		return Author.SortKey;
	}

	private int getSeriesIndex(String seriesName) {
		if (mySeriesMap == null) {
			return -1;
		}
		Integer value = mySeriesMap.get(seriesName);
		if (value == null) {
			return -1;
		}
		return value.intValue();
	}

	private void setSeriesIndex(String seriesName, int index) {
		if (mySeriesMap == null) {
			mySeriesMap = new HashMap<String,Integer>();
		}
		mySeriesMap.put(seriesName, Integer.valueOf(index));
	}

	public void updateSubtrees(LinkedList<NetworkBookItem> books) {
		if (myBooksNumber >= books.size()) {
			return;
		}

		ListIterator<NetworkBookItem> booksIterator = books.listIterator(myBooksNumber);
		while (booksIterator.hasNext()) {
			NetworkBookItem book = booksIterator.next();

			if (book.SeriesTitle != null) {
				final int seriesPosition = getSeriesIndex(book.SeriesTitle);
				if (seriesPosition == -1) {
					final int insertAt = subtrees().size();
					setSeriesIndex(book.SeriesTitle, insertAt);
					new NetworkBookTree(this, book, false);
				} else {
					FBTree treeAtSeriesPosition = subtrees().get(seriesPosition);
					if (treeAtSeriesPosition instanceof NetworkBookTree) {
						final NetworkBookTree bookTree = (NetworkBookTree) treeAtSeriesPosition;
						bookTree.removeSelf();
						final NetworkSeriesTree seriesTree = new NetworkSeriesTree(this, book.SeriesTitle, seriesPosition, false);
						new NetworkBookTree(seriesTree, bookTree.Book, false);
						treeAtSeriesPosition = seriesTree;
					}

					if (!(treeAtSeriesPosition instanceof NetworkSeriesTree)) {
						throw new RuntimeException("That's impossible!!!");
					}
					final NetworkSeriesTree seriesTree = (NetworkSeriesTree) treeAtSeriesPosition;
					ListIterator<FBTree> nodesIterator = seriesTree.subtrees().listIterator();
					int insertAt = 0;
					while (nodesIterator.hasNext()) {
						FBTree tree = nodesIterator.next();
						if (!(tree instanceof NetworkBookTree)) {
							throw new RuntimeException("That's impossible!!!");
						}
						NetworkBookTree bookTree = (NetworkBookTree) tree;
						if (bookTree.Book.IndexInSeries > book.IndexInSeries) {
							break;
						}
						++insertAt;
					}
					new NetworkBookTree(seriesTree, book, insertAt, false);
				}
			} else {
				new NetworkBookTree(this, book, false);
			}
		}

		myBooksNumber = books.size();
	}

	@Override
	protected String getStringId() {
		return "@Author:" + Author.DisplayName + ":" + Author.SortKey;
	}
}
