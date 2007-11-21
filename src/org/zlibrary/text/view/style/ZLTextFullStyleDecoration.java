package org.zlibrary.text.view.style;

import org.zlibrary.options.*;
import org.zlibrary.options.util.*;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextFullStyleDecoration extends ZLTextStyleDecoration {
	public ZLIntegerRangeOption SpaceBeforeOption;	
	public ZLIntegerRangeOption SpaceAfterOption;		
	public ZLIntegerRangeOption LeftIndentOption;	
	public ZLIntegerRangeOption RightIndentOption;
	public ZLIntegerRangeOption FirstLineIndentDeltaOption;

	public ZLIntegerOption AlignmentOption;

	public ZLDoubleOption LineSpaceOption;	

	public ZLTextFullStyleDecoration(String name, int fontSizeDelta, ZLBoolean3 bold, ZLBoolean3 italic, int spaceBefore, int spaceAfter, int leftIndent,int rightIndent, int firstLineDelta, int verticalShift, ZLTextAlignmentType alignment, double lineSpace, ZLBoolean3 allowHyphenations) {
		super(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
	}

	public boolean isFullDecoration() {
		return true;
	}

//	public ZLTextStyle createDecoratedStyle(ZLTextStyle base) {
//		
//	}
}
