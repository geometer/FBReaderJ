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

package org.geometerplus.zlibrary.text.model;

public final class ZLTextStyleEntry {
	public interface Feature {
		int LENGTH_LEFT_INDENT =                0;
		int LENGTH_RIGHT_INDENT =               1;
		int LENGTH_FIRST_LINE_INDENT_DELTA =    2;
		int LENGTH_SPACE_BEFORE =               3;
		int LENGTH_SPACE_AFTER =                4;
		int NUMBER_OF_LENGTHS =                 5;
		int ALIGNMENT_TYPE =                    NUMBER_OF_LENGTHS;
		int FONT_SIZE_MAGNIFICATION =           NUMBER_OF_LENGTHS + 1;
		int FONT_FAMILY =                       NUMBER_OF_LENGTHS + 2;
		int FONT_STYLE_MODIFIER =               NUMBER_OF_LENGTHS + 3;
	}

	private short myFeatureMask;
	private byte myAlignmentType;
	private byte myFontSizeMagnification;
	private String myFontFamily;

	static boolean isFeatureSupported(short mask, int featureId) {
		return (mask & (1 << featureId)) != 0;
	}

	public boolean isFeatureSupported(int featureId) {
		return isFeatureSupported(myFeatureMask, featureId);
	}

	//private short myLeftIndent;
	//private short myRightIndent;

	public ZLTextStyleEntry() {
	}

	/*
	public short getLeftIndent() {
		return myLeftIndent;
	}
	
	public short getRightIndent() {
		return myRightIndent;
	}
	*/

	void setAlignmentType(byte alignmentType) {
		myFeatureMask |= 1 << Feature.ALIGNMENT_TYPE;
		myAlignmentType = alignmentType;
	}
	
	public byte getAlignmentType() {
		return myAlignmentType;
	}

	void setFontSizeMagnification(byte fontSizeMagnification) {
		myFeatureMask |= 1 << Feature.FONT_SIZE_MAGNIFICATION;
		myFontSizeMagnification = fontSizeMagnification;
	}
	
	public byte getFontSizeMagnification() {
		return myFontSizeMagnification;
	}

	void setFontFamily(String fontFamily) {
		myFeatureMask |= 1 << Feature.FONT_FAMILY;
		System.err.println("setting font family to " + fontFamily);
		myFontFamily = fontFamily;
	}

	public String getFontFamily() {
		return myFontFamily;
	}
}
