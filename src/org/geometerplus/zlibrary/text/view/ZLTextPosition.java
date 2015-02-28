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

public abstract class ZLTextPosition implements Comparable<ZLTextPosition> {
	public abstract int getParagraphIndex();
	public abstract int getElementIndex();
	public abstract int getCharIndex();

	public boolean samePositionAs(ZLTextPosition position) {
		return
			getParagraphIndex() == position.getParagraphIndex() &&
			getElementIndex() == position.getElementIndex() &&
			getCharIndex() == position.getCharIndex();
	}

	public int compareTo(ZLTextPosition position) {
		final int p0 = getParagraphIndex();
		final int p1 = position.getParagraphIndex();
		if (p0 != p1) {
			return p0 < p1 ? -1 : 1;
		}

		final int e0 = getElementIndex();
		final int e1 = position.getElementIndex();
		if (e0 != e1) {
			return e0 < e1 ? -1 : 1;
		}

		return getCharIndex() - position.getCharIndex();
	}

	public int compareToIgnoreChar(ZLTextPosition position) {
		final int p0 = getParagraphIndex();
		final int p1 = position.getParagraphIndex();
		if (p0 != p1) {
			return p0 < p1 ? -1 : 1;
		}

		return getElementIndex() - position.getElementIndex();
	}

	@Override
	public int hashCode() {
		return (getParagraphIndex() << 16) + (getElementIndex() << 8) + getCharIndex();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof ZLTextPosition)) {
			return false;
		}
		final ZLTextPosition position = (ZLTextPosition)object;
		return
			getParagraphIndex() == position.getParagraphIndex() &&
			getElementIndex() == position.getElementIndex() &&
			getCharIndex() == position.getCharIndex();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + getParagraphIndex() + "," + getElementIndex() + "," + getCharIndex() + "]";
	}
}
