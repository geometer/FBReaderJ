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

package org.geometerplus.fbreader.network.opds;

import java.util.Map;
import java.util.HashSet;

import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

import org.geometerplus.fbreader.network.*;

class OPDSCatalogItem extends NetworkCatalogItem {

	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(INetworkLink link, OnNewItemListener listener) {
			super(link, listener);
		}
	}
	private State myLoadingState;

	OPDSCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType) {
		super(link, title, summary, cover, urlByType);
	}

	OPDSCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, cover, urlByType, visibility);
	}

	OPDSCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility, int catalogType) {
		super(link, title, summary, cover, urlByType, visibility, catalogType);
	}

	private String doLoadChildren(NetworkOperationData.OnNewItemListener listener,
			ZLNetworkRequest networkRequest) {
		while (networkRequest != null) {
			final String errorMessage = ZLNetworkManager.Instance().perform(networkRequest);
			if (errorMessage != null) {
				myLoadingState = null;
				return errorMessage;
			}
			if (listener.confirmInterrupt()) {
				return null;
			}
			networkRequest = myLoadingState.resume();
		}
		return null;
	}

	@Override
	public final String loadChildren(NetworkOperationData.OnNewItemListener listener) {
		OPDSNetworkLink opdsLink = (OPDSNetworkLink) Link;

		myLoadingState = opdsLink.createOperationData(Link, listener);

		ZLNetworkRequest networkRequest =
			opdsLink.createNetworkData(URLByType.get(URL_CATALOG), myLoadingState);

		return doLoadChildren(listener, networkRequest);
	}

	@Override
	public final boolean supportsResumeLoading() {
		return true;
	}

	@Override
	public final String resumeLoading(NetworkOperationData.OnNewItemListener listener) {
		if (myLoadingState == null) {
			return null;
		}
		myLoadingState.Listener = listener;
		ZLNetworkRequest networkRequest = myLoadingState.resume();
		return doLoadChildren(listener, networkRequest);
	}
}
