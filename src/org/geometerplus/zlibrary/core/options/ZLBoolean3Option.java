/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

public final class ZLBoolean3Option extends ZLOption {
	private ZLBoolean3 myValue;
	private final ZLBoolean3 myDefaultValue;

	public ZLBoolean3Option(String group, String optionName, ZLBoolean3 defaultValue) {
		super(group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public ZLBoolean3 getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				myValue = ZLBoolean3.getByName(value);
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(ZLBoolean3 value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;

		if (myValue == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue(myValue.Name);
		}
	}
}
