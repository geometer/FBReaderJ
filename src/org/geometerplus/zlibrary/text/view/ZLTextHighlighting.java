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

package org.geometerplus.zlibrary.text.view;

import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class ZLTextHighlighting implements Comparable<ZLTextHighlighting> {
	public abstract boolean isEmpty();

	public abstract ZLTextPosition getStartPosition();
	public abstract ZLTextPosition getEndPosition();
	public abstract ZLTextElementArea getStartArea(ZLTextPage page);
	public abstract ZLTextElementArea getEndArea(ZLTextPage page);

	public abstract ZLColor getForegroundColor();
	public abstract ZLColor getBackgroundColor();

	boolean intersects(ZLTextPage page) {
		return
			!isEmpty() &&
			!page.StartCursor.isNull() && !page.EndCursor.isNull() &&
			page.StartCursor.compareTo(getEndPosition()) < 0 &&
			page.EndCursor.compareTo(getStartPosition()) > 0;
	}

	boolean intersects(ZLTextRegion region) {
		final ZLTextRegion.Soul soul = region.getSoul();
		return
			!isEmpty() &&
			soul.compareTo(getStartPosition()) >= 0 &&
			soul.compareTo(getEndPosition()) <= 0;
	}

	public int compareTo(ZLTextHighlighting highlighting) {
		final int cmp = getStartPosition().compareTo(highlighting.getStartPosition());
		return cmp != 0 ? cmp : getEndPosition().compareTo(highlighting.getEndPosition());
	}
}
