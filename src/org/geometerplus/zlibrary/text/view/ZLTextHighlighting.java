/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

class ZLTextHighlighting implements ZLTextAbstractHighlighting {
	private ZLTextPosition myStartPosition;
	private ZLTextPosition myEndPosition;

	void setup(ZLTextPosition start, ZLTextPosition end) {
		myStartPosition = new ZLTextFixedPosition(start);
		myEndPosition = new ZLTextFixedPosition(end);
	}

	public boolean clear() {
		if (isEmpty()) {
			return false;
		}
		myStartPosition = null;
		myEndPosition = null;
		return true;
	}

	public boolean isEmpty() {
		return myStartPosition == null;
	}

	public ZLTextPosition getStartPosition() {
		return myStartPosition;
	}

	public ZLTextPosition getEndPosition() {
		return myEndPosition;
	}

	public ZLTextElementArea getStartArea(ZLTextPage page) {
		return page.TextElementMap.getFirstAfter(myStartPosition);
	}

	public ZLTextElementArea getEndArea(ZLTextPage page) {
		return page.TextElementMap.getLastBefore(myEndPosition);
	}
}
