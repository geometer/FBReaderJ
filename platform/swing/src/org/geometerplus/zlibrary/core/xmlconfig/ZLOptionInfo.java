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

final class ZLOptionInfo {
	private String myValue;
	private String myCategory;
	//private String myName;

	/*
	ZLOptionInfo(String name, String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
		myName = (name != null) ? name : "";
	}
	*/
	
	ZLOptionInfo(String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
	}

	void setValue(String value) {
		if (value != null) {
			myValue = value;
		}
	}
	
	void setCategory(String category) {
		if (category != null) {
			myCategory = category;
		}
	}
	
	String getValue() {
		return myValue;
	}

	String getCategory() {
		return myCategory;
	}

	/*
	public String toXML(String name) {
		return "    <option name=\"" + myName + "\" value=\"" + myValue + "\"/>\n";
	}
	*/
	
	public String toXML(String name) {
		return "    <option name=\"" + name + "\" value=\"" + myValue + "\"/>\n";
	}
}
