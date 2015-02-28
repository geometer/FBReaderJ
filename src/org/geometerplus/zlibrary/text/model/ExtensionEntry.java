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

package org.geometerplus.zlibrary.text.model;

import java.util.Map;

public class ExtensionEntry {
	public final String Type;
	public final Map<String,String> Data;

	ExtensionEntry(String type, Map<String,String> data) {
		Type = type;
		Data = data;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExtensionEntry)) {
			return false;
		}
		final ExtensionEntry entry = (ExtensionEntry)other;
		return Type.equals(entry.Type) && Data.equals(entry.Data);
	}

	@Override
	public int hashCode() {
		return Type.hashCode() + 23 * Data.hashCode();
	}
}
