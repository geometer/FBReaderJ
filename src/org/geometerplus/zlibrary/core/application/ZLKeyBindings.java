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

package org.geometerplus.zlibrary.core.application;

import java.util.*;
import java.util.Map.Entry;

import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public final class ZLKeyBindings {
	private static final String BINDINGS_NUMBER = "Number";
	private static final String BINDED_KEY = "Key";
	private static final String BINDED_ACTION = "Action";

	private final String myName;
	private final HashMap myBindingsMap = new HashMap();
	private	boolean myIsChanged;

	public ZLKeyBindings(String name) {
		myName = name;
		new ZLKeyBindingsReader(myBindingsMap).readBindings();
		loadCustomBindings();
		myIsChanged = false;
	}
	
	public void bindKey(String key, String actionId) {
		myBindingsMap.put(key, actionId);
		myIsChanged = true;
	}
	
	public String getBinding(String key) {
		return (String)myBindingsMap.get(key);
	}
	
	/*
	public Set getKeys() {
		return myBindingsMap.keySet();
	}
	*/
	
	private	void loadCustomBindings() {
		final int size =
			new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).getValue();
		final ZLStringOption keyOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		final ZLStringOption actionOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		for (int i = 0; i < size; ++i) {
			keyOption.changeName(BINDED_KEY + i);
			final String keyValue = keyOption.getValue();
			if (keyValue.length() != 0) {
				actionOption.changeName(BINDED_ACTION + i);
				final String actionValue = actionOption.getValue();
				if (actionValue.length() != 0) {
					bindKey(keyValue, actionValue);
				}
			}
		}
	}

	public void saveCustomBindings() {
		if (!myIsChanged) {
			return;
		}
		
		final HashMap keymap = new HashMap();
		new ZLKeyBindingsReader(keymap).readBindings();
		
		int counter = 0;
		final ZLStringOption keyOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		final ZLStringOption actionOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		for (Iterator it = myBindingsMap.keySet().iterator(); it.hasNext(); ) {
			final String key = (String)it.next();
			final String originalValue = (String)keymap.get(key);
			final String value = (String)myBindingsMap.get(key);
			if (!value.equals(originalValue)) {
				keyOption.changeName(BINDED_KEY + counter);
				actionOption.changeName(BINDED_ACTION + counter);
				keyOption.setValue(key);
				actionOption.setValue(value);
				++counter;
			}
		}
		new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).setValue(counter);
	}
}
