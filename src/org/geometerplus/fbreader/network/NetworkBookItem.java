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


public class NetworkBookItem extends NetworkLibraryItem {

	public static class AuthorData implements Comparable<AuthorData> {
		public final String DisplayName;
		public final String SortKey;

		/**
		 * Creates new AuthorData instance. 
		 *
		 * @param displayName author's name. Must be not <code>null</code>.
		 * @param sortKey     string that defines sorting order of book's authors. Must be not <code>null</code>.
		 */
		public AuthorData(String displayName, String sortKey) {
			DisplayName = displayName;
			SortKey = sortKey;
		}

		@Override
		public int compareTo(AuthorData data) {
			int key = SortKey.compareTo(data.SortKey);
			if (key != 0) {
				return key;
			}
			return DisplayName.compareTo(data.DisplayName);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AuthorData)) {
				return false;
			}
			AuthorData data = (AuthorData) o;
			return SortKey == data.SortKey && DisplayName == data.DisplayName;
		}

		@Override
		public int hashCode() {
			return SortKey.hashCode() + DisplayName.hashCode();
		}
	}

	public int Index;

	public final String Id;
	public final String Language;
	public final String Date;
	// number with curency code (see http://en.wikipedia.org/wiki/List_of_circulating_currencies for example)
	public final String Price;
	public final List<AuthorData> Authors;
	public final List<String> Tags;
	public final String SeriesTitle;
	public final int IndexInSeries;


	/**
	 * Creates new NetworkLibraryItem instance.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param id            string that uniquely identifies this book item. Must be not <code>null</code>.
	 * @param index         sequence number of this book in corresponding catalog
	 * @param title         title of this book. Must be not <code>null</code>.
	 * @param summary       description of this book. Can be <code>null</code>.
	 * @param langage       string specifies language of this book. Can be <code>null</code>.
	 * @param date          string specifies release date of this book. Can be <code>null</code>.
	 * @param price         string specifies price of this book. Can be <code>null</code>.
	 * @param authors       list of book authors. Should contain at least one author.
	 * @param tags          list of book tags. Must be not <code>null</code> (can be empty).
	 * @param seriesTitle   title of this book's series. Can be <code>null</code>.
	 * @param indexInSeries	sequence number of this book within book's series. Ignored if seriesTitle is <code>null</code>.
	 * @param urlByType     map contains URLs and their types. Must be not <code>null</code>.
	 */
	public NetworkBookItem(NetworkLink link, String id, int index, 
		String title, String summary, String language, String date, String price,
		List<AuthorData> authors, List<String> tags, String seriesTitle, int indexInSeries,
		Map<Integer, String> urlByType) {
		super(link, title, summary, urlByType);
		Index = index;
		Id = id;
		Language = language;
		Date = date;
		Price = price;
		Authors = new LinkedList<AuthorData>(authors);
		Tags = new LinkedList<String>(tags);
		SeriesTitle = seriesTitle;
		IndexInSeries = indexInSeries;
	}

}
