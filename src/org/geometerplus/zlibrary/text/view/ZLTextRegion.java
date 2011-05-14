/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

public abstract class ZLTextRegion {
	public static interface Filter {
		boolean accepts(ZLTextRegion region);
	}

	public static Filter AnyRegionFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			return true;
		}
	};

	public static Filter HyperlinkFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			return region instanceof ZLTextHyperlinkRegion;
		}
	};

	public static Filter ImageOrHyperlinkFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			return
				region instanceof ZLTextImageRegion ||
				region instanceof ZLTextHyperlinkRegion;
		}
	};

	private final List<ZLTextElementArea> myList;
	private final int myFromIndex;
	private int myToIndex;
	private ZLTextHorizontalConvexHull myHull;

	ZLTextRegion(List<ZLTextElementArea> list, int fromIndex) {
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

	public int getTop() {
		return myList.get(myFromIndex).YStart;
	}

	public int getBottom() {
		return myList.get(myToIndex - 1).YEnd;
	}

	void draw(ZLPaintContext context) {
		convexHull().draw(context);
	}

	int distanceTo(int x, int y) {
		return convexHull().distanceTo(x, y);
	}

	boolean isAtRightOf(ZLTextRegion other) {
		return
			other == null ||
			myList.get(myFromIndex).XStart >= other.myList.get(other.myToIndex - 1).XEnd;
	}

	boolean isAtLeftOf(ZLTextRegion other) {
		return other == null || other.isAtRightOf(this);
	}

	boolean isUnder(ZLTextRegion other) {
		return
			other == null ||
			myList.get(myFromIndex).YStart >= other.myList.get(other.myToIndex - 1).YEnd;
	}

	boolean isOver(ZLTextRegion other) {
		return other == null || other.isUnder(this);
	}

	boolean isExactlyUnder(ZLTextRegion other) {
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

	boolean isExactlyOver(ZLTextRegion other) {
		return other == null || other.isExactlyUnder(this);
	}

	@Override
	public abstract boolean equals(Object other);
}
