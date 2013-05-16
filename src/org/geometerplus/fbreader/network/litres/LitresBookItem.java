/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.atom.ATOMCategory;
import org.geometerplus.fbreader.network.litres.readers.LitresBookEntry;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public class LitresBookItem extends NetworkBookItem {

	LitresBookItem(INetworkLink link, String id, int index,
			CharSequence title, CharSequence summary, List<AuthorData> authors,
			List<String> tags, String seriesTitle, float indexInSeries,
			UrlInfoCollection<?> urls) {
		super(link, id, index, title, summary, authors, tags, seriesTitle,
				indexInSeries, urls);
	}
	
	protected LitresBookItem(INetworkLink link, LitresBookEntry entry, String baseUrl, int index) {
		this(
			link, entry.Id.Uri, index,
			entry.Title, getAnnotation(entry),
			entry.myAuthors, getTags(entry),
			entry.SeriesTitle, entry.SeriesIndex,
			getUrls((LitresNetworkLink)link, entry)
		);
	}
	 
	private static List<String> getTags(LitresBookEntry entry) {
			final LinkedList<String> tags = new LinkedList<String>();
			for (ATOMCategory category : entry.Categories) {
				String label = category.getLabel();
				if (label == null) {
					label = category.getTerm();
				}
				if (label != null) {
					tags.add(label);
				}
			}
			return tags;
	}
	 
	private static CharSequence getAnnotation(LitresBookEntry entry) {
			if (entry.Content != null) {
				return entry.Content;
			}
			if (entry.Summary != null) {
				return entry.Summary;
			}
			return null;
	}
	
	private static UrlInfoCollection<UrlInfo> getUrls(LitresNetworkLink networkLink, LitresBookEntry entry) {
		return entry.getUrls();
	}
}
