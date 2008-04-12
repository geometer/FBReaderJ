/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

/**
 * класс ранжированная целочисленная опция. есть верхний и нижний пределы,
 * которые тут же и указываются.
 * 
 * @author Администратор
 * 
 */
public final class ZLIntegerRangeOption extends ZLOption {
	private final int myMinValue;
	private final int myMaxValue;

	private final int myDefaultValue;
	private int myValue;

	public ZLIntegerRangeOption(String category, String group, String optionName, int minValue, int maxValue, int defaultValue) {
		super(category, group, optionName);
		myMinValue = minValue;
		myMaxValue = maxValue;
		myDefaultValue = Math.max(myMinValue, Math.min(myMaxValue, defaultValue));
		myValue = myDefaultValue;
	}

	public int getMinValue() {
		return myMinValue;
	}

	public int getMaxValue() {
		return myMaxValue;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					int intValue = Integer.parseInt(value);
					if (intValue < myMinValue) {
						intValue = myMinValue;
					} else if (intValue > myMaxValue) {
						intValue = myMaxValue;
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
		value = Math.max(myMinValue, Math.min(myMaxValue, value));
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue("" + myValue);
		}
	}
}
