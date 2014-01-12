/*
 * Copyright (C) 2012-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.fb2;

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

abstract class FB2Util {
	static ZLFile getRealFB2File(ZLFile file) {
		final String name = file.getShortName().toLowerCase();
		if (name.endsWith(".fb2.zip") && file.isArchive()) {
			final List<ZLFile> children = file.children();
			if (children == null) {
				return null;
			}
			ZLFile candidate = null;
			for (ZLFile item : children) {
				if ("fb2".equals(item.getExtension())) {
					if (candidate == null) {
						candidate = item;
					} else {
						return null;
					}
				}
			}
			return candidate;
		} else {
			return file;
		}
	}
}
