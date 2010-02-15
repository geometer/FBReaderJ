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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

public final class ZLBoolean3Option extends ZLOption implements ZLSimpleOption {
	private int myValue;
	private final int myDefaultValue;

	public ZLBoolean3Option(String group, String optionName, int defaultValue) {
		super(group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public int getType() {
		return Type.BOOLEAN3;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				myValue = ZLBoolean3.getByString(value);
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(int value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;

		if (myValue == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue(ZLBoolean3.getName(myValue));
		}
	}
}
