/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

public class ZLTextSelection {
	private final ZLTextView myView;

	private ZLTextRegion myInitialRegion;
	private ZLTextElementArea myLeftBound;
	private ZLTextElementArea myRightBound;

	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
	}

	boolean isEmpty() {
		return myInitialRegion == null;
	}

	boolean clear() {
		if (isEmpty()) {
			return false;
		}

		stop();
		myInitialRegion = null;
		myLeftBound = null;
		myRightBound = null;
		return true;
	}

	boolean start(int x, int y) {
		clear();
		myInitialRegion = myView.findRegion(x, y, ZLTextView.MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (myInitialRegion == null) {
			return false;
		}

		myLeftBound = myInitialRegion.getFirstArea();
		myRightBound = myInitialRegion.getLastArea();
		return true;
	}

	void stop() {
		if (myScroller != null) {
			myScroller.stop();
			myScroller = null;
		}
	}

	boolean expandTo(int x, int y) {
		if (myInitialRegion == null) {
			return start(x, y);
		}

		if (y < 10) {
			if (myScroller != null && myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(false, x, y);
				return false;
			}
		} else if (y > myView.getTextAreaHeight() - 10) {
			if (myScroller != null && !myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(true, x, y);
				return false;
			}
		} else {
			if (myScroller != null) {
				myScroller.stop();
				myScroller = null;
			}
		}

		if (myScroller != null) {
			myScroller.setXY(x, y);
		}

		ZLTextRegion region = myView.findRegion(x, y, ZLTextView.MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region == null && myScroller != null) {
			region = myView.findRegion(x, y, ZLTextRegion.AnyRegionFilter);
		}
		if (region == null) {
			return false;
		}

		final int cmp = myInitialRegion.compareTo(region);
		final ZLTextElementArea firstArea = region.getFirstArea();
		final ZLTextElementArea lastArea = region.getLastArea();
		if (cmp < 0) {
			if (myRightBound.compareTo(lastArea) != 0) {
				myRightBound = lastArea;
				return true;
			}
		} else if (cmp > 0) {
			if (myLeftBound.compareTo(firstArea) != 0) {
				myLeftBound = firstArea;
				return true;
			}
		} else {
			if (myLeftBound.compareTo(firstArea) != 0 || myRightBound.compareTo(lastArea) != 0) {
				myLeftBound = firstArea;
				myRightBound = lastArea;
				return true;
			}
		}
		return false;
	}

	boolean isAreaSelected(ZLTextElementArea area) {
		return
			!isEmpty()
			&& myLeftBound.weakCompareTo(area) <= 0
			&& myRightBound.weakCompareTo(area) >= 0;
	}

	ZLTextElementArea getStartArea() {
		return myLeftBound;
	}

	ZLTextElementArea getEndArea() {
		return myRightBound;
	}

	private class Scroller implements Runnable {
		private final boolean myScrollForward;
		private int myX, myY;

		Scroller(boolean forward, int x, int y) {
			myScrollForward = forward;
			setXY(x, y);
			myView.Application.addTimerTask(this, 400);
		}

		boolean scrollsForward() {
			return myScrollForward;
		}

		void setXY(int x, int y) {
			myX = x;
			myY = y;
		}

		public void run() {
			myView.scrollPage(myScrollForward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			myView.preparePaintInfo();
			expandTo(myX, myY);
			myView.Application.getViewWidget().reset();
			myView.Application.getViewWidget().repaint();
		}

		private void stop() {
			myView.Application.removeTimerTask(this);
		}
	}
}
