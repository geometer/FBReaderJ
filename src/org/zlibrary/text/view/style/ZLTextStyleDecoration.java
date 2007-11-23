package org.zlibrary.text.view.style;

import org.zlibrary.options.util.*;
import org.zlibrary.options.*;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.entry.*;

public class ZLTextStyleDecoration {
	public static final String STYLE = "Style";
	public static final byte BOLD = 28;
	public static final byte LARGE_FONT_SIZE = 31;

	public ZLStringOption FontFamilyOption;
	public ZLIntegerRangeOption FontSizeDeltaOption;
	public ZLBoolean3Option BoldOption;
	public ZLBoolean3Option ItalicOption;

	private String myName;

	public ZLIntegerOption VerticalShiftOption;

	public ZLBoolean3Option AllowHyphenationsOption;

	public ZLTextStyleDecoration(ZLTextControlEntry entry) {
		if (entry.getKind() == BOLD) {
			BoldOption = new ZLBoolean3Option(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, ":bold", ZLBoolean3.B3_TRUE);
			FontSizeDeltaOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, ":defaultFont", -10, 100, 0);
		} else if (entry.getKind() == LARGE_FONT_SIZE) {
			BoldOption = new ZLBoolean3Option(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, ":bold", ZLBoolean3.B3_FALSE);
			FontSizeDeltaOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, STYLE, ":largeFont", -10, 100, 22);
		}	
	}
	
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
