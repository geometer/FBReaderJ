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

package org.geometerplus.zlibrary.core.xml;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;

// optimized partially implemented map String -> String
// key must be interned
// there is no remove() in this implementation
// put with the same key does not remove old entry

public final class ZLStringMap {
	private String[] myKeys;
	private String[] myValues;
	private int mySize;

	public ZLStringMap() {
		myKeys = new String[8];
		myValues = new String[8];
	}

	public void put(String key, String value) {
		final int size = mySize++;
		String[] keys = myKeys;
		if (keys.length == size) {
			keys = ZLArrayUtils.createCopy(keys, size, size << 1);
			myKeys = keys;
			myValues = ZLArrayUtils.createCopy(myValues, size, size << 1);
		}
		keys[size] = key;
		myValues[size] = value;
	}

	/*
	 * Parameter `key` must be an interned string.
	 */
	public String getValue(String key) {
		int index = mySize;
		if (index > 0) {
			final String[] keys = myKeys;
			while (--index >= 0) {
				if (keys[index] == key) {
					return myValues[index];
				}
			}
		}
		return null;
	}

	public int getSize() {
		return mySize;
	}

	public String getKey(int index) {
		return myKeys[index];
	}

	public void clear() {
		mySize = 0;
	}
}
