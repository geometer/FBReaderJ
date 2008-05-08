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

final class ZLTextRectangularAreaVector {
	private ZLTextRectangularArea[] myData = new ZLTextRectangularArea[10];
	private int myLength;	

	public boolean isEmpty() {
		return myLength == 0;
	}

	public int size() {
		return myLength;
	}

	public void add(ZLTextRectangularArea area) {
		final int index = myLength++;
		if (index == myData.length) {
			ZLTextRectangularArea[] extended = new ZLTextRectangularArea[2 * index];
			System.arraycopy(myData, 0, extended, 0, index);
			myData = extended;
		}
		myData[index] = area;
	}

	public void clear() {
		final ZLTextRectangularArea[] data = myData;
		for (int i = myLength - 1; i >= 0; --i) {
			data[i] = null;
		}
		myLength = 0;
	}

	ZLTextRectangularArea get(int index) {
		return myData[index];
	}

	ZLTextRectangularArea binarySearch(int x, int y) {
		int left = 0;
		int right = myLength;
		while (left < right) {
			final int middle = (left + right) / 2;
			final ZLTextRectangularArea candidate = myData[middle];
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
		int right = myLength;
		while (left < right) {
			final int middle = (left + right) / 2;
			final ZLTextRectangularArea candidate = myData[middle];
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
