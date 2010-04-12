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

import org.geometerplus.fbreader.network.*;

public class NetworkTreeFactory {

	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkLibraryItem item) {
		return createNetworkTree(parent, item, -1);
	}

	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkLibraryItem item, int position) {
		if (item instanceof NetworkCatalogItem) {
			NetworkCatalogItem catalogItem = (NetworkCatalogItem) item;
			if (!NetworkCatalogTree.processAccountDependent(catalogItem)) {
				return null;
			}
			NetworkCatalogTree tree = new NetworkCatalogTree(parent, catalogItem, position);
			catalogItem.onDisplayItem();
			return tree;
		} else if (item instanceof NetworkBookItem) {
			return new NetworkBookTree(parent, (NetworkBookItem) item, position);
		}
		return null;
	}

	//static void createSubtrees(SearchResultTree parent, NetworkBookCollection books);

	/*public static void fillAuthorNode(NetworkTree parent, List<NetworkLibraryItem> books) {
		NetworkSeriesTree seriesTree = null;

		boolean showAuthors = !(parent instanceof NetworkAuthorTree)
			&& !(parent instanceof NetworkCatalogTree && ((NetworkCatalogTree)parent).Item.CatalogType == NetworkCatalogItem.CATALOG_BY_AUTHORS);

		ListIterator<NetworkLibraryItem> it = books.listIterator();
		while (it.hasNext()) {
			NetworkLibraryItem item = it.next();
			if (!(item instanceof NetworkBookItem)) {
				continue;
			}
			final NetworkBookItem book = (NetworkBookItem) item;
			String seriesTitle = book.SeriesTitle;

			if (seriesTitle != null && seriesTitle.length() > 0 && (seriesTree == null || !seriesTitle.equals(seriesTree.SeriesTitle))) {
				ListIterator<NetworkLibraryItem> jt = books.listIterator(it.nextIndex());
				NetworkBookItem next = null;
				while (jt.hasNext()) {
					final NetworkLibraryItem jtem = jt.next();
					if (jtem instanceof NetworkBookItem) {
						next = (NetworkBookItem) jtem;
						break;
					}
				}
				if (next == null) {
					seriesTitle = null;
				} else if (!seriesTitle.equals(next.SeriesTitle)) {
					seriesTitle = null;
				}
			}

			if (seriesTitle == null || seriesTitle.length() == 0) {
				seriesTree = null;
				new NetworkBookTree(parent, book, -1);
			} else {
				if (seriesTree == null || !seriesTree.SeriesTitle.equals(seriesTitle)) {
					seriesTree = new NetworkSeriesTree(parent, seriesTitle, showAuthors);
				}
				new NetworkBookTree(seriesTree, book, -1);
			}
		}
	}*/
}
