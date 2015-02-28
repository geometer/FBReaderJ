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

public abstract class ZLTextSimpleHighlighting extends ZLTextHighlighting {
	protected final ZLTextView View;
	private final ZLTextPosition myStartPosition;
	private final ZLTextPosition myEndPosition;

	protected ZLTextSimpleHighlighting(ZLTextView view, ZLTextPosition start, ZLTextPosition end) {
		View = view;
		myStartPosition = new ZLTextFixedPosition(start);
		myEndPosition = new ZLTextFixedPosition(end);
	}

	@Override
	public final boolean isEmpty() {
		return false;
	}

	@Override
	public final ZLTextPosition getStartPosition() {
		return myStartPosition;
	}

	@Override
	public final ZLTextPosition getEndPosition() {
		return myEndPosition;
	}

	@Override
	public final ZLTextElementArea getStartArea(ZLTextPage page) {
		return page.TextElementMap.getFirstAfter(myStartPosition);
	}

	@Override
	public final ZLTextElementArea getEndArea(ZLTextPage page) {
		return page.TextElementMap.getLastBefore(myEndPosition);
	}
}
