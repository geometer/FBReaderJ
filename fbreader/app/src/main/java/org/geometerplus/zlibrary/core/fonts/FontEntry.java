/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.fonts;

import java.util.HashMap;
import java.util.Map;

import org.fbreader.util.ComparisonUtil;

public final class FontEntry {
	private static Map<String,FontEntry> ourSystemEntries = new HashMap<String,FontEntry>();

	public static FontEntry systemEntry(String family) {
		synchronized(ourSystemEntries) {
			FontEntry entry = ourSystemEntries.get(family);
			if (entry == null) {
				entry = new FontEntry(family);
				ourSystemEntries.put(family, entry);
			}
			return entry;
		}
	}

	public final String Family;
	private final FileInfo[] myFileInfos;

	public FontEntry(String family, FileInfo normal, FileInfo bold, FileInfo italic, FileInfo boldItalic) {
		Family = family;
		myFileInfos = new FileInfo[4];
		myFileInfos[0] = normal;
		myFileInfos[1] = bold;
		myFileInfos[2] = italic;
		myFileInfos[3] = boldItalic;
	}

	FontEntry(String family) {
		Family = family;
		myFileInfos = null;
	}

	public boolean isSystem() {
		return myFileInfos == null;
	}

	public FileInfo fileInfo(boolean bold, boolean italic) {
		return myFileInfos != null ? myFileInfos[(bold ? 1 : 0) + (italic ? 2 : 0)] : null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("FontEntry[");
		builder.append(Family);
		if (myFileInfos != null) {
			for (int i = 0; i < 4; ++i) {
				final FileInfo info = myFileInfos[i];
				builder.append(";").append(info != null ? info.Path : null);
			}
		}
		return builder.append("]").toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof FontEntry)) {
			return false;
		}
		final FontEntry entry = (FontEntry)other;
		if (!ComparisonUtil.equal(Family, entry.Family)) {
			return false;
		}
		if (myFileInfos == null) {
			return entry.myFileInfos == null;
		}
		if (entry.myFileInfos == null) {
			return false;
		}
		for (int i = 0; i < myFileInfos.length; ++i) {
			if (!ComparisonUtil.equal(myFileInfos[i], entry.myFileInfos[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return ComparisonUtil.hashCode(Family);
	}
}
