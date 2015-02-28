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

public class ZLTextFixedPosition extends ZLTextPosition {
	public final int ParagraphIndex;
	public final int ElementIndex;
	public final int CharIndex;

	public ZLTextFixedPosition(int paragraphIndex, int elementIndex, int charIndex) {
		ParagraphIndex = paragraphIndex;
		ElementIndex = elementIndex;
		CharIndex = charIndex;
	}

	public ZLTextFixedPosition(ZLTextPosition position) {
		ParagraphIndex = position.getParagraphIndex();
		ElementIndex = position.getElementIndex();
		CharIndex = position.getCharIndex();
	}

	public final int getParagraphIndex() {
		return ParagraphIndex;
	}

	public final int getElementIndex() {
		return ElementIndex;
	}

	public final int getCharIndex() {
		return CharIndex;
	}

	public static class WithTimestamp extends ZLTextFixedPosition {
		public final long Timestamp;

		public WithTimestamp(int paragraphIndex, int elementIndex, int charIndex, Long stamp) {
			super(paragraphIndex, elementIndex, charIndex);
			Timestamp = stamp != null ? stamp : -1;
		}

		@Override
		public String toString() {
			return super.toString() + "; timestamp = " + Timestamp;
		}
	}
}
