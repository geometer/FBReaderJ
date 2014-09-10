/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.book.*;

public final class NoteHighlighting extends ZLTextSimpleHighlighting {
	final IBookCollection Collection;
	final Note Note;

	private static ZLTextPosition startPosition(Note note) {
		return new ZLTextFixedPosition(note.getParagraphIndex(), note.getElementIndex(), 0);
	}

	private static ZLTextPosition endPosition(Note note) {
		final ZLTextPosition end = note.getEnd();
		if (end != null) {
			return end;
		}
		// TODO: compute end and save note
		return note;
	}

	NoteHighlighting(ZLTextView view, IBookCollection collection, Note note) {
		super(view, startPosition(note), endPosition(note));
		Collection = collection;
		Note = note;
	}

	@Override
	public ZLColor getBackgroundColor() {
		final HighlightingStyle bmStyle = Collection.getHighlightingStyle(3);
		return bmStyle != null ? bmStyle.getBackgroundColor() : null;
	}
}
