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

package org.geometerplus.zlibrary.core.application;

import java.util.*;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

public final class ZLKeyBindings {
	private static final String BINDINGS_NUMBER = "Number";
	private static final String KEY = "Key";
	private static final String ACTION = "Action";
	private static final String LONG_PRESS_ACTION = "LongPressAction";

	private final String myName;
	private final TreeMap<String,String> myActionMap = new TreeMap<String,String>();
	private final TreeMap<String,String> myLongPressActionMap = new TreeMap<String,String>();
	private	boolean myIsChanged;

	public ZLKeyBindings(String name) {
		myName = name;
		new ZLKeyBindingsReader(myActionMap).readBindings();
		myLongPressActionMap.put("<Back>", "longCancel");
		loadCustomBindings();
		myIsChanged = false;
	}

	public void bindKey(String key, String actionId, boolean longPress) {
		if (ZLMiscUtil.isEmptyString(key) || ZLMiscUtil.isEmptyString(actionId)) {
			return;
		}
		final TreeMap<String,String> map = longPress ? myLongPressActionMap : myActionMap;
		map.put(key, actionId);
		myIsChanged = true;
	}

	public String getBinding(String key, boolean longPress) {
		final TreeMap<String,String> map = longPress ? myLongPressActionMap : myActionMap;
		return map.get(key);
	}

	private	void loadCustomBindings() {
		final int size = new ZLIntegerRangeOption(myName, BINDINGS_NUMBER, 0, 256, 0).getValue();
		for (int i = 0; i < size; ++i) {
			final String key = new ZLStringOption(myName, KEY + i, "").getValue();
			bindKey(key, new ZLStringOption(myName, ACTION + i, "").getValue(), false);
			bindKey(key, new ZLStringOption(myName, LONG_PRESS_ACTION + i, "").getValue(), true);
		}
	}

	public void saveCustomBindings() {
		if (!myIsChanged) {
			return;
		}

		final TreeMap<String,String> keymap = new TreeMap<String,String>();
		new ZLKeyBindingsReader(keymap).readBindings();

		final TreeSet<String> allKeys = new TreeSet(keymap.keySet());
		allKeys.addAll(myActionMap.keySet());
		allKeys.addAll(myLongPressActionMap.keySet());

		int counter = 0;
		for (final String key : allKeys) {
			final String originalAction = keymap.get(key);
			final String action = myActionMap.get(key);
			final String longPressAction = myLongPressActionMap.get(key);
			if (!ZLMiscUtil.equals(originalAction, action) ||
				!ZLMiscUtil.isEmptyString(longPressAction)) {
				new ZLStringOption(myName, KEY + counter, "").setValue(key);
				new ZLStringOption(myName, ACTION + counter, "").setValue(action);
				new ZLStringOption(myName, LONG_PRESS_ACTION + counter, "").setValue(longPressAction);
				++counter;
			}
		}
		new ZLIntegerRangeOption(myName, BINDINGS_NUMBER, 0, 256, 0).setValue(counter);
	}
}
