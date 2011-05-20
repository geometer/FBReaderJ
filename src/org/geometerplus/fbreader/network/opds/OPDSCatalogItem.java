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

import java.util.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSCatalogItem extends NetworkURLCatalogItem {
	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(OPDSNetworkLink link, OnNewItemListener listener) {
			super(link, listener);
		}
	}
	private State myLoadingState;
	private final Map<String,String> myExtraData;

	OPDSCatalogItem(OPDSNetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Map<String,String> extraData) {
		super(link, title, summary, urls);
		myExtraData = extraData;
	}

	protected OPDSCatalogItem(OPDSNetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags) {
		super(link, title, summary, urls, accessibility, flags);
		myExtraData = null;
	}

	private static UrlInfoCollection<UrlInfo> createSimpleCollection(String url) {
		final UrlInfoCollection<UrlInfo> collection = new UrlInfoCollection<UrlInfo>();
		collection.addInfo(new UrlInfo(UrlInfo.Type.Catalog, url));
		return collection;
	}

	OPDSCatalogItem(OPDSNetworkLink link, RelatedUrlInfo info) {
		super(link, info.Title, null, createSimpleCollection(info.Url));
		myExtraData = null;
	}

	private void doLoadChildren(ZLNetworkRequest networkRequest) throws ZLNetworkException {
		try {
			super.doLoadChildren(myLoadingState, networkRequest);
		} catch (ZLNetworkException e) {
			myLoadingState = null;
			throw e;
		}
	}

	@Override
	public final Map<String,String> extraData() {
		return myExtraData;
	}

	protected String getCatalogUrl() {
		return getUrl(UrlInfo.Type.Catalog);
	}

	@Override
	public final void loadChildren(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		OPDSNetworkLink opdsLink = (OPDSNetworkLink) Link;

		myLoadingState = opdsLink.createOperationData(listener);

		ZLNetworkRequest networkRequest =
			opdsLink.createNetworkData(getCatalogUrl(), myLoadingState);

		doLoadChildren(networkRequest);
	}

	@Override
	public final boolean supportsResumeLoading() {
		return true;
	}

	@Override
	public final void resumeLoading(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		if (myLoadingState != null) {
			myLoadingState.Listener = listener;
			ZLNetworkRequest networkRequest = myLoadingState.resume();
			doLoadChildren(networkRequest);
		}
	}
}
