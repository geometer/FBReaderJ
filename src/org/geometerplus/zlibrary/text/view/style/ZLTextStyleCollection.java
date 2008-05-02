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

package org.geometerplus.zlibrary.text.view.style;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextStyleCollection {
	private static ZLTextStyleCollection ourInstance = null;
	
	private ZLTextBaseStyle myBaseStyle;
	private final ZLTextStyleDecoration[] myDecorationMap = new ZLTextStyleDecoration[256];
	
	private ZLTextStyleCollection() {
		new TextStyleReader(this).read(ZLibrary.JAR_DATA_PREFIX + "data/default/styles.xml");
		if (myBaseStyle == null) {
			myBaseStyle = new ZLTextBaseStyle("", 20);
		}
	}
	
	public static ZLTextStyleCollection getInstance() {
		if (ourInstance == null) {
			ourInstance = new ZLTextStyleCollection();
		}
		return ourInstance;
	}
	
	public static void deleteInstance() {
		ourInstance = null;
	}
	
	public ZLTextBaseStyle getBaseStyle() {
		return myBaseStyle;
	}
	
	public ZLTextStyleDecoration getDecoration(byte kind) {
		return myDecorationMap[kind & 0xFF];
	}
		
	public ZLTextBaseStyle baseStyle() {
		return myBaseStyle;
	}

	private static class TextStyleReader extends ZLXMLReaderAdapter {
		private ZLTextStyleCollection myCollection;

		private static int intValue(ZLStringMap attributes, String name, int defaultValue) {
			int i = defaultValue;
			String value = attributes.getValue(name);
			if (value != null) {
				try {
					i = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} 
			return i;
		}

		private static boolean booleanValue(ZLStringMap attributes, String name) {
			return "true" == attributes.getValue(name);
		}

		private static int b3Value(ZLStringMap attributes, String name) {
			return ZLBoolean3.getByString(attributes.getValue(name));
		}
			
		public TextStyleReader(ZLTextStyleCollection collection) {
			myCollection = collection;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			final String BASE = "base";
			final String STYLE = "style";

			if (BASE.equals(tag)) {
				myCollection.myBaseStyle = new ZLTextBaseStyle(attributes.getValue("family"), intValue(attributes, "fontSize", 0));
			} else if (STYLE.equals(tag)) {
				String idString = attributes.getValue("id");
				String name = attributes.getValue("name");
				if ((idString != null) && (name != null)) {
					byte id = Byte.parseByte(idString);
					ZLTextStyleDecoration decoration;

					int fontSizeDelta = intValue(attributes, "fontSizeDelta", 0);
					int bold = b3Value(attributes, "bold");
					int italic = b3Value(attributes, "italic");
					int verticalShift = intValue(attributes, "vShift", 0);
					int allowHyphenations = b3Value(attributes, "allowHyphenations");
					byte hyperlinkStyle = HyperlinkStyle.NONE;
					String hyperlink = attributes.getValue("hyperlink");
					if (hyperlink != null) {
						if ("internal".equals(hyperlink)) {
							hyperlinkStyle = HyperlinkStyle.INTERNAL;
						}
						if ("external".equals(hyperlink)) {
							hyperlinkStyle = HyperlinkStyle.EXTERNAL;
						}
					}

					if (booleanValue(attributes, "partial")) {
						decoration = new ZLTextStyleDecoration(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
					} else {
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

						decoration = new ZLTextFullStyleDecoration(name, fontSizeDelta, bold, italic, spaceBefore, spaceAfter, leftIndent, rightIndent, firstLineIndentDelta, verticalShift, alignment, lineSpacePercent, allowHyphenations);
					}
					decoration.setHyperlinkStyle(hyperlinkStyle);

					String fontFamily = attributes.getValue("family");
					if (fontFamily != null) {
						decoration.FontFamilyOption.setValue(fontFamily);
					}

					myCollection.myDecorationMap[id & 0xFF] = decoration;
				}
			}
			return false;
		}
	}	
}
