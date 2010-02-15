/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

public abstract class ZLTextPosition implements Comparable<ZLTextPosition> {
	public abstract int getParagraphIndex();
	public abstract int getElementIndex();
	public abstract int getCharIndex();

	public boolean samePositionAs(ZLTextPosition position) {
		return
			(getParagraphIndex() == position.getParagraphIndex()) &&
			(getElementIndex() == position.getElementIndex()) &&
			(getCharIndex() == position.getCharIndex());
	}

	public int compareTo(ZLTextPosition position) {
		final int pDiff = getParagraphIndex() - position.getParagraphIndex();
		if (pDiff != 0) {
			return pDiff;
		}

		final int eDiff = getElementIndex() - position.getElementIndex();
		if (eDiff != 0) {
			return eDiff;
		}

		return getCharIndex() - position.getCharIndex();
	}
}
