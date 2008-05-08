/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.view.impl;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;

public class ZLTextElementArea extends ZLTextRectangularArea { 
	public final int ParagraphIndex;
	public final int TextElementIndex;
	final int StartCharIndex;	
	final int Length;
	final boolean AddHyphenationSign;
	final boolean ChangeStyle;
	final ZLTextStyle Style;
	public final ZLTextElement Element;

	ZLTextElementArea(int paragraphIndex, int textElementIndex, int startCharIndex, int length, boolean addHyphenationSign, boolean changeStyle, ZLTextStyle style, ZLTextElement element, int xStart, int xEnd, int yStart, int yEnd) {
		super(xStart, xEnd, yStart, yEnd);
		ParagraphIndex = paragraphIndex;
		TextElementIndex = textElementIndex;
		StartCharIndex = startCharIndex;
		Length = length;
		AddHyphenationSign = addHyphenationSign;
		ChangeStyle = changeStyle;
		Style = style;
		Element = element;
	}
}
