package org.zlibrary.text.view;

import org.zlibrary.core.util.ZLColor;

public interface ZLTextStyle {
	String getFontFamily();
	int getFontSize();

	ZLColor getColor();

	boolean isBold();
	boolean isItalic();
	int getLeftIndent();
	int getRightIndent();
	int getFirstLineIndentDelta();
	int getLineSpacePercent();
	int getVerticalShift();
	int getSpaceBefore();
	int getSpaceAfter();
	byte getAlignment();

	ZLTextStyle getBase();
}
