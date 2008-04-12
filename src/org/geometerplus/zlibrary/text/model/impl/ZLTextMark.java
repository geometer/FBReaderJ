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

package org.geometerplus.zlibrary.text.model.impl;

public class ZLTextMark {
	public int ParagraphNumber;
	public int Offset;
	public int Length;

	public ZLTextMark() {
		ParagraphNumber = -1;
		Offset = -1;
		Length = -1;
	}

	public ZLTextMark(int paragraphNumber, int offset, int length) {
		ParagraphNumber = paragraphNumber;
		Offset = offset;
		Length = length;
	}

	public ZLTextMark(final ZLTextMark mark) {
		ParagraphNumber = mark.ParagraphNumber;
		Offset = mark.Offset;
		Length = mark.Length;
	}

	public int compareTo(ZLTextMark mark) {
		if (ParagraphNumber == mark.ParagraphNumber) {
			if (Offset == mark.Offset) {
				return 0;
			} else if (Offset < mark.Offset) {
				return -1;
			} else {
				return 1;
			}
		} else if (ParagraphNumber < mark.ParagraphNumber) {
			return -1;
		} else {
			return 1;
		}
	}

	public String toString() {
		return (ParagraphNumber + " " + Offset + " " + Length);
	}
}
