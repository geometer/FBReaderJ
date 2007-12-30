package org.zlibrary.text.view.style;

import org.zlibrary.core.options.*;
import org.zlibrary.core.util.*;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.ZLTextAlignmentType;

class ZLTextFullStyleDecoration extends ZLTextStyleDecoration {
	public final ZLIntegerRangeOption SpaceBeforeOption;
	public final ZLIntegerRangeOption SpaceAfterOption;
	public final ZLIntegerRangeOption LeftIndentOption;
	public final ZLIntegerRangeOption RightIndentOption;
	public final ZLIntegerRangeOption FirstLineIndentDeltaOption;

	public final ZLIntegerOption AlignmentOption;

	public final ZLDoubleOption LineSpaceOption;

	public ZLTextFullStyleDecoration(String name, int fontSizeDelta, int bold, int italic, int spaceBefore, int spaceAfter, int leftIndent,int rightIndent, int firstLineIndentDelta, int verticalShift, byte alignment, double lineSpace, int allowHyphenations) {
		super(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
		final String category = ZLOption.LOOK_AND_FEEL_CATEGORY;
		SpaceBeforeOption = new ZLIntegerRangeOption(category, STYLE, name + ":spaceBefore", -10, 100, spaceBefore);
		SpaceAfterOption = new ZLIntegerRangeOption(category, STYLE, name + ":spaceAfter", -10, 100, spaceAfter);
		LeftIndentOption = new ZLIntegerRangeOption(category, STYLE, name + ":leftIndent", -300, 300, leftIndent);
		RightIndentOption = new ZLIntegerRangeOption(category, STYLE, name + ":rightIndent", -300, 300, rightIndent);
		FirstLineIndentDeltaOption = new ZLIntegerRangeOption(category, STYLE, name + ":firstLineIndentDelta", -300, 300, firstLineIndentDelta);
		AlignmentOption = new ZLIntegerOption(category, STYLE, name + ":alignment", alignment);
		LineSpaceOption = new ZLDoubleOption(category, STYLE, name + ":lineSpace", lineSpace);
	}

	public boolean isFullDecoration() {
		return true;
	}

	public ZLTextStyle createDecoratedStyle(ZLTextStyle base) {
		return new ZLTextFullDecoratedStyle(base, this);
	}
}
