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

public final class ZLEnumOption<T extends Enum<T>> extends ZLOption {
	private T myValue;
	private String myStringValue;
	private Class<T> myEnumClass;

	public ZLEnumOption(String group, String optionName, T defaultValue) {
		super(group, optionName, String.valueOf(defaultValue));
		myEnumClass = defaultValue.getDeclaringClass();
	}

	public T getValue() {
		final String stringValue = getConfigValue();
		if (!stringValue.equals(myStringValue)) {
			myStringValue = stringValue;
			try {
				myValue = T.valueOf(myEnumClass, stringValue);
			} catch (Throwable t) {
			}
		}
		return myValue;
	}

	public void setValue(T value) {
		if (value == null) {
			return;
		}
		myValue = value;
		myStringValue = String.valueOf(value);
		setConfigValue(myStringValue);
	}
}
