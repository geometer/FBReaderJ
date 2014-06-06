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

import java.util.Map;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextStyleCollection {
	public final String Screen;
	private int myDefaultFontSize;
	private ZLTextBaseStyle myBaseStyle;
	private final ZLTextNGStyleDescription[] myDescriptionMap = new ZLTextNGStyleDescription[256];
	private final ZLTextStyleDecoration[] myDecorationMap = new ZLTextStyleDecoration[256];

	public ZLTextStyleCollection(String screen) {
		Screen = screen;
		final Map<Integer,ZLTextNGStyleDescription> descriptions =
			new SimpleCSSReader().read(ZLResourceFile.createResourceFile("default/styles.css"));
		for (Map.Entry<Integer,ZLTextNGStyleDescription> entry : descriptions.entrySet()) {
			myDescriptionMap[entry.getKey() & 0xFF] = entry.getValue();
		}
		new TextStyleReader().readQuietly(ZLResourceFile.createResourceFile("default/styles.xml"));
	}

	public int getDefaultFontSize() {
		return myDefaultFontSize;
	}

	public ZLTextBaseStyle getBaseStyle() {
		return myBaseStyle;
	}

	public ZLTextNGStyleDescription getDescription(byte kind) {
		return myDescriptionMap[kind & 0xFF];
	}

	public ZLTextStyleDecoration getDecoration(byte kind) {
		return myDecorationMap[kind & 0xFF];
	}

	private class TextStyleReader extends ZLXMLReaderAdapter {
		private final int myDpi = ZLibrary.Instance().getDisplayDPI();

		@Override
		public boolean dontCacheAttributeValues() {
			return true;
		}

		private int intValue(ZLStringMap attributes, String name, int defaultValue) {
			int i = defaultValue;
			String value = attributes.getValue(name);
			if (value != null) {
				if (value.startsWith("dpi*")) {
					try {
						final float coe = Float.parseFloat(value.substring(4));
						i = (int)(coe * myDpi + .5f);
					} catch (NumberFormatException e) {
					}
				} else {
					try {
						i = Integer.parseInt(value);
					} catch (NumberFormatException e) {
					}
				}
			}
			return i;
		}

		private boolean booleanValue(ZLStringMap attributes, String name) {
			return "true".equals(attributes.getValue(name));
		}

		private ZLBoolean3 b3Value(ZLStringMap attributes, String name) {
			return ZLBoolean3.getByName(attributes.getValue(name));
		}

		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			final String BASE = "base";
			final String STYLE = "style";

			if (BASE.equals(tag)) {
				if (Screen.equals(attributes.getValue("screen"))) {
					myDefaultFontSize = intValue(attributes, "fontSize", 0);
					myBaseStyle = new ZLTextBaseStyle(Screen, attributes.getValue("family"), myDefaultFontSize);
				}
			} else if (STYLE.equals(tag)) {
				String idString = attributes.getValue("id");
				String name = attributes.getValue("name");
				if (idString != null && name != null) {
					byte id = Byte.parseByte(idString);

					final int fontSizeDelta = intValue(attributes, "fontSizeDelta", 0);
					final ZLBoolean3 bold = b3Value(attributes, "bold");
					final ZLBoolean3 italic = b3Value(attributes, "italic");
					final ZLBoolean3 allowHyphenations = b3Value(attributes, "allowHyphenations");

					int spaceBefore = intValue(attributes, "spaceBefore", 0);
					int spaceAfter = intValue(attributes, "spaceAfter", 0);
					int leftIndent = intValue(attributes, "leftIndent", 0);
					int rightIndent = intValue(attributes, "rightIndent", 0);
					int firstLineIndentDelta = intValue(attributes, "firstLineIndentDelta", 0);

					byte alignment = ZLTextAlignmentType.ALIGN_UNDEFINED;
					String alignmentString = attributes.getValue("alignment");
					if (alignmentString != null) {
						if (alignmentString.equals("left")) {
							alignment = ZLTextAlignmentType.ALIGN_LEFT;
						} else if (alignmentString.equals("right")) {
							alignment = ZLTextAlignmentType.ALIGN_RIGHT;
						} else if (alignmentString.equals("center")) {
							alignment = ZLTextAlignmentType.ALIGN_CENTER;
						} else if (alignmentString.equals("justify")) {
							alignment = ZLTextAlignmentType.ALIGN_JUSTIFY;
						}
					}
					final int lineSpacePercent = intValue(attributes, "lineSpacingPercent", -1);

					myDecorationMap[id & 0xFF] = new ZLTextStyleDecoration(name, null, fontSizeDelta, bold, italic, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED, spaceBefore, spaceAfter, leftIndent, rightIndent, firstLineIndentDelta, 0, alignment, lineSpacePercent, allowHyphenations);
				}
			}
			return false;
		}
	}
}
