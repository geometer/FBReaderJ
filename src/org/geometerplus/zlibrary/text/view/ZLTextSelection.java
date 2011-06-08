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

	private ZLTextRegion myLeftMostRegion;
	private ZLTextRegion myRightMostRegion;

	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
	}

	boolean isEmpty() {
		return myLeftMostRegion == null;
	}

	boolean clear() {
		if (isEmpty()) {
			return false;
		}

		stop();
		myLeftMostRegion = null;
		myRightMostRegion = null;
		return true;
	}

	boolean start(int x, int y) {
		clear();

		myLeftMostRegion = myView.findRegion(
			x, y, ZLTextView.MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter
		);
		if (myLeftMostRegion == null) {
			return false;
		}

		myRightMostRegion = myLeftMostRegion;
		return true;
	}

	void stop() {
		if (myScroller != null) {
			myScroller.stop();
			myScroller = null;
		}
	}

	ZLTextSelectionCursor expandTo(int x, int y, ZLTextSelectionCursor cursorToMove) {
		if (isEmpty()) {
			return cursorToMove;
		}

		/*
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
		*/

		ZLTextRegion region = myView.findRegion(x, y, ZLTextView.MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region == null && myScroller != null) {
			region = myView.findRegion(x, y, ZLTextRegion.AnyRegionFilter);
		}
		if (region == null) {
			return cursorToMove;
		}

		if (cursorToMove == ZLTextSelectionCursor.Right) {
			if (myLeftMostRegion.compareTo(region) <= 0) {
				myRightMostRegion = region;
				return cursorToMove;
			} else {
				myRightMostRegion = myLeftMostRegion;
				myLeftMostRegion = region;
				return ZLTextSelectionCursor.Left;
			}
		} else {
			if (myRightMostRegion.compareTo(region) >= 0) {
				myLeftMostRegion = region;
				return cursorToMove;
			} else {
				myLeftMostRegion = myRightMostRegion;
				myRightMostRegion = region;
				return ZLTextSelectionCursor.Right;
			}
		}
	}

	boolean isAreaSelected(ZLTextElementArea area) {
		return
			!isEmpty()
			&& myLeftMostRegion.getFirstArea().weakCompareTo(area) <= 0
			&& myRightMostRegion.getLastArea().weakCompareTo(area) >= 0;
	}

	ZLTextElementArea getStartArea() {
		return myLeftMostRegion.getFirstArea();
	}

	ZLTextElementArea getEndArea() {
		return myRightMostRegion.getLastArea();
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
			//expandTo(myX, myY, myScrollForward);
			myView.Application.getViewWidget().reset();
			myView.Application.getViewWidget().repaint();
		}

		private void stop() {
			myView.Application.removeTimerTask(this);
		}
	}
}
