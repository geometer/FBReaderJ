package org.zlibrary.text.view.impl;

import java.util.ArrayList;

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
