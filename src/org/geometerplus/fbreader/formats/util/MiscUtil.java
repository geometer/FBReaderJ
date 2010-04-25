/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.util;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class MiscUtil {
	public static String htmlDirectoryPrefix(ZLFile file) {
		String shortName = file.getName(false);
		String path = file.getPath();
		int index = -1;
		if ((path.length() > shortName.length()) &&
				(path.charAt(path.length() - shortName.length() - 1) == ':')) {
			index = shortName.lastIndexOf('/');
		}
		return path.substring(0, path.length() - shortName.length() + index + 1);
	}

	public static String archiveEntryName(String fullPath) {
		final int index = fullPath.lastIndexOf(':');
		return (index >= 2) ? fullPath.substring(index + 1) : fullPath;
	}

	private static boolean isHexDigit(char ch) {
		return
			(ch >= '0' && ch <= '9') ||
			(ch >= 'a' && ch <= 'f') ||
			(ch >= 'A' && ch <= 'F');
	}

	public static String decodeHtmlReference(String name) {
		int index = 0;
		while (true) {
			index = name.indexOf('%', index);
			if (index == -1 || index >= name.length() - 2) {
				break;
			}
			if (isHexDigit(name.charAt(index + 1)) &&
				isHexDigit(name.charAt(index + 2))) {
				char c = 0;
				try {
					c = (char)Integer.decode("0x" + name.substring(index + 1, index + 3)).intValue();
				} catch (NumberFormatException e) {
				}
				name = name.substring(0, index) + c + name.substring(index + 3);
			}
			index = index + 1;
		}
		return name;
	}
}
