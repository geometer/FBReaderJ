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

package org.geometerplus.zlibrary.core.options;

import org.geometerplus.zlibrary.core.util.ZLColor;

public final class ZLColorOption extends ZLOption {
	private ZLColor myValue;
	private String myStringValue;

	private static String stringColorValue(ZLColor color) {
		return String.valueOf(color != null ? color.intValue() : -1);
	}

	public ZLColorOption(String group, String optionName, ZLColor defaultValue) {
		super(group, optionName, stringColorValue(defaultValue));
	}

	public ZLColor getValue() {
		final String stringValue = getConfigValue();
		if (!stringValue.equals(myStringValue)) {
			myStringValue = stringValue;
			try {
				final int intValue = Integer.parseInt(stringValue);
				myValue = intValue != -1 ? new ZLColor(intValue) : null;
			} catch (NumberFormatException e) {
			}
		}
		return myValue;
	}

	public void setValue(ZLColor value) {
		if (value == null) {
			return;
		}
		myValue = value;
		myStringValue = stringColorValue(value);
		setConfigValue(myStringValue);
	}
}
