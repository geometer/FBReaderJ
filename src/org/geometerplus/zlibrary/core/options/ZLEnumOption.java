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

public final class ZLEnumOption<T extends Enum<T>> extends ZLOption {
	private final T myDefaultValue;
	private T myValue;

	public ZLEnumOption(String group, String optionName, T defaultValue) {
		super(group, optionName);
		myDefaultValue = defaultValue;
		myValue = defaultValue;
	}

	public T getValue() {
		if (!myIsSynchronized) {
			final String value = getConfigValue(null);
			if (value != null) {
				try {
					myValue = T.valueOf(myDefaultValue.getDeclaringClass(), value);
				} catch (Throwable t) {
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(T value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (value == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue("" + value.toString());
		}
	}
}
