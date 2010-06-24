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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.*;

public class NetworkTreeFactory {

	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkLibraryItem item) {
		return createNetworkTree(parent, item, -1);
	}

	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkLibraryItem item, int position) {
		final int subtreesSize = parent.subTrees().size();
		if (position == -1) {
			position = subtreesSize;
		} else if (position < 0 || position > subtreesSize) {
			throw new IndexOutOfBoundsException("`position` value equals " + position + " but must be in range [0; " + subtreesSize + "]");
		}

		if (item instanceof NetworkCatalogItem) {
			NetworkCatalogItem catalogItem = (NetworkCatalogItem) item;
			if (catalogItem.getVisibility() == ZLBoolean3.B3_FALSE) {
				return null;
			}
			NetworkCatalogTree tree = new NetworkCatalogTree(parent, catalogItem, position);
			catalogItem.onDisplayItem();
			return tree;
		} else if (item instanceof NetworkBookItem) {
			if (position != subtreesSize) {
				throw new RuntimeException("Unable to insert NetworkBookItem to the middle of the catalog");
			}

			final boolean showAuthors = parent.Item.CatalogType != NetworkCatalogItem.CATALOG_BY_AUTHORS;

			NetworkBookItem book = (NetworkBookItem) item;
			String seriesTitle = book.SeriesTitle;
			if (seriesTitle == null) {
				return new NetworkBookTree(parent, (NetworkBookItem) item, position, showAuthors);
			}

			if (position > 0) {
				final NetworkTree previous = (NetworkTree) parent.subTrees().get(position - 1);
				if (previous instanceof NetworkSeriesTree) {
					final NetworkSeriesTree seriesTree = (NetworkSeriesTree) previous;
					if (seriesTitle.equals(seriesTree.SeriesTitle)) {
						seriesTree.invalidateChildren(); // call to update secondString
						return new NetworkBookTree(seriesTree, book, showAuthors);
					}
				} else if (previous instanceof NetworkBookTree) {
					final NetworkBookTree bookTree = (NetworkBookTree) previous;
					final NetworkBookItem previousBook = bookTree.Book;
					if (seriesTitle.equals(previousBook.SeriesTitle)) {
						bookTree.removeSelf();
						final NetworkSeriesTree seriesTree = new NetworkSeriesTree(parent, seriesTitle, --position, showAuthors);
						new NetworkBookTree(seriesTree, previousBook, showAuthors);
						return new NetworkBookTree(seriesTree, book, showAuthors);
					}
				}
			}

			return new NetworkBookTree(parent, book, position, showAuthors);
		}
		return null;
	}
}
