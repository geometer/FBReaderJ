/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.book.Bookmark;

public final class BookmarkHighlighting extends ZLTextSimpleHighlighting {
	private static ZLTextPosition endPosition(Bookmark bookmark) {
		final ZLTextPosition end = bookmark.getEnd();
		if (end != null) {
			return end;
		}
		// TODO: compute end and save bookmark
		return bookmark;
	}

	BookmarkHighlighting(ZLTextView view, Bookmark bookmark) {
		super(view, bookmark, endPosition(bookmark));
	}

	@Override
	public ZLColor getBackgroundColor() {
		return new ZLColor(127, 127, 127);
	}
}
