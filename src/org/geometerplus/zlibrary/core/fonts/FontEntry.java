/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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
	private final String[] myFiles;

	public FontEntry(String family, String normal, String bold, String italic, String boldItalic) {
		Family = family;
		myFiles = new String[4];
		myFiles[0] = normal;
		myFiles[1] = bold;
		myFiles[2] = italic;
		myFiles[3] = boldItalic;
	}

	FontEntry(String family) {
		Family = family;
		myFiles = null;
	}

	public boolean isSystem() {
		return myFiles == null;
	}

	public String fileName(boolean bold, boolean italic) {
		return myFiles != null ? myFiles[(bold ? 1 : 0) + (italic ? 2 : 0)] : null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("FontEntry[");
		builder.append(Family);
		if (myFiles != null) {
			for (int i = 0; i < 4; ++i) {
				builder.append(";").append(myFiles[i]);
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
		return
			Family.equals(entry.Family) &&
			(myFiles == null ? entry.myFiles == null : myFiles.equals(entry.myFiles));
	}

	@Override
	public int hashCode() {
		return Family.hashCode();
	}
}
