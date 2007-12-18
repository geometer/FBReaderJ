package org.zlibrary.text.view.impl;

import java.util.ArrayList;

class ZLTextRectangularArea {
	final int XStart;	
	final int XEnd;	
	final int YStart;	
	final int YEnd;	
	
	ZLTextRectangularArea(int xStart, int xEnd, int yStart, int yEnd) {
		XStart = xStart;
		XEnd = xEnd;
		YStart = yStart;
		YEnd = yEnd;
	}

	static <T extends ZLTextRectangularArea> T binarySearch(ArrayList<T> vector, int x, int y) {
		int left = 0;
		int right = vector.size();
		while (left < right) {
			final int middle = (left + right) / 2;
			final T candidate = vector.get(middle);
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
}
