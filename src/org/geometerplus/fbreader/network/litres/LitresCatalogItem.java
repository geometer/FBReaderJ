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

import java.util.HashSet;
import java.util.Map;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitresCatalogItem extends NetworkURLCatalogItem {
	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(LitresNetworkLink link, NetworkItemsLoader loader) {
			super(link, loader);
		}
	}
	protected State myLoadingState;
	
	protected LitresCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		this(link, title, summary, urls, Accessibility.SIGNED_IN, FLAGS_DEFAULT, null);
	}
	
	protected LitresCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags, Map<String,String> extraData) {
		super(link, title, summary, urls, accessibility, flags);
	}

	protected void doLoadChildren(ZLNetworkRequest networkRequest) throws ZLNetworkException {
		try {
			super.doLoadChildren(myLoadingState, networkRequest);
		} catch (ZLNetworkException e) {
			myLoadingState = null;
			throw e;
		}
	}
	@Override
	public void loadChildren(NetworkItemsLoader loader)
			throws ZLNetworkException {
		
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;

		myLoadingState = litresLink.createOperationData(loader);
		UrlInfo info = myURLs.getInfo(UrlInfo.Type.Catalog);
		if(info != null)
		doLoadChildren(
				litresLink.createNetworkData(info.Url, MimeType.APP_LITRES_XML, myLoadingState)
		);
	}

	@Override
	public final boolean supportsResumeLoading() {
		return true;
	}

	@Override
	public final void resumeLoading(NetworkItemsLoader loader) throws ZLNetworkException {
		if (myLoadingState != null) {
			myLoadingState.Loader = loader;
			ZLNetworkRequest networkRequest = myLoadingState.resume();
			doLoadChildren(networkRequest);
		}
	}
}
