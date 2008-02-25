package org.zlibrary.text.view.style;

import org.zlibrary.core.util.ZLColor;

import org.zlibrary.text.view.ZLTextStyle;

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
