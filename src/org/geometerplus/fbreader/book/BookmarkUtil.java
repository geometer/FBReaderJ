/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.book;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.*;

public abstract class BookmarkUtil {
	public static String getStyleName(HighlightingStyle style) {
		final String name = style.getNameOrNull();
		return (name != null && name.length() > 0) ? name : defaultName(style);
	}

	public static void setStyleName(HighlightingStyle style, String name) {
		style.setName(defaultName(style).equals(name) ? null : name);
	}

	private static String defaultName(HighlightingStyle style) {
		return ZLResource.resource("style").getValue().replace("%s", String.valueOf(style.Id));
	}

	public static void findEnd(Bookmark bookmark, ZLTextView view) {
		if (bookmark.getEnd() != null) {
			return;
		}
		ZLTextWordCursor cursor = view.getStartCursor();
		if (cursor.isNull()) {
			cursor = view.getEndCursor();
		}
		if (cursor.isNull()) {
			return;
		}
		cursor = new ZLTextWordCursor(cursor);
		cursor.moveTo(bookmark);

		ZLTextWord word = null;
mainLoop:
		for (int count = bookmark.getLength(); count > 0; cursor.nextWord()) {
			while (cursor.isEndOfParagraph()) {
				if (!cursor.nextParagraph()) {
					break mainLoop;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				if (word != null) {
					--count;
				}
				word = (ZLTextWord)element;
				count -= word.Length;
			}
		}
		if (word != null) {
			bookmark.setEnd(cursor.getParagraphIndex(), cursor.getElementIndex(), word.Length);
		}
	}
}
