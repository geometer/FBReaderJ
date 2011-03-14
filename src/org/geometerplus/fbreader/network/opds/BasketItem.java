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

package org.geometerplus.fbreader.network.opds;

import java.util.Map;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.opds.OPDSCatalogItem;

public class BasketItem extends OPDSCatalogItem {
	BasketItem(OPDSNetworkLink link, String title, String summary, String cover, Map<Integer,String> urlByType, Accessibility accessibility) {
		super(link, title, summary, cover, urlByType, accessibility, FLAGS_DEFAULT & ~FLAGS_GROUP);
		link.setSupportsBasket();
	}

	@Override
	protected String getUrl() {
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

		return ZLNetworkUtil.appendParameter(URLByType.get(URL_CATALOG), "ids", builder.toString());
	}
}
