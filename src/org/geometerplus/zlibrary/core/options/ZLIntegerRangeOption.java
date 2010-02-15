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

public final class ZLIntegerRangeOption extends ZLOption {
	public final int MinValue;
	public final int MaxValue;

	private final int myDefaultValue;
	private int myValue;

	public ZLIntegerRangeOption(String group, String optionName, int minValue, int maxValue, int defaultValue) {
		super(group, optionName);
		MinValue = minValue;
		MaxValue = maxValue;
		if (defaultValue < MinValue) {
			defaultValue = MinValue;
		} else if (defaultValue > MaxValue) {
			defaultValue = MaxValue;
		}
		myDefaultValue = defaultValue;
		myValue = defaultValue;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					int intValue = Integer.parseInt(value);
					if (intValue < MinValue) {
						intValue = MinValue;
					} else if (intValue > MaxValue) {
						intValue = MaxValue;
					}
					myValue = intValue;
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(int value) {
		if (value < MinValue) {
			value = MinValue;
		} else if (value > MaxValue) {
			value = MaxValue;
		}
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (value == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue("" + value);
		}
	}
}
