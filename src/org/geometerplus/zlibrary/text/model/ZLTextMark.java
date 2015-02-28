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

package org.geometerplus.zlibrary.text.model;

public class ZLTextMark implements Comparable<ZLTextMark> {
	public final int ParagraphIndex;
	public final int Offset;
	public final int Length;

	public ZLTextMark(int paragraphIndex, int offset, int length) {
		ParagraphIndex = paragraphIndex;
		Offset = offset;
		Length = length;
	}

	public ZLTextMark(final ZLTextMark mark) {
		ParagraphIndex = mark.ParagraphIndex;
		Offset = mark.Offset;
		Length = mark.Length;
	}

	public int compareTo(ZLTextMark mark) {
		final int diff = ParagraphIndex - mark.ParagraphIndex;
		return diff != 0 ? diff : Offset - mark.Offset;
	}

	public String toString() {
		return ParagraphIndex + " " + Offset + " " + Length;
	}
}
