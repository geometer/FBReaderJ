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

import org.fbreader.util.Boolean3;

import org.geometerplus.fbreader.network.*;

class NetworkTreeFactory {
	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkItem item) {
		return createNetworkTree(parent, item, -1);
	}

	public static NetworkTree createNetworkTree(NetworkCatalogTree parent, NetworkItem item, int position) {
		final int subtreesSize = parent.subtrees().size();
		if (position == -1) {
			position = subtreesSize;
		} else if (position < 0 || position > subtreesSize) {
			throw new IndexOutOfBoundsException("`position` value equals " + position + " but must be in range [0; " + subtreesSize + "]");
		}

		if (item instanceof NetworkCatalogItem) {
			final NetworkCatalogItem catalogItem = (NetworkCatalogItem)item;
			if (catalogItem.getVisibility() == Boolean3.FALSE) {
				return null;
			}
			return new NetworkCatalogTree(parent, parent.getLink(), catalogItem, position);
		} else if (item instanceof NetworkBookItem) {
			if (position != subtreesSize) {
				throw new RuntimeException("Unable to insert NetworkBookItem to the middle of the catalog");
			}

			final NetworkBookItem book = (NetworkBookItem)item;
			final int flags = parent.Item.getFlags();
			final boolean showAuthors = (flags & NetworkCatalogItem.FLAG_SHOW_AUTHOR) != 0;

			switch (flags & NetworkCatalogItem.FLAGS_GROUP) {
				default:
					return new NetworkBookTree(parent, book, position, showAuthors);
				case NetworkCatalogItem.FLAG_GROUP_BY_SERIES:
					if (book.SeriesTitle == null) {
						return new NetworkBookTree(parent, book, position, showAuthors);
					} else {
						final NetworkTree previous = position > 0
							? (NetworkTree)parent.subtrees().get(position - 1) : null;
						NetworkSeriesTree seriesTree = null;
						if (previous instanceof NetworkSeriesTree) {
							seriesTree = (NetworkSeriesTree)previous;
							if (!book.SeriesTitle.equals(seriesTree.SeriesTitle)) {
								seriesTree = null;
							}
						}
						if (seriesTree == null) {
							seriesTree = new NetworkSeriesTree(
								parent, book.SeriesTitle, position, showAuthors
							);
						}
						return new NetworkBookTree(seriesTree, book, showAuthors);
					}
				case NetworkCatalogItem.FLAG_GROUP_MORE_THAN_1_BOOK_BY_SERIES:
					if (position > 0 && book.SeriesTitle != null) {
						final NetworkTree previous =
							(NetworkTree)parent.subtrees().get(position - 1);
						if (previous instanceof NetworkSeriesTree) {
							final NetworkSeriesTree seriesTree = (NetworkSeriesTree)previous;
							if (book.SeriesTitle.equals(seriesTree.SeriesTitle)) {
								return new NetworkBookTree(seriesTree, book, showAuthors);
							}
						} else /* if (previous instanceof NetworkBookTree) */ {
							final NetworkBookItem previousBook = ((NetworkBookTree)previous).Book;
							if (book.SeriesTitle.equals(previousBook.SeriesTitle)) {
								previous.removeSelf();
								final NetworkSeriesTree seriesTree = new NetworkSeriesTree(
									parent, book.SeriesTitle, position - 1, showAuthors
								);
								new NetworkBookTree(seriesTree, previousBook, showAuthors);
								return new NetworkBookTree(seriesTree, book, showAuthors);
							}
						}
					}
					return new NetworkBookTree(parent, book, position, showAuthors);
				case NetworkCatalogItem.FLAG_GROUP_BY_AUTHOR:
					if (book.Authors.isEmpty()) {
						return new NetworkBookTree(parent, book, position, showAuthors);
					} else {
						final NetworkBookItem.AuthorData author = book.Authors.get(0);
						final NetworkTree previous = position > 0
							? (NetworkTree)parent.subtrees().get(position - 1) : null;
						NetworkAuthorTree authorTree = null;
						if (previous instanceof NetworkAuthorTree) {
							authorTree = (NetworkAuthorTree)previous;
							if (!author.equals(authorTree.Author)) {
								authorTree = null;
							}
						}
						if (authorTree == null) {
							authorTree = new NetworkAuthorTree(parent, author);
						}
						return new NetworkBookTree(authorTree, book, showAuthors);
					}
			}
		} else if (item instanceof TopUpItem) {
			return new TopUpTree(parent, (TopUpItem)item);
		}
		return null;
	}
}
