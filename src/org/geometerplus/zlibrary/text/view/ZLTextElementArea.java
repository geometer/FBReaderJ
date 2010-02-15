/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view;

final class ZLTextElementArea extends ZLTextFixedPosition { 
	final int XStart;	
	final int XEnd;	
	final int YStart;	
	final int YEnd;	
	
	final int Length;
	final boolean AddHyphenationSign;
	final boolean ChangeStyle;
	final ZLTextStyle Style;
	final ZLTextElement Element;

	ZLTextElementArea(int paragraphIndex, int elementIndex, int charIndex, int length, boolean addHyphenationSign, boolean changeStyle, ZLTextStyle style, ZLTextElement element, int xStart, int xEnd, int yStart, int yEnd) {
		super(paragraphIndex, elementIndex, charIndex);

		XStart = xStart;
		XEnd = xEnd;
		YStart = yStart;
		YEnd = yEnd;

		Length = length;
		AddHyphenationSign = addHyphenationSign;
		ChangeStyle = changeStyle;
		Style = style;
		Element = element;
	}

	boolean contains(int x, int y) {
		return (y >= YStart) && (y <= YEnd) && (x >= XStart) && (x <= XEnd);
	}
}
