/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class DataUtil {
	static ZLFile fileFromEncodedPath(String encodedPath) {
		final StringBuilder path = new StringBuilder();
		for (String item : encodedPath.split("X")) {
			if (item.length() == 0) {
				continue;
			}
			path.append((char)Short.parseShort(item, 16));
		}
		return ZLFile.createFileByPath(path.toString());
	}

	public static String buildUrl(DataService.Connection connection, String prefix, String path) {
		final int port = connection.getPort();
		if (port == -1) {
			return null;
		}
		final StringBuilder url = new StringBuilder("http://127.0.0.1:").append(port)
			.append("/").append(prefix).append("/");
		for (int i = 0; i < path.length(); ++i) {
			url.append(String.format("X%X", (short)path.charAt(i)));
		}
		return url.toString();
	}
}
