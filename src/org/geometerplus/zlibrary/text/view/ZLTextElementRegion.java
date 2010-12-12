/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import org.geometerplus.zlibrary.core.view.ZLPaintContext;

abstract class ZLTextElementRegion {
	static interface Filter {
		boolean accepts(ZLTextElementRegion region);
	}

	static Filter Filter = new Filter() {
		public boolean accepts(ZLTextElementRegion region) {
			return true;
		}
	};

	private final List<ZLTextElementArea> myList;
	private final int myFromIndex;
	private int myToIndex;
	private ZLTextHorizontalConvexHull myHull;

	ZLTextElementRegion(List<ZLTextElementArea> list, int fromIndex) {
		myList = list;
		myFromIndex = fromIndex;
		myToIndex = fromIndex + 1;
	}

	void extend() {
		++myToIndex;
		myHull = null;
	}

	private List<ZLTextElementArea> textAreas() {
		return myList.subList(myFromIndex, myToIndex);
	}
	private ZLTextHorizontalConvexHull convexHull() {
		if (myHull == null) {
			myHull = new ZLTextHorizontalConvexHull(textAreas());
		}
		return myHull;
	}

	public void draw(ZLPaintContext context) {
		convexHull().draw(context);
	}

	public int distanceTo(int x, int y) {
		return convexHull().distanceTo(x, y);
	}

	public boolean isAtRightOf(ZLTextElementRegion other) {
		return
			other == null ||
			myList.get(myFromIndex).XStart >= other.myList.get(other.myToIndex - 1).XEnd;
	}

	public boolean isAtLeftOf(ZLTextElementRegion other) {
		return other == null || other.isAtRightOf(this);
	}

	public boolean isUnder(ZLTextElementRegion other) {
		return
			other == null ||
			myList.get(myFromIndex).YStart >= other.myList.get(other.myToIndex - 1).YEnd;
	}

	public boolean isOver(ZLTextElementRegion other) {
		return other == null || other.isUnder(this);
	}

	public boolean isExactlyUnder(ZLTextElementRegion other) {
		if (other == null) {
			return true;
		}
		if (!isUnder(other)) {
			return false;
		}
		final List<ZLTextElementArea> areas0 = textAreas();
		final List<ZLTextElementArea> areas1 = other.textAreas();
		for (ZLTextElementArea i : areas0) {
			for (ZLTextElementArea j : areas1) {
				if (i.XStart <= j.XEnd && j.XStart <= i.XEnd) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isExactlyOver(ZLTextElementRegion other) {
		return other == null || other.isExactlyUnder(this);
	}
}
