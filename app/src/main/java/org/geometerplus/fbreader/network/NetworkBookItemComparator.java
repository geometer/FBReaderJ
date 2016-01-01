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

package org.geometerplus.fbreader.network;

import java.util.Comparator;
import java.util.LinkedList;

public final class NetworkBookItemComparator implements Comparator<NetworkItem> {
	public int compare(NetworkItem item0, NetworkItem item1) {
		final boolean item0isABook = item0 instanceof NetworkBookItem;
		final boolean item1isABook = item1 instanceof NetworkBookItem;

		final String title0 = item0.Title.toString();
		final String title1 = item1.Title.toString();

		if (!item0isABook && !item1isABook) {
			return title0.compareTo(title1);
		}
		if (!item0isABook || !item1isABook) {
			return item0isABook ? 1 : -1;
		}

		final NetworkBookItem book0 = (NetworkBookItem)item0;
		final NetworkBookItem book1 = (NetworkBookItem)item1;

		final LinkedList<NetworkBookItem.AuthorData> authors0 = book0.Authors;
		final LinkedList<NetworkBookItem.AuthorData> authors1 = book1.Authors;

		final boolean authors0empty = authors0.size() == 0;
		final boolean authors1empty = authors1.size() == 0;

		if (authors0empty && !authors1empty) {
			return -1;
		}
		if (authors1empty && !authors0empty) {
			return 1;
		}
		if (!authors0empty && !authors1empty) {
			final int diff = authors0.get(0).SortKey.compareTo(authors1.get(0).SortKey);
			if (diff != 0) {
				return diff;
			}
		}

		/*if (book0.Index != book1.Index) {
			return book0.Index - book1.Index;
		}*/

		final boolean book0HasSeriesTitle = book0.SeriesTitle != null;
		final boolean book1HasSeriesTitle = book1.SeriesTitle != null;

		if (book0HasSeriesTitle && book1HasSeriesTitle) {
			final int comp = book0.SeriesTitle.compareTo(book1.SeriesTitle);
			if (comp != 0) {
				return comp;
			} else {
				final float diff = book0.IndexInSeries - book1.IndexInSeries;
				if (diff != 0) {
					return diff > 0 ? 1 : -1;
				}
			}
			return title0.compareTo(title1);
		}

		final String book0Key = book0HasSeriesTitle ? book0.SeriesTitle : title0;
		final String book1Key = book1HasSeriesTitle ? book1.SeriesTitle : title1;
		final int comp = book0Key.compareTo(book1Key);
		if (comp != 0) {
			return comp;
		}
		return book1HasSeriesTitle ? -1 : 1;
	}
}
