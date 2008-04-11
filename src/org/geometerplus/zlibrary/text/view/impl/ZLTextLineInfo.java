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

import org.geometerplus.zlibrary.text.view.*;

final class ZLTextLineInfo {
	static final class TreeNodeInfo {
		final boolean IsLeaf;
		final boolean IsOpen;
		final boolean IsFirstLine;
		final int ParagraphNumber;
		final boolean[] VerticalLinesStack;

		TreeNodeInfo(boolean isLeaf, boolean isOpen, boolean isFirstLine, int paragraphNumber, boolean[] stack) {
			IsLeaf = isLeaf;
			IsOpen = isOpen;
			IsFirstLine = isFirstLine;
			ParagraphNumber = paragraphNumber;
			VerticalLinesStack = stack;
		}
	};
	TreeNodeInfo NodeInfo;

	final ZLTextParagraphCursor ParagraphCursor;
	final int ParagraphCursorLength;

	final int StartWordNumber;
	final int StartCharNumber;
	int RealStartWordNumber;
	int RealStartCharNumber;
	int EndWordNumber;
	int EndCharNumber;

	boolean IsVisible;
	int LeftIndent;
	int Width;
	int Height;
	int Descent;
	int VSpaceAfter;
	int SpaceCounter;
	ZLTextStyle StartStyle;

	ZLTextLineInfo(ZLTextParagraphCursor paragraphCursor, int wordNumber, int charNumber, ZLTextStyle style) {
		ParagraphCursor = paragraphCursor;
		ParagraphCursorLength = paragraphCursor.getParagraphLength();

		StartWordNumber = wordNumber;
		StartCharNumber = charNumber;
		RealStartWordNumber = wordNumber;
		RealStartCharNumber = charNumber;
		EndWordNumber = wordNumber;
		EndCharNumber = charNumber;

		StartStyle = style;
	}

	boolean isEndOfParagraph() {
		return EndWordNumber == ParagraphCursorLength;
	}

	public boolean equals(Object o) {
		ZLTextLineInfo info = (ZLTextLineInfo)o;
		return
			(ParagraphCursor == info.ParagraphCursor) &&
			(StartWordNumber == info.StartWordNumber) &&
			(StartCharNumber == info.StartCharNumber);
	}

	public int hashCode() {
		return ParagraphCursor.hashCode() + StartWordNumber + 239 * StartCharNumber;
	}
}
