package org.zlibrary.text.view;

import org.zlibrary.core.util.ZLColor;

public interface ZLTextStyle {
	String getFontFamily();
	int getFontSize();

	ZLColor getColor();

	boolean bold();
	boolean italic();
	int leftIndent();
	int rightIndent();
	int firstLineIndentDelta();
	double lineSpace();
	int verticalShift();
	int spaceBefore();
	int spaceAfter();
	boolean isDecorated();
	byte alignment();
}
