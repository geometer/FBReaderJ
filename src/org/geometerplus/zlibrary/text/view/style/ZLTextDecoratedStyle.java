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

import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public abstract class ZLTextDecoratedStyle {
	private ZLTextStyle myBase;
	
	protected ZLTextDecoratedStyle(ZLTextStyle base) {
		myBase = base;
	}

	public ZLTextStyle getBase() {
		return myBase;
	}

	private ZLColor myColor;
	public final ZLColor getColor() {
		ZLColor color = myColor;
		if (color == null) {
			color = getColorInternal();
			myColor = color;
		}
		return color;
	}
	protected abstract ZLColor getColorInternal();

	private String myFontFamily;
	public final String getFontFamily() {
		String family = myFontFamily;
		if (family == null) {
			family = getFontFamilyInternal();
			myFontFamily = family;
		}
		return family;
	}
	protected abstract String getFontFamilyInternal();

	private boolean myIsItalic;
	private boolean myIsItalicCached;
	public final boolean isItalic() {
		if (myIsItalicCached) {
			return myIsItalic;
		}
		final boolean answer = isItalicInternal();
		myIsItalic = answer;
		myIsItalicCached = true;
		return answer;
	}
	protected abstract boolean isItalicInternal();

	private boolean myIsBold;
	private boolean myIsBoldCached;
	public final boolean isBold() {
		if (myIsBoldCached) {
			return myIsBold;
		}
		final boolean answer = isBoldInternal();
		myIsBold = answer;
		myIsBoldCached = true;
		return answer;
	}
	protected abstract boolean isBoldInternal();

	private int myVerticalShift;
	private boolean myVerticalShiftCached;
	public final int getVerticalShift() {
		if (myVerticalShiftCached) {
			return myVerticalShift;
		}
		final int shift = getVerticalShiftInternal();
		myVerticalShift = shift;
		myVerticalShiftCached = true;
		return shift;
	}
	protected abstract int getVerticalShiftInternal();

	private int myFontSize;
	private boolean myFontSizeCached;
	public final int getFontSize() {
		if (myFontSizeCached) {
			return myFontSize;
		}
		final int size = getFontSizeInternal();
		myFontSize = size;
		myFontSizeCached = true;
		return size;
	}
	protected abstract int getFontSizeInternal();
}
