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

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextStyleDecoration {
	static final String STYLE = "Style";

	public final ZLStringOption FontFamilyOption;
	public final ZLIntegerRangeOption FontSizeDeltaOption;
	public final ZLBoolean3Option BoldOption;
	public final ZLBoolean3Option ItalicOption;
	public final ZLIntegerOption VerticalShiftOption;
	public final ZLBoolean3Option AllowHyphenationsOption;

	private final String myName;
	private byte myHyperlinkStyle;

	public ZLTextStyleDecoration(String name, int fontSizeDelta, int bold, int italic, int verticalShift, int allowHyphenations) {
		myName = name;
		final String category = ZLOption.LOOK_AND_FEEL_CATEGORY;
		FontFamilyOption = new ZLStringOption(category, STYLE, name + ":fontFamily", "");
		FontSizeDeltaOption = new ZLIntegerRangeOption(category, STYLE, name + ":fontSize", -16, 16, fontSizeDelta);
		BoldOption = new ZLBoolean3Option(category, STYLE, name + ":bold", bold);
		ItalicOption = new ZLBoolean3Option(category, STYLE, name + ":italic", italic);
		VerticalShiftOption = new ZLIntegerOption(category, STYLE, name + ":vShift", verticalShift);
		AllowHyphenationsOption = new ZLBoolean3Option(category, STYLE, name + ":allowHyphenations", allowHyphenations);
	}
	
	public ZLTextStyle createDecoratedStyle(ZLTextStyle base) {
		return new ZLTextPartialDecoratedStyle(base, this);
	}
	
	public boolean isFullDecoration() {
		return false;
	}

	public String getName() {
		return myName;
	}

	public byte getHyperlinkStyle() {
		return myHyperlinkStyle;
	}

	public void setHyperlinkStyle(byte hyperlinkStyle) {
		myHyperlinkStyle = hyperlinkStyle;
	}
}
