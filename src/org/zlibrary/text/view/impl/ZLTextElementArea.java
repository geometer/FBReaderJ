package org.zlibrary.text.view.impl;

import org.zlibrary.text.view.ZLTextStyle;

/*package*/ class ZLTextElementArea extends ZLTextRectangularArea { 
	public int ParagraphNumber;
	public int TextElementNumber;
	public int StartCharNumber;	
	public int Length;
	public boolean AddHyphenationSign;
	public boolean ChangeStyle;
	public ZLTextStyle Style;
	public ZLTextElement Element;

	public ZLTextElementArea(int paragraphNumber, int textElementNumber, int startCharNumber, int length, boolean addHyphenationSign, boolean changeStyle, ZLTextStyle style, ZLTextElement element, int xStart, int xEnd, int yStart, int yEnd) {
		super(xStart, xEnd, yStart, yEnd);
		ParagraphNumber = paragraphNumber;
		TextElementNumber = textElementNumber;
		StartCharNumber = startCharNumber;
		Length = length;
		AddHyphenationSign = addHyphenationSign;
		ChangeStyle = changeStyle;
		Style = style;
		Element = element;
	}
}
