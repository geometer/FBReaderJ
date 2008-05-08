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
		final int ParagraphIndex;
		final boolean[] VerticalLinesStack;

		TreeNodeInfo(boolean isLeaf, boolean isOpen, boolean isFirstLine, int paragraphIndex, boolean[] stack) {
			IsLeaf = isLeaf;
			IsOpen = isOpen;
			IsFirstLine = isFirstLine;
			ParagraphIndex = paragraphIndex;
			VerticalLinesStack = stack;
		}
	};
	TreeNodeInfo NodeInfo;

	final ZLTextParagraphCursor ParagraphCursor;
	final int ParagraphCursorLength;

	final int StartWordIndex;
	final int StartCharIndex;
	int RealStartWordIndex;
	int RealStartCharIndex;
	int EndWordIndex;
	int EndCharIndex;

	boolean IsVisible;
	int LeftIndent;
	int Width;
	int Height;
	int Descent;
	int VSpaceAfter;
	int SpaceCounter;
	ZLTextStyle StartStyle;

	ZLTextLineInfo(ZLTextParagraphCursor paragraphCursor, int wordIndex, int charIndex, ZLTextStyle style) {
		ParagraphCursor = paragraphCursor;
		ParagraphCursorLength = paragraphCursor.getParagraphLength();

		StartWordIndex = wordIndex;
		StartCharIndex = charIndex;
		RealStartWordIndex = wordIndex;
		RealStartCharIndex = charIndex;
		EndWordIndex = wordIndex;
		EndCharIndex = charIndex;

		StartStyle = style;
	}

	boolean isEndOfParagraph() {
		return EndWordIndex == ParagraphCursorLength;
	}

	public boolean equals(Object o) {
		ZLTextLineInfo info = (ZLTextLineInfo)o;
		return
			(ParagraphCursor == info.ParagraphCursor) &&
			(StartWordIndex == info.StartWordIndex) &&
			(StartCharIndex == info.StartCharIndex);
	}

	public int hashCode() {
		return ParagraphCursor.hashCode() + StartWordIndex + 239 * StartCharIndex;
	}
}
