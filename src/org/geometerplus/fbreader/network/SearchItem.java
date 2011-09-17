/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

//import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.urlInfo.*;

public class SearchItem extends NetworkCatalogItem {
	private static String getInitialSummary(INetworkLink link) {
		if (link == null) {
			return NetworkLibrary.resource().getResource("searchSummaryAllCatalogs").getValue();
		} else {
			return NetworkLibrary.resource().getResource("searchSummary").getValue().replace("%s", link.getSiteName());
		}
	}

	public SearchItem(INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("search").getValue(),
			getInitialSummary(link),
			new UrlInfoCollection<UrlInfo>(),
			Accessibility.ALWAYS,
			FLAGS_DEFAULT
		);
	}

	public String getStringId() {
		return "@Search";
	}

	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
	}

	/*
	@Override
	protected String getCatalogUrl() {
		final StringBuilder builder = new StringBuilder();
		boolean flag = false;
		for (String bookId : Link.basket().bookIds()) {
			if (flag) {
				builder.append(',');
			} else {
				flag = true;
			}
			builder.append(bookId);
		}

		return ZLNetworkUtil.appendParameter(getUrl(UrlInfo.Type.Catalog), "ids", builder.toString());
	}
	*/
}
