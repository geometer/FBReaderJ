package org.zlibrary.text.view.style;

import org.zlibrary.core.options.*;
import org.zlibrary.core.options.util.*;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.ZLTextAlignmentType;
import org.zlibrary.text.model.entry.*;

public class ZLTextFullStyleDecoration extends ZLTextStyleDecoration {
	public ZLIntegerRangeOption SpaceBeforeOption;	
	public ZLIntegerRangeOption SpaceAfterOption;		
	public ZLIntegerRangeOption LeftIndentOption;	
	public ZLIntegerRangeOption RightIndentOption;
	public ZLIntegerRangeOption FirstLineIndentDeltaOption;

	public ZLIntegerOption AlignmentOption;

	public ZLDoubleOption LineSpaceOption;	

	public ZLTextFullStyleDecoration(String name, int fontSizeDelta, ZLBoolean3 bold, ZLBoolean3 italic, int spaceBefore, int spaceAfter, int leftIndent,int rightIndent, int firstLineIndentDelta, int verticalShift, int alignment, double lineSpace, ZLBoolean3 allowHyphenations) {
		super(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
//		System.out.println("Constructed, fonSizeDelta = " + fontSizeDelta);
		SpaceBeforeOption = null;
		SpaceAfterOption = null;
		LeftIndentOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, name + ":leftIndent", -300, 300, leftIndent);
		RightIndentOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, name + ":rightIndent", -300, 300, rightIndent);
		FirstLineIndentDeltaOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, name + ":firstLineIndentDelta", -300, 300, firstLineIndentDelta);
		AlignmentOption = new ZLIntegerOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, name + ":alignment", alignment);
		LineSpaceOption = null;
	}

	public boolean isFullDecoration() {
		return true;
	}

	public ZLTextStyle createDecoratedStyle(ZLTextStyle base) {
		return new ZLTextFullDecoratedStyle(base, this);
	}
}
