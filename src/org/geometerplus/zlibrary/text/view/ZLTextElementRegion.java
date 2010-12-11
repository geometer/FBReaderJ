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
	}

	public List<ZLTextElementArea> textAreas() {
		return myList.subList(myFromIndex, myToIndex);
	}

	public void draw(ZLPaintContext context) {
		if (myHull == null) {
			myHull = new ZLTextHorizontalConvexHull(textAreas());
		}
		myHull.draw(context);
	}

	public int distanceTo(int x, int y) {
		if (myHull == null) {
			myHull = new ZLTextHorizontalConvexHull(textAreas());
		}
		return myHull.distanceTo(x, y);
	}
}
