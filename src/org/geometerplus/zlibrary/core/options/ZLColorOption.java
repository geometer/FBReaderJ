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

package org.geometerplus.zlibrary.core.options;

import org.geometerplus.zlibrary.core.util.ZLColor;

public final class ZLColorOption extends ZLOption {
	private final ZLColor myDefaultValue;
	private ZLColor myValue;

	public ZLColorOption(String group, String optionName, ZLColor defaultValue) {
		super(group, optionName);
		myDefaultValue = (defaultValue != null) ? defaultValue : new ZLColor(0);
		myValue = myDefaultValue;
	}

	public ZLColor getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					int intValue = Integer.parseInt(value);
					if (myValue.getIntValue() != intValue) {
						myValue = new ZLColor(intValue);
					}
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(ZLColor colorValue) {
		if (colorValue != null) {
			final boolean sameValue = myValue.equals(colorValue);
			if (myIsSynchronized && sameValue) {
				return;
			}
			if (!sameValue) {
				myValue = colorValue;
			}
			myIsSynchronized = true;
			if (colorValue.equals(myDefaultValue)) {
				unsetConfigValue();
			} else {
				setConfigValue("" + colorValue.getIntValue());
			}
		}
	}
}
