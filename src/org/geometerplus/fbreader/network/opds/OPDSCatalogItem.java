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

import org.geometerplus.fbreader.network.*;


class OPDSCatalogItem extends NetworkCatalogItem {

	OPDSCatalogItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType) {
		super(link, title, summary, urlByType);
	}

	OPDSCatalogItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, urlByType, visibility);
	}

	OPDSCatalogItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType, int visibility, int catalogType) {
		super(link, title, summary, urlByType, visibility, catalogType);
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
				final URL url = new URL(urlString);
				final URLConnection connection = url.openConnection();
				if (!(connection instanceof HttpURLConnection)) {
					break;
				}
				final HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setConnectTimeout(15000); // FIXME: hardcoded timeout value!!!
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
			try {
				return NetworkErrors.errorMessage(NetworkErrors.ERROR_SOMETHING_WRONG, new URL(urlString).getHost());
			} catch (MalformedURLException ex2) {
				// return error???
				return null;
			}
		}
		return null;
	}
}
