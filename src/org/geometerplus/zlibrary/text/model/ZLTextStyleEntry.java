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

package org.geometerplus.zlibrary.text.model;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

public abstract class ZLTextStyleEntry {
	public interface Feature {
		int LENGTH_LEFT_INDENT                = 0;
		int LENGTH_RIGHT_INDENT               = 1;
		int LENGTH_FIRST_LINE_INDENT          = 2;
		int LENGTH_SPACE_BEFORE               = 3;
		int LENGTH_SPACE_AFTER                = 4;
		int LENGTH_FONT_SIZE                  = 5;
		int NUMBER_OF_LENGTHS                 = 6;
		int ALIGNMENT_TYPE                    = NUMBER_OF_LENGTHS;
		int FONT_FAMILY                       = NUMBER_OF_LENGTHS + 1;
		int FONT_STYLE_MODIFIER               = NUMBER_OF_LENGTHS + 2;
	}

	public interface FontModifier {
		byte FONT_MODIFIER_BOLD               = 1 << 0;
		byte FONT_MODIFIER_ITALIC             = 1 << 1;
		byte FONT_MODIFIER_UNDERLINED         = 1 << 2;
		byte FONT_MODIFIER_STRIKEDTHROUGH     = 1 << 3;
		byte FONT_MODIFIER_SMALLCAPS          = 1 << 4;
		byte FONT_MODIFIER_INHERIT            = 1 << 5;
		byte FONT_MODIFIER_SMALLER            = 1 << 6;
		byte FONT_MODIFIER_LARGER             = (byte)(1 << 7);
	}

	public interface SizeUnit {
		byte PIXEL                            = 0;
		byte POINT                            = 1;
		byte EM_100                           = 2;
		byte EX_100                           = 3;
		byte PERCENT                          = 4;
	}

	public static class Length {
		public final short Size;
		public final byte Unit;

		public Length(short size, byte unit) {
			Size = size;
			Unit = unit;
		}

		@Override
		public String toString() {
			return Size + "." + Unit;
		}
	}

	private short myFeatureMask;

	private Length[] myLengths = new Length[Feature.NUMBER_OF_LENGTHS];
	private byte myAlignmentType;
	private List<FontEntry> myFontEntries;
	private byte mySupportedFontModifiers;
	private byte myFontModifiers;

	static boolean isFeatureSupported(short mask, int featureId) {
		return (mask & (1 << featureId)) != 0;
	}

	protected ZLTextStyleEntry() {
	}

	public final boolean isFeatureSupported(int featureId) {
		return isFeatureSupported(myFeatureMask, featureId);
	}

	final void setLength(int featureId, short size, byte unit) {
		myFeatureMask |= 1 << featureId;
		myLengths[featureId] = new Length(size, unit);
	}

	private static int fullSize(ZLTextMetrics metrics, int fontSize, int featureId) {
		switch (featureId) {
			default:
			case Feature.LENGTH_LEFT_INDENT:
			case Feature.LENGTH_RIGHT_INDENT:
			case Feature.LENGTH_FIRST_LINE_INDENT:
				return metrics.FullWidth;
			case Feature.LENGTH_SPACE_BEFORE:
			case Feature.LENGTH_SPACE_AFTER:
				return metrics.FullHeight;
			case Feature.LENGTH_FONT_SIZE:
				return fontSize;
		}
	}

	public final int getLength(int featureId, ZLTextMetrics metrics, int baseFontSize) {
		return compute(myLengths[featureId], metrics, baseFontSize, featureId);
	}

	public static int compute(Length length, ZLTextMetrics metrics, int baseFontSize, int featureId) {
		switch (length.Unit) {
			default:
			case SizeUnit.PIXEL:
				return length.Size * baseFontSize / metrics.DefaultFontSize;
			// we understand "point" as "1/2 point"
			case SizeUnit.POINT:
				return length.Size
					* metrics.DPI * baseFontSize
					/ 72 / metrics.DefaultFontSize / 2;
			case SizeUnit.EM_100:
				return (length.Size * baseFontSize + 50) / 100;
			case SizeUnit.EX_100:
				// TODO 1.5 font size => height of X
				return (length.Size * baseFontSize * 3 / 2 + 50) / 100;
			case SizeUnit.PERCENT:
				return (length.Size * fullSize(metrics, baseFontSize, featureId) + 50) / 100;
		}
	}

	final void setAlignmentType(byte alignmentType) {
		myFeatureMask |= 1 << Feature.ALIGNMENT_TYPE;
		myAlignmentType = alignmentType;
	}

	public final byte getAlignmentType() {
		return myAlignmentType;
	}

	final void setFontFamilies(FontManager fontManager, int fontFamiliesIndex) {
		myFeatureMask |= 1 << Feature.FONT_FAMILY;
		myFontEntries = fontManager.getFamilyEntries(fontFamiliesIndex);
	}

	public final List<FontEntry> getFontEntries() {
		return myFontEntries;
	}

	final void setFontModifiers(byte supported, byte values) {
		myFeatureMask |= 1 << Feature.FONT_STYLE_MODIFIER;
		mySupportedFontModifiers = supported;
		myFontModifiers = values;
	}

	public final void setFontModifier(byte modifier, boolean on) {
		myFeatureMask |= 1 << Feature.FONT_STYLE_MODIFIER;
		mySupportedFontModifiers |= modifier;
		if (on) {
			myFontModifiers |= modifier;
		} else {
			myFontModifiers &= ~modifier;
		}
	}

	public final ZLBoolean3 getFontModifier(byte modifier) {
		if ((mySupportedFontModifiers & modifier) == 0) {
			return ZLBoolean3.B3_UNDEFINED;
		}
		return (myFontModifiers & modifier) == 0 ? ZLBoolean3.B3_FALSE : ZLBoolean3.B3_TRUE;
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder("StyleEntry[");
		buffer.append("features: ").append(myFeatureMask).append(";");
		if (isFeatureSupported(Feature.LENGTH_SPACE_BEFORE)) {
			buffer.append("space-before: ").append(myLengths[Feature.LENGTH_SPACE_BEFORE]).append(";");
		}
		if (isFeatureSupported(Feature.LENGTH_SPACE_AFTER)) {
			buffer.append("space-after: ").append(myLengths[Feature.LENGTH_SPACE_AFTER]).append(";");
		}
		buffer.append("]");
		return buffer.toString();
	}
}
