/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view.style;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextStyleCollection {
	public final String Screen;
	private ZLTextBaseStyle myBaseStyle;
	private final List<ZLTextNGStyleDescription> myDescriptionList;
	private final ZLTextNGStyleDescription[] myDescriptionMap = new ZLTextNGStyleDescription[256];

	public ZLTextStyleCollection(String screen) {
		Screen = screen;
		final Map<Integer,ZLTextNGStyleDescription> descriptions =
			new SimpleCSSReader().read(ZLResourceFile.createResourceFile("default/styles.css"));
		myDescriptionList = Collections.unmodifiableList(
			new ArrayList<ZLTextNGStyleDescription>(descriptions.values())
		);
		for (Map.Entry<Integer,ZLTextNGStyleDescription> entry : descriptions.entrySet()) {
			myDescriptionMap[entry.getKey() & 0xFF] = entry.getValue();
		}
		new TextStyleReader().readQuietly(ZLResourceFile.createResourceFile("default/styles.xml"));
	}

	public ZLTextBaseStyle getBaseStyle() {
		return myBaseStyle;
	}

	public List<ZLTextNGStyleDescription> getDescriptionList() {
		return myDescriptionList;
	}

	public ZLTextNGStyleDescription getDescription(byte kind) {
		return myDescriptionMap[kind & 0xFF];
	}

	private class TextStyleReader extends ZLXMLReaderAdapter {
		@Override
		public boolean dontCacheAttributeValues() {
			return true;
		}

		private int intValue(ZLStringMap attributes, String name, int defaultValue) {
			final String value = attributes.getValue(name);
			if (value != null) {
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
				}
			}
			return defaultValue;
		}

		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if ("base".equals(tag) && Screen.equals(attributes.getValue("screen"))) {
				myBaseStyle = new ZLTextBaseStyle(
					Screen,
					attributes.getValue("family"),
					intValue(attributes, "fontSize", 0)
				);
			}
			return false;
		}
	}
}
