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

package org.geometerplus.zlibrary.core.application;

import java.util.*;

import android.view.KeyEvent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.XmlUtil;

import org.geometerplus.fbreader.Paths;

import org.geometerplus.android.util.DeviceType;

public final class ZLKeyBindings {
	private static final String ACTION = "Action";
	private static final String LONG_PRESS_ACTION = "LongPressAction";

	private final String myName;
	private ZLStringListOption myKeysOption;
	private final TreeMap<Integer,ZLStringOption> myActionMap = new TreeMap<Integer,ZLStringOption>();
	private final TreeMap<Integer,ZLStringOption> myLongPressActionMap = new TreeMap<Integer,ZLStringOption>();

	public ZLKeyBindings() {
		this("Keys");
	}

	private ZLKeyBindings(String name) {
		myName = name;
		Config.Instance().runOnConnect(new Initializer());
	}

	private class Initializer implements Runnable {
		public void run() {
			final Set<String> keys = new TreeSet<String>();

			final DeviceType deviceType = DeviceType.Instance();
			final String keymapFilename;
			if (deviceType == DeviceType.NOOK || deviceType == DeviceType.NOOK12) {
				keymapFilename = "keymap-nook.xml";
			} else {
				keymapFilename = "keymap.xml";
			}
			new Reader(keys).readQuietly("default/" + keymapFilename);
			new Reader(keys).readQuietly(Paths.systemShareDirectory() + "/keymap.xml");
			new Reader(keys).readQuietly(Paths.bookPath().get(0) + "/keymap.xml");
			myKeysOption = new ZLStringListOption(myName, "KeyList", new ArrayList<String>(keys), ",");
		}
	}

	private ZLStringOption createOption(int key, boolean longPress, String defaultValue) {
		final String group = myName + ":" + (longPress ? LONG_PRESS_ACTION : ACTION);
		return new ZLStringOption(group, String.valueOf(key), defaultValue);
	}

	public ZLStringOption getOption(int key, boolean longPress) {
		final TreeMap<Integer,ZLStringOption> map = longPress ? myLongPressActionMap : myActionMap;
		ZLStringOption option = map.get(key);
		if (option == null) {
			option = createOption(key, longPress, ZLApplication.NoAction);
			map.put(key, option);
		}
		return option;
	}

	public void bindKey(int key, boolean longPress, String actionId) {
		if (myKeysOption == null) {
			return;
		}
		final String stringKey = String.valueOf(key);
		List<String> keys = myKeysOption.getValue();
		if (!keys.contains(stringKey)) {
			keys = new ArrayList<String>(keys);
			keys.add(stringKey);
			Collections.sort(keys);
			myKeysOption.setValue(keys);
		}
		getOption(key, longPress).setValue(actionId);
	}

	public String getBinding(int key, boolean longPress) {
		return getOption(key, longPress).getValue();
	}

	public boolean hasBinding(int key, boolean longPress) {
		return !ZLApplication.NoAction.equals(getBinding(key, longPress));
	}

	private class Reader extends DefaultHandler {
		private final Set<String> myKeySet;

		Reader(Set<String> keySet) {
			myKeySet = keySet;
		}

		public void readQuietly(String path) {
			XmlUtil.parseQuietly(ZLFile.createFileByPath(path), this);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("binding".equals(localName)) {
				final String stringKey = attributes.getValue("key");
				final String actionId = attributes.getValue("action");
				if (stringKey != null && actionId != null) {
					try {
						final int key = Integer.parseInt(stringKey);
						myKeySet.add(stringKey);
						myActionMap.put(key, createOption(key, false, actionId));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
}
