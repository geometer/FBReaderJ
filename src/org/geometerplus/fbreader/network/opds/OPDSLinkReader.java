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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.*;


public class OPDSLinkReader {

	public static ICustomNetworkLink createCustomLink(int id, String siteName, String title, String summary, String icon, Map<String, String> links) {
		if (siteName == null || title == null || links.get(INetworkLink.URL_MAIN) == null) {
			return null;
		}
		return new OPDSCustomLink(id, siteName, title, summary, icon, links);
	}

	public static ICustomNetworkLink createCustomLinkWithoutInfo(String siteName, String url) {
		final HashMap<String, String> links = new HashMap<String, String>();
		links.put(INetworkLink.URL_MAIN, url);
		return new OPDSCustomLink(ICustomNetworkLink.INVALID_ID, siteName, null, null, null, links);
	}

	public static ZLNetworkRequest loadOPDSLinksRequest(String url, final NetworkLibrary.OnNewLinkListener listener) {
		return new ZLNetworkRequest(url) {
			@Override
			public String handleStream(URLConnection connection, InputStream inputStream) throws IOException {
				new OPDSLinkXMLReader(listener).read(inputStream);
				return null;
			}
		};
	}
}
