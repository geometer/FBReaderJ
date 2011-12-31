/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public class SingleCatalogSearchItem extends SearchItem {
	public SingleCatalogSearchItem(INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("search").getResource("summary").getValue().replace("%s", link.getSiteName())
		);
	}

	@Override
	public void runSearch(NetworkItemsLoader loader, String pattern) throws ZLNetworkException {
		final NetworkOperationData data = Link.createOperationData(loader);
		ZLNetworkRequest request = Link.simpleSearchRequest(pattern, data);

		while (request != null) {
			ZLNetworkManager.Instance().perform(request);
			if (loader.confirmInterruption()) {
				return;
			}
			request = data.resume();
		}
	}
}
