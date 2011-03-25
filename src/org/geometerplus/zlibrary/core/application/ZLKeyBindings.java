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

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public final class ZLKeyBindings {
	private static final String ACTION = "Action";
	private static final String LONG_PRESS_ACTION = "LongPressAction";

	private final String myName;
	private final ZLStringListOption myKeysOption;
	private final TreeMap<String,ZLStringOption> myActionMap = new TreeMap<String,ZLStringOption>();
	private final TreeMap<String,ZLStringOption> myLongPressActionMap = new TreeMap<String,ZLStringOption>();

	public ZLKeyBindings(String name) {
		myName = name;
		final List<String> keys = new LinkedList<String>();
		new Reader(keys).readBindings();
 		myKeysOption = new ZLStringListOption(name, "KeyList", keys);
	}

	private ZLStringOption createOption(String key, boolean longPress, String defaultValue) {
		final String group = myName + ":" + (longPress ? LONG_PRESS_ACTION : ACTION);
		return new ZLStringOption(group, key, defaultValue);
	}

	public ZLStringOption getOption(String key, boolean longPress) {
		final TreeMap<String,ZLStringOption> map = longPress ? myLongPressActionMap : myActionMap;
		ZLStringOption option = map.get(key);
		if (option == null) {
			option = createOption(key, longPress, ZLApplication.NoAction);
			map.put(key, option);
		}
		return option;
	}

	public void bindKey(String key, boolean longPress, String actionId) {
		List<String> keys = myKeysOption.getValue();
		if (!keys.contains(key)) {
			keys = new ArrayList<String>(keys);
			keys.add(key);
			myKeysOption.setValue(keys);
		}
		getOption(key, longPress).setValue(actionId);
	}

	public String getBinding(String key, boolean longPress) {
		return getOption(key, longPress).getValue();
	}

	private class Reader extends ZLXMLReaderAdapter {
		private final List<String> myKeyList;

		Reader(List<String> keyList) {
			myKeyList = keyList;
		}

		@Override
		public boolean dontCacheAttributeValues() {
			return true;
		}

		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if ("binding".equals(tag)) {
				final String key = attributes.getValue("key");
				final String actionId = attributes.getValue("action");
				if (key != null && actionId != null) {
					myKeyList.add(key);
					myActionMap.put(key, createOption(key, false, actionId));
				}
			}
			return false;
		}

		public void readBindings() {
			read(ZLResourceFile.createResourceFile("default/keymap.xml"));
		}
	}
}
