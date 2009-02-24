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

final class ZLOptionID {
	private String myGroup = "";
	private String myName = "";

	public ZLOptionID(String group, String name) {
		if (group != null) {
			myGroup = group;
		}
		if (name != null) {
			myName = name;
		}
	}

	public String getGroup() {
		return myGroup;
	}

	public String getName() {
		return myName;
	}

	public int hashCode() {
		return myName.length();
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ZLOptionID)) {
			return false;
		}

		ZLOptionID arg = (ZLOptionID) o;

		if (arg.hashCode() != this.hashCode()) {
			return false;
		}

		return arg.myName.equals(myName) && arg.myGroup.equals(myGroup);

	}
}
