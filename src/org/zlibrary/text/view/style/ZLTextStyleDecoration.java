package org.zlibrary.text.view.style;

import org.zlibrary.options.util.*;
import org.zlibrary.options.*;

import org.zlibrary.text.view.ZLTextStyle;


public class ZLTextStyleDecoration {
	public static final String STYLE = "Style";
	
	public ZLStringOption FontFamilyOption;
	public ZLIntegerRangeOption FontSizeDeltaOption;
	public ZLBoolean3Option BoldOption;
	public ZLBoolean3Option ItalicOption;

	private String myName;

	public ZLIntegerOption VerticalShiftOption;

	public ZLBoolean3Option AllowHyphenationsOption;

	public ZLTextStyleDecoration(String name, int fontSizeDelta, ZLBoolean3 bold, ZLBoolean3 italic, int verticalShift, ZLBoolean3 allowHyphenations) {
		FontFamilyOption = null;
		FontSizeDeltaOption = null;
		BoldOption = new ZLBoolean3Option(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, name + ":bold", bold);
		ItalicOption = null;
		VerticalShiftOption = null;
		AllowHyphenationsOption = null;
	}
	
	public boolean isFullDecoration() {
		return false;
	}

	public String getName() {
		return myName;
	}
}
