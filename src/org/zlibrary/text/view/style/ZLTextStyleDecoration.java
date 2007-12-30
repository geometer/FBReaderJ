package org.zlibrary.text.view.style;

import org.zlibrary.core.options.*;
import org.zlibrary.core.util.*;

import org.zlibrary.text.view.ZLTextStyle;

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
