package org.zlibrary.text.view.style;

import org.zlibrary.core.options.*;
import org.zlibrary.core.options.util.*;

import org.zlibrary.text.view.ZLTextStyle;

import org.zlibrary.text.model.entry.*;

public class ZLTextStyleDecoration {
	public static final String STYLE = "Style";
	public static final byte BOLD = 28;
	public static final byte LARGE_FONT_SIZE = 31;

	public final ZLStringOption FontFamilyOption;
	public final ZLIntegerRangeOption FontSizeDeltaOption;
	public final ZLBoolean3Option BoldOption;
	public final ZLBoolean3Option ItalicOption;
	public final ZLIntegerOption VerticalShiftOption;
	public final ZLBoolean3Option AllowHyphenationsOption;

	private final String myName;
	private HyperlinkStyle myHyperlinkStyle;

	public ZLTextStyleDecoration(String name, int fontSizeDelta, ZLBoolean3 bold, ZLBoolean3 italic, int verticalShift, ZLBoolean3 allowHyphenations) {
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

	public HyperlinkStyle getHyperlinkStyle() {
		return myHyperlinkStyle;
	}

	public void setHyperlinkStyle(HyperlinkStyle hyperlinkStyle) {
		myHyperlinkStyle = hyperlinkStyle;
	}
}
