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
		OperationData data = new OperationData(Link, listener);

		String urlString = URLByType.get(URL_CATALOG);
		if (urlString == null) {
			return null; // TODO: return error/information message???
		}

		try {
			while (data.ResumeCount < 10 // FIXME: hardcoded resume limit constant!!!
					&& urlString != null) {
				urlString = Link.rewriteUrl(urlString, false);
				final URL url = new URL(urlString);
				final URLConnection connection = url.openConnection();
				if (!(connection instanceof HttpURLConnection)) {
					return null; // TODO: return error/information message???
				}
				final HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setConnectTimeout(15000); // FIXME: hardcoded timeout value!!!
				httpConnection.setReadTimeout(30000); // FIXME: hardcoded timeout value!!!
				httpConnection.setRequestProperty("Connection", "Close");
				httpConnection.setRequestProperty("User-Agent", ZLNetworkUtil.getUserAgent());
				final int response = httpConnection.getResponseCode();
				if (response == HttpURLConnection.HTTP_OK) {
					InputStream inStream = httpConnection.getInputStream();
					try {
						final NetworkOPDSFeedReader feedReader = new NetworkOPDSFeedReader(urlString, data);
						final OPDSXMLReader xmlReader = new OPDSXMLReader(feedReader);
						xmlReader.read(inStream);
					} finally {
						inStream.close();
					}
				} else {
					return null; // return error???
				}

				urlString = data.ResumeURI;
				data.clear();
			}
		} catch (MalformedURLException ex) {
			// return error???
			return null;
		} catch (SocketTimeoutException ex) {
			return NetworkErrors.errorMessage("operationTimedOutMessage");
		} catch (IOException ex) {
			return NetworkErrors.errorMessage(NetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(urlString));
		}
		return null;
	}
}
