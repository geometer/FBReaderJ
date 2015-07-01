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

package org.geometerplus.fbreader.book;

import org.geometerplus.zlibrary.core.util.ZLColor;

public class HighlightingStyle {
	public final int Id;
	public final long LastUpdateTimestamp;

	private String myName;
	private ZLColor myBackgroundColor;
	private ZLColor myForegroundColor;

	HighlightingStyle(int id, long timestamp, String name, ZLColor bgColor, ZLColor fgColor) {
		Id = id;
		LastUpdateTimestamp = timestamp;

		myName = name;
		myBackgroundColor = bgColor;
		myForegroundColor = fgColor;
	}

	public String getNameOrNull() {
		return "".equals(myName) ? null : myName;
	}

	void setName(String name) {
		myName = name;
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
