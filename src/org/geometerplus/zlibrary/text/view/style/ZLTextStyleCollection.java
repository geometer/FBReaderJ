/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextStyleCollection {
	private static ZLTextStyleCollection ourInstance = null;

	public static ZLTextStyleCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new ZLTextStyleCollection();
		}
		return ourInstance;
	}

	public static void deleteInstance() {
		ourInstance = null;
	}

	private int myDefaultFontSize;
	private ZLTextBaseStyle myBaseStyle;
	private final ZLTextStyleDecoration[] myDecorationMap = new ZLTextStyleDecoration[256];

	public final ZLBooleanOption UseCSSTextAlignmentOption =
		new ZLBooleanOption("Style", "css:textAlignment", true);
	public final ZLBooleanOption UseCSSFontSizeOption =
		new ZLBooleanOption("Style", "css:fontSize", true);

	private ZLTextStyleCollection() {
		new TextStyleReader(this).readQuietly(ZLResourceFile.createResourceFile("default/styles.xml"));
	}

	public int getDefaultFontSize() {
		return myDefaultFontSize;
	}

	public ZLTextBaseStyle getBaseStyle() {
		return myBaseStyle;
	}

	public ZLTextStyleDecoration getDecoration(byte kind) {
		return myDecorationMap[kind & 0xFF];
	}

	private static class TextStyleReader extends ZLXMLReaderAdapter {
		private final int myDpi = ZLibrary.Instance().getDisplayDPI();
		private ZLTextStyleCollection myCollection;

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

		private static boolean booleanValue(ZLStringMap attributes, String name) {
			return "true".equals(attributes.getValue(name));
		}

		private static ZLBoolean3 b3Value(ZLStringMap attributes, String name) {
			return ZLBoolean3.getByName(attributes.getValue(name));
		}

		public TextStyleReader(ZLTextStyleCollection collection) {
			myCollection = collection;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			final String BASE = "base";
			final String STYLE = "style";

			if (BASE.equals(tag)) {
				myCollection.myDefaultFontSize = intValue(attributes, "fontSize", 0);
				myCollection.myBaseStyle = new ZLTextBaseStyle(attributes.getValue("family"), myCollection.myDefaultFontSize);
			} else if (STYLE.equals(tag)) {
				String idString = attributes.getValue("id");
				String name = attributes.getValue("name");
				if ((idString != null) && (name != null)) {
					byte id = Byte.parseByte(idString);
					ZLTextStyleDecoration decoration;

					int fontSizeDelta = intValue(attributes, "fontSizeDelta", 0);
					ZLBoolean3 bold = b3Value(attributes, "bold");
					ZLBoolean3 italic = b3Value(attributes, "italic");
					ZLBoolean3 underline = b3Value(attributes, "underline");
					ZLBoolean3 strikeThrough = b3Value(attributes, "strikeThrough");
					int verticalShift = intValue(attributes, "vShift", 0);
					ZLBoolean3 allowHyphenations = b3Value(attributes, "allowHyphenations");

					if (booleanValue(attributes, "partial")) {
						decoration = new ZLTextStyleDecoration(name, fontSizeDelta, bold, italic, underline, strikeThrough, verticalShift, allowHyphenations);
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

						decoration = new ZLTextFullStyleDecoration(name, fontSizeDelta, bold, italic, underline, strikeThrough, spaceBefore, spaceAfter, leftIndent, rightIndent, firstLineIndentDelta, verticalShift, alignment, lineSpacePercent, allowHyphenations);
					}

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
