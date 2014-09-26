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

package org.geometerplus.fbreader.book;

import java.util.Map;
import java.util.HashMap;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;

public class HighlightingStyle {
	public final int Id;

	private String myName;
	private ZLColor myBackgroundColor;
	private ZLColor myForegroundColor;

	HighlightingStyle(int id, String name, ZLColor bgColor, ZLColor fgColor) {
		Id = id;
		myName = name;
		myBackgroundColor = bgColor;
		myForegroundColor = fgColor;
	}

	private String defaultName() {
		return ZLResource.resource("style").getValue().replace("%s", String.valueOf(Id));
	}

	public String getName() {
		if (myName == null || "".equals(myName)) {
			return defaultName();
		}
		return myName;
	}

	public void setName(String name) {
		myName = defaultName().equals(name) ? "" : name;
	}

	public ZLColor getBackgroundColor() {
		return myBackgroundColor;
	}

	public void setBackgroundColor(ZLColor bgColor) {
		myBackgroundColor = bgColor;
	}

	public ZLColor getForegroundColor() {
		return myForegroundColor;
	}

	public void setForegroundColor(ZLColor fgColor) {
		myForegroundColor = fgColor;
	}
}
