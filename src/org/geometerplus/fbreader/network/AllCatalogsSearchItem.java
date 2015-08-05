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

import java.util.LinkedList;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public class AllCatalogsSearchItem extends SearchItem {
	private final NetworkLibrary myLibrary;

	public AllCatalogsSearchItem(NetworkLibrary library) {
		super(
			null,
			NetworkLibrary.resource().getResource("search").getResource("summaryAllCatalogs").getValue()
		);
		myLibrary = library;
	}

	@Override
	public void runSearch(ZLNetworkContext nc, NetworkItemsLoader loader, String pattern) throws ZLNetworkException {
		final LinkedList<ZLNetworkRequest> requestList = new LinkedList<ZLNetworkRequest>();
		final LinkedList<NetworkOperationData> dataList = new LinkedList<NetworkOperationData>();

		boolean containsCyrillicLetters = false;
		for (char c : pattern.toLowerCase().toCharArray()) {
			if ("абвгдеёжзийклмнопрстуфхцчшщъыьэюя".indexOf(c) >= 0) {
				containsCyrillicLetters = true;
				break;
			}
		}
		for (INetworkLink link : myLibrary.activeLinks()) {
			if (containsCyrillicLetters) {
				if ("ebooks.qumran.org".equals(link.getHostName())) {
					continue;
				}
			}
			final NetworkOperationData data = link.createOperationData(loader);
			final ZLNetworkRequest request = link.simpleSearchRequest(pattern, data);
			if (request != null) {
				dataList.add(data);
				requestList.add(request);
			}
		}

		while (!requestList.isEmpty()) {
			nc.perform(requestList);

			requestList.clear();

			if (loader.confirmInterruption()) {
				return;
			}
			for (NetworkOperationData data : dataList) {
				ZLNetworkRequest request = data.resume();
				if (request != null) {
					requestList.add(request);
				}
			}
		}
	}

	@Override
	public MimeType getMimeType() {
		return MimeType.APP_ATOM_XML;
	}

	@Override
	public String getUrl(String pattern) {
		return null;
	}
}
