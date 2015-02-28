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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public class OPDSCatalogItem extends NetworkURLCatalogItem {
	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(OPDSNetworkLink link, NetworkItemsLoader loader) {
			super(link, loader);
		}
	}
	private State myLoadingState;
	private final Map<String,String> myExtraData;

	OPDSCatalogItem(OPDSNetworkLink link, RelatedUrlInfo info) {
		this(link, info.Title, null, createSimpleCollection(info.Url));
	}

	OPDSCatalogItem(OPDSNetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		this(link, title, summary, urls, Accessibility.ALWAYS, FLAGS_DEFAULT, null);
	}

	protected OPDSCatalogItem(OPDSNetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags, Map<String,String> extraData) {
		super(link, title, summary, urls, accessibility, flags);
		myExtraData = extraData;
	}

	private static UrlInfoCollection<UrlInfo> createSimpleCollection(String url) {
		final UrlInfoCollection<UrlInfo> collection = new UrlInfoCollection<UrlInfo>();
		collection.addInfo(new UrlInfo(UrlInfo.Type.Catalog, url, MimeType.APP_ATOM_XML));
		return collection;
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

	@Override
	public final void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException{
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)Link;

		myLoadingState = opdsLink.createOperationData(loader);

		doLoadChildren(
			opdsLink.createNetworkData(getCatalogUrl(), myLoadingState)
		);
	}

	@Override
	public final boolean supportsResumeLoading() {
		return true;
	}

	@Override
	public final boolean canResumeLoading() {
		return myLoadingState != null && myLoadingState.ResumeURI != null;
	}

	@Override
	public final void resumeLoading(NetworkItemsLoader loader) throws ZLNetworkException {
		if (canResumeLoading()) {
			myLoadingState.Loader = loader;
			ZLNetworkRequest networkRequest = myLoadingState.resume();
			doLoadChildren(networkRequest);
		}
	}
}
