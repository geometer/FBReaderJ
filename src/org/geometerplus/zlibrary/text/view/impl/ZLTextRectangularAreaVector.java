/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
package org.geometerplus.zlibrary.text.view.impl;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

final class ZLTextRectangularAreaVector extends ArrayList {
	ZLTextRectangularArea getArea(int index) {
		return (ZLTextRectangularArea)super.get(index);
	}

	ZLTextRectangularArea binarySearch(int x, int y) {
		int left = 0;
		int right = size();
		while (left < right) {
			final int middle = (left + right) / 2;
			final ZLTextRectangularArea candidate = (ZLTextRectangularArea)get(middle);
			if (candidate.YStart > y) {
				right = middle;
			} else if (candidate.YEnd < y) {
				left = middle + 1;
			} else if (candidate.XStart > x) {
				right = middle;
			} else if (candidate.XEnd < x) {
				left = middle + 1;
			} else {
				return candidate;
			}
		}
		return null;
	}

	ZLTextRectangularArea binarySearch(int y) {
		int left = 0;
		int right = size();
		while (left < right) {
			final int middle = (left + right) / 2;
			final ZLTextRectangularArea candidate = (ZLTextRectangularArea)get(middle);
			if (candidate.YStart > y) {
				right = middle;
			} else if (candidate.YEnd < y) {
				left = middle + 1;
			} else {
				return candidate;
			}
		}
		return null;
	}
}
