/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.graphics.Rect;

import org.geometerplus.zlibrary.core.view.*;

abstract class HullUtil {
	static Hull hull(ZLTextElementArea[] areas) {
		return hull(Arrays.asList(areas));
	}

	static Hull hull(List<ZLTextElementArea> areas) {
		final List<Rect> rectangles0 = new ArrayList<Rect>(areas.size());
		final List<Rect> rectangles1 = new ArrayList<Rect>(areas.size());
		for (ZLTextElementArea a : areas) {
			final Rect rect = new Rect(a.XStart, a.YStart, a.XEnd, a.YEnd);
			if (a.ColumnIndex == 0) {
				rectangles0.add(rect);
			} else {
				rectangles1.add(rect);
			}
		}
		if (rectangles0.isEmpty()) {
			return new HorizontalConvexHull(rectangles1);
		} else if (rectangles1.isEmpty()) {
			return new HorizontalConvexHull(rectangles0);
		} else {
			return new UnionHull(
				new HorizontalConvexHull(rectangles0),
				new HorizontalConvexHull(rectangles1)
			);
		}
	}
}
