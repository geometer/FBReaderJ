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

package org.geometerplus.zlibrary.core.network;

import java.util.*;
import java.net.CookieHandler;
import java.net.URI;
import java.io.IOException;

public class ZLCookieManager extends CookieHandler {
	private final Set<Cookie> myAllCookies = new HashSet<Cookie>();

	@Override
	public Map<String,List<String>> get(URI uri, Map<String,List<String>> requestHeaders) throws IOException {
		final StringBuilder builder = new StringBuilder();
		// TODO: compare cookies with the same name, select best matching
		for (Cookie c : getCookies(uri)) {
			if (builder.length() > 0) {
				builder.append("; ");
			}
			builder.append(c.Name);
			builder.append("=");
			builder.append(c.Value);
		}
		if (builder.length() == 0) {
			return Collections.emptyMap();
		}
		System.err.println("Cookie = " + builder.toString());
		return Collections.singletonMap("Cookie", Collections.singletonList(builder.toString()));
	}

	private List<Cookie> getCookies(URI uri) {
		List<Cookie> list = null;
		for (Cookie c : myAllCookies) {
			if (c.isApplicable(uri)) {
				if (list == null) {
					list = new LinkedList<Cookie>();
				}
				list.add(c);
			}
		}
		return list != null ? list : Collections.<Cookie>emptyList();
	}

	@Override
	public void put(URI uri, Map<String,List<String>> responseHeaders) throws IOException {
		addCookies(uri, responseHeaders.get("Set-Cookie"));
		addCookies(uri, responseHeaders.get("Set-Cookie2"));
	}

	private void addCookies(URI uri, List<String> setCookiesList) {
		if (setCookiesList != null) {
			for (String s : setCookiesList) {
				final Cookie c = Cookie.create(uri, s);
				if (c != null) {
					myAllCookies.add(c);
				}
			}
		}
	}
}
