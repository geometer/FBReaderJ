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

import java.util.*;

final class ZLDeletedValuesSet {
	
	private final HashSet<ZLOptionID> myData = new LinkedHashSet<ZLOptionID>();

	public void add(String group, String name) {
		myData.add(new ZLOptionID(group, name));
	}

	public Set<String> getGroups() {
		HashSet<String> temp = new HashSet<String>();
		for (ZLOptionID option : myData) {
			temp.add(option.getGroup());
		}
		return temp;
	}
	
	public boolean contains(String group, String name) {
		for (ZLOptionID option : myData) {
			if (option.getName().equals(name) 
					&& option.getGroup().equals(group)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<ZLOptionID> getAll() {
		return Collections.unmodifiableSet(myData);
	}

	public void clear() {
		myData.clear();
	}
}
