/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.text.view.impl.ZLTextWordCursor;

public final class ZLTextPosition implements Comparable<ZLTextPosition> {
	public int ParagraphIndex;
	public int WordIndex;
	public int CharIndex;

	public ZLTextPosition(int paragraphIndex, int wordIndex, int charIndex) {
		ParagraphIndex = paragraphIndex;
		WordIndex = wordIndex;
		CharIndex = charIndex;
	}

	public ZLTextPosition(ZLTextWordCursor cursor) {
		set(cursor);
	}

	public void set(ZLTextWordCursor cursor) {
		if (!cursor.isNull()) {
			ParagraphIndex = cursor.getParagraphCursor().Index;
			WordIndex = cursor.getWordIndex();
			CharIndex = cursor.getCharIndex();
		}
	}

	public boolean equalsToCursor(ZLTextWordCursor cursor) {
		return
			(ParagraphIndex == cursor.getParagraphCursor().Index) &&
			(WordIndex == cursor.getWordIndex()) &&
			(CharIndex == cursor.getCharIndex());
	}

	public int compareTo(ZLTextPosition position) {
		if (ParagraphIndex != position.ParagraphIndex) {
			return ParagraphIndex - position.ParagraphIndex;
		}
		if (WordIndex != position.WordIndex) {
			return WordIndex - position.WordIndex;
		}
		return CharIndex - position.CharIndex;
	}
}
