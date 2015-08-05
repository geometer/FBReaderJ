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

package org.geometerplus.fbreader.network.rss;

import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.atom.ATOMId;

public class RSSChannelHandler extends AbstractRSSChannelHandler {
	private final NetworkCatalogItem myCatalog;
	private final String myBaseURL;
	private final RSSCatalogItem.State myData;

	private int myIndex;
	private String mySkipUntilId;
	private boolean myFoundNewIds;

	/**
	 * Creates new RSSChannelHandler instance that can be used to get NetworkItem objects from RSS feeds.
	 *
	 * @param baseURL    string that contains URL of the RSS feed, that will be read using this instance of the reader
	 * @param result     network results buffer. Must be created using RSSNetworkLink corresponding to the RSS feed,
	 *                   that will be read using this instance of the reader.
	 */
	RSSChannelHandler(String baseURL, RSSCatalogItem.State result) {
		myCatalog = result.Loader.Tree.Item;
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		myFoundNewIds = mySkipUntilId != null;
		if (!(result.Link instanceof RSSNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of OPDSNetworkLink class.");
		}

	}

	@Override
	public void processFeedStart() {
	}

	@Override
	public boolean processFeedMetadata(RSSChannelMetadata feed,
			boolean beforeEntries) {
		return false;
	}

	@Override
	public boolean processFeedEntry(RSSItem entry) {

		if (entry.Id == null) {
			entry.Id = new ATOMId();
			entry.Id.Uri = "id_"+myIndex;
		}

		myData.LastLoadedId = entry.Id.Uri;
		if (!myFoundNewIds && !myData.LoadedIds.contains(entry.Id.Uri)) {
			myFoundNewIds = true;
		}
		myData.LoadedIds.add(entry.Id.Uri);

		NetworkItem item = new RSSBookItem((RSSNetworkLink)myData.Link, entry, myBaseURL, myIndex++);

		if (item != null) {
			myData.Loader.onNewItem(item);
		}
		return false;
	}

	@Override
	public void processFeedEnd() {
	}

}
