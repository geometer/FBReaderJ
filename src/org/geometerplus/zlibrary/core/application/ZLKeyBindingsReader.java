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

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

class ZLKeyBindingsReader extends ZLXMLReaderAdapter {
	private final HashMap<String, String> myKeymap;

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}

	public ZLKeyBindingsReader(HashMap<String, String> keymap) {
		myKeymap = keymap; 
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if ("binding".equals(tag)) {
			final String key = attributes.getValue("key");
			final String actionId = attributes.getValue("action");
			if ((key != null) && (actionId != null)) {
				myKeymap.put(key, actionId);
			}
		}
		return false;
	}

	public void readBindings() {
		read(ZLResourceFile.createResourceFile("default/keymap.xml"));
	}
}
