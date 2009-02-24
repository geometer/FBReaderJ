/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.xmlconfig;

import java.util.Set;
import java.util.HashMap;

final class ZLGroup {
	private final HashMap<String,ZLOptionInfo> myData = new HashMap<String,ZLOptionInfo>();

	Set<String> optionNames() {
		return myData.keySet();
	}

	ZLOptionInfo getOption(String name) {
		return myData.get(name);
	}

	String getValue(String name) {
		ZLOptionInfo info = myData.get(name);
		return (info != null) ? info.getValue() : null;
	}

	void setValue(String name, String value, String category) {
		ZLOptionInfo info = myData.get(name);
		if (info != null) {
			info.setValue(value);
			info.setCategory(category);
		} else {
			myData.put(name, new ZLOptionInfo(value, category));
		}
	}

	public void unsetValue(String name) {
		myData.remove(name);
	}
}
