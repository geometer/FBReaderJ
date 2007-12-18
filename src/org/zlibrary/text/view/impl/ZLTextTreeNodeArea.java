package org.zlibrary.text.view.impl;

final class ZLTextTreeNodeArea extends ZLTextRectangularArea {
	final int ParagraphNumber;
	ZLTextTreeNodeArea(int paragraphNumber, int xStart, int xEnd, int yStart, int yEnd) {
		super(xStart, xEnd, yStart, yEnd);
		ParagraphNumber = paragraphNumber;
	}
}
