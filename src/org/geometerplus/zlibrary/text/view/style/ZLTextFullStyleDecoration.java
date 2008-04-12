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

import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextFullStyleDecoration extends ZLTextStyleDecoration {
	public final ZLIntegerRangeOption SpaceBeforeOption;
	public final ZLIntegerRangeOption SpaceAfterOption;
	public final ZLIntegerRangeOption LeftIndentOption;
	public final ZLIntegerRangeOption RightIndentOption;
	public final ZLIntegerRangeOption FirstLineIndentDeltaOption;

	public final ZLIntegerOption AlignmentOption;

	public final ZLIntegerOption LineSpacePercentOption;

	public ZLTextFullStyleDecoration(String name, int fontSizeDelta, int bold, int italic, int spaceBefore, int spaceAfter, int leftIndent,int rightIndent, int firstLineIndentDelta, int verticalShift, byte alignment, int lineSpace, int allowHyphenations) {
		super(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
		final String category = ZLOption.LOOK_AND_FEEL_CATEGORY;
		SpaceBeforeOption = new ZLIntegerRangeOption(category, STYLE, name + ":spaceBefore", -10, 100, spaceBefore);
		SpaceAfterOption = new ZLIntegerRangeOption(category, STYLE, name + ":spaceAfter", -10, 100, spaceAfter);
		LeftIndentOption = new ZLIntegerRangeOption(category, STYLE, name + ":leftIndent", -300, 300, leftIndent);
		RightIndentOption = new ZLIntegerRangeOption(category, STYLE, name + ":rightIndent", -300, 300, rightIndent);
		FirstLineIndentDeltaOption = new ZLIntegerRangeOption(category, STYLE, name + ":firstLineIndentDelta", -300, 300, firstLineIndentDelta);
		AlignmentOption = new ZLIntegerOption(category, STYLE, name + ":alignment", alignment);
		LineSpacePercentOption = new ZLIntegerOption(category, STYLE, name + ":lineSpacePercent", lineSpace);
	}

	public boolean isFullDecoration() {
		return true;
	}

	public ZLTextStyle createDecoratedStyle(ZLTextStyle base) {
		return new ZLTextFullDecoratedStyle(base, this);
	}
}
