/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

final class ZLTextLineInfo {
	final ZLTextParagraphCursor ParagraphCursor;
	final int ParagraphCursorLength;

	final int StartElementIndex;
	final int StartCharIndex;
	int RealStartElementIndex;
	int RealStartCharIndex;
	int EndElementIndex;
	int EndCharIndex;

	boolean IsVisible;
	int LeftIndent;
	int Width;
	int Height;
	int Descent;
	int VSpaceBefore;
	int VSpaceAfter;
	boolean PreviousInfoUsed;
	int SpaceCounter;
	ZLTextStyle StartStyle;

	ZLTextLineInfo(ZLTextParagraphCursor paragraphCursor, int elementIndex, int charIndex, ZLTextStyle style) {
		ParagraphCursor = paragraphCursor;
		ParagraphCursorLength = paragraphCursor.getParagraphLength();

		StartElementIndex = elementIndex;
		StartCharIndex = charIndex;
		RealStartElementIndex = elementIndex;
		RealStartCharIndex = charIndex;
		EndElementIndex = elementIndex;
		EndCharIndex = charIndex;

		StartStyle = style;
	}

	boolean isEndOfParagraph() {
		return EndElementIndex == ParagraphCursorLength;
	}

	void adjust(ZLTextLineInfo previous) {
		if (!PreviousInfoUsed && previous != null) {
			Height -= Math.min(previous.VSpaceAfter, VSpaceBefore);
			PreviousInfoUsed = true;
		}
	}

	@Override
	public boolean equals(Object o) {
		ZLTextLineInfo info = (ZLTextLineInfo)o;
		return
			(ParagraphCursor == info.ParagraphCursor) &&
			(StartElementIndex == info.StartElementIndex) &&
			(StartCharIndex == info.StartCharIndex);
	}

	@Override
	public int hashCode() {
		return ParagraphCursor.hashCode() + StartElementIndex + 239 * StartCharIndex;
	}
}
