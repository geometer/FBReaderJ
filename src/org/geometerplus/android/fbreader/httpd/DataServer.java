/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.httpd;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

public class DataServer extends NanoHTTPD {
	DataServer() {
		super(12345);
	}

	private static final String PREFIX_VIDEO = "/video/";

	@Override
	public Response serve(IHTTPSession session) {
		final Method method = session.getMethod();
		final String uri = session.getUri();
		if (uri.startsWith(PREFIX_VIDEO)) {
			final String encodedPath = uri.substring(PREFIX_VIDEO.length());
			// TODO: serve video from path
			String i = null;
			try {
				final StringBuilder path = new StringBuilder();
				for (String item : encodedPath.split("X")) {
					if (item.length() == 0) {
						continue;
					}
					i = item;
					path.append((char)Short.parseShort(item, 16));
				}
				final Response res = new Response(
					Response.Status.OK,
					MimeType.VIDEO_WEBM.toString(),
					ZLFile.createFileByPath(path.toString()).getInputStream()
				);
				res.addHeader("Accept-Ranges", "bytes");
				return res;
			} catch (Exception e) {
				return new Response(
					Response.Status.NOT_FOUND,
					MimeType.TEXT_HTML.toString(),
					"<html><body><h1>" + e.getMessage() + "</h1>\n(" + uri + ")\n(" + encodedPath + ")</body></html>"
				);
			}
		}
		return new Response(
			Response.Status.NOT_FOUND,
			MimeType.TEXT_HTML.toString(),
			"<html><body><h1>Not found: " + uri + "</h1></body></html>"
		);
	}
}
