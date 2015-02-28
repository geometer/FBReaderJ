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

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public class SingleCatalogSearchItem extends SearchItem {
	public SingleCatalogSearchItem(INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("search").getResource("summary").getValue().replace("%s", link.getShortName())
		);
	}

	@Override
	public void runSearch(ZLNetworkContext nc, NetworkItemsLoader loader, String pattern) throws ZLNetworkException {
		final NetworkOperationData data = Link.createOperationData(loader);
		ZLNetworkRequest request = Link.simpleSearchRequest(pattern, data);
		// TODO: possible infinite loop, use "continue link" instead
		while (request != null) {
			nc.perform(request);
			if (loader.confirmInterruption()) {
				return;
			}
			request = data.resume();
		}
	}

	@Override
	public MimeType getMimeType() {
		final UrlInfo info = Link.getUrlInfo(UrlInfo.Type.Search);
		return info != null ? info.Mime : MimeType.NULL;
	}

	@Override
	public String getUrl(String pattern) {
		final UrlInfo info = Link.getUrlInfo(UrlInfo.Type.Search);
		return info != null && info.Url != null ? info.Url.replace("%s", pattern) : null;
	}
}
