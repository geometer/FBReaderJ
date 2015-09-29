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

import java.util.*;

import org.geometerplus.zlibrary.core.util.MiscUtil;

public class ZLStringListOption extends ZLOption {
	private final String myDelimiter;
	private List<String> myValue;
	private String myStringValue;

	public ZLStringListOption(String group, String optionName, List<String> defaultValue, String delimiter) {
		super(group, optionName, MiscUtil.join(defaultValue, delimiter));
		myDelimiter = delimiter;
	}

	public ZLStringListOption(String group, String optionName, String defaultValue, String delimiter) {
		this(
			group, optionName, defaultValue != null
				? Collections.singletonList(defaultValue)
				: Collections.<String>emptyList(),
			delimiter
		);
	}

	public List<String> getValue() {
		final String stringValue = getConfigValue();
		if (!stringValue.equals(myStringValue)) {
			myStringValue = stringValue;
			myValue = MiscUtil.split(stringValue, myDelimiter);
		}
		return myValue;
	}

	public void setValue(List<String> value) {
		if (value == null) {
			value = Collections.emptyList();
		}
		if (value.equals(myValue)) {
			return;
		}
		myValue = new ArrayList<String>(value);
		myStringValue = MiscUtil.join(value, myDelimiter);
		setConfigValue(myStringValue);
	}
}
