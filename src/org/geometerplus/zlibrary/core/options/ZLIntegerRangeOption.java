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

public final class ZLIntegerRangeOption extends ZLOption {
	private static int valueInRange(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}

	public final int MinValue;
	public final int MaxValue;

	private int myValue;
	private String myStringValue;

	public ZLIntegerRangeOption(String group, String optionName, int minValue, int maxValue, int defaultValue) {
		super(group, optionName, String.valueOf(valueInRange(defaultValue, minValue, maxValue)));
		MinValue = minValue;
		MaxValue = maxValue;
	}

	public int getValue() {
		final String stringValue = getConfigValue();
		if (!stringValue.equals(myStringValue)) {
			myStringValue = stringValue;
			try {
				myValue = valueInRange(Integer.parseInt(stringValue), MinValue, MaxValue);
			} catch (NumberFormatException e) {
			}
		}
		return myValue;
	}

	public void setValue(int value) {
		value = valueInRange(value, MinValue, MaxValue);
		myValue = value;
		myStringValue = String.valueOf(value);
		setConfigValue(myStringValue);
	}
}
