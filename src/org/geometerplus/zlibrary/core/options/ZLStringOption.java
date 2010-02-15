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

public final class ZLStringOption extends ZLOption implements ZLSimpleOption {
	private final String myDefaultValue;
	private String myValue;

	public ZLStringOption(String group, String optionName, String defaultValue) {
		super(group, optionName);
		myDefaultValue = (defaultValue != null) ? defaultValue.intern() : "";
		myValue = myDefaultValue;
	}

	public int getType() {
		return Type.STRING;
	}

	public String getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(myDefaultValue);
			if (value != null) {
				myValue = value;
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(String value) {
		if (value == null) {
			return;
		}
		value = value.intern();
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		if (value == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue(value);
		}
		myIsSynchronized = true;
	}
}
