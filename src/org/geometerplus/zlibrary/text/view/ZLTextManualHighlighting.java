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

package org.geometerplus.zlibrary.text.view;

import org.geometerplus.zlibrary.core.util.ZLColor;

class ZLTextManualHighlighting extends ZLTextHighlighting {
	private final ZLTextView myView;
	private final ZLTextPosition myStartPosition;
	private final ZLTextPosition myEndPosition;

	ZLTextManualHighlighting(ZLTextView view, ZLTextPosition start, ZLTextPosition end) {
		myView = view;
		myStartPosition = new ZLTextFixedPosition(start);
		myEndPosition = new ZLTextFixedPosition(end);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public ZLTextPosition getStartPosition() {
		return myStartPosition;
	}

	@Override
	public ZLTextPosition getEndPosition() {
		return myEndPosition;
	}

	@Override
	public ZLTextElementArea getStartArea(ZLTextPage page) {
		return page.TextElementMap.getFirstAfter(myStartPosition);
	}

	@Override
	public ZLTextElementArea getEndArea(ZLTextPage page) {
		return page.TextElementMap.getLastBefore(myEndPosition);
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myView.getHighlightingBackgroundColor();
	}
}
