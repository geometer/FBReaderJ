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

import java.util.*;
import java.io.*;
import java.net.*;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.network.*;

import org.geometerplus.fbreader.network.*;


class OPDSCatalogItem extends NetworkCatalogItem {

	OPDSCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType) {
		super(link, title, summary, cover, urlByType);
	}

	OPDSCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, cover, urlByType, visibility);
	}

	OPDSCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility, int catalogType) {
		super(link, title, summary, cover, urlByType, visibility, catalogType);
	}

	@Override
	public String loadChildren(CatalogListener listener) {
		String urlString = URLByType.get(URL_CATALOG);
		if (urlString == null) {
			return null; // TODO: return error/information message???
		}

		final OperationData data = new OperationData(Link, listener);

		String errorMessage = null;
		while (data.ResumeCount < 10 // FIXME: hardcoded resume limit constant!!!
				&& urlString != null && errorMessage == null) {

			urlString = Link.rewriteUrl(urlString, false);

			errorMessage = ZLNetworkManager.Instance().perform(new ZLNetworkRequest(urlString) {
				@Override
				public String handleStream(URLConnection connection, InputStream inputStream) throws IOException {
					new OPDSXMLReader(
						new NetworkOPDSFeedReader(URL, data)
					).read(inputStream);
					return null;
				}
			});

			urlString = data.ResumeURI;
			data.clear();
		}
		return errorMessage;
	}
}
