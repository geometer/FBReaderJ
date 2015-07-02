/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.SelectionCursor;

class ZLTextSelection extends ZLTextHighlighting {
	static class Point {
		int X;
		int Y;

		Point(int x, int y) {
			X = x;
			Y = y;
		}
	}

	private final ZLTextView myView;

	private ZLTextRegion.Soul myLeftMostRegionSoul;
	private ZLTextRegion.Soul myRightMostRegionSoul;

	private SelectionCursor.Which myCursorInMovement = null;
	private final Point myCursorInMovementPoint = new Point(-1, -1);

	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
	}

	@Override
	public boolean isEmpty() {
		return myLeftMostRegionSoul == null;
	}

	public boolean clear() {
		if (isEmpty()) {
			return false;
		}

		stop();
		myLeftMostRegionSoul = null;
		myRightMostRegionSoul = null;
		myCursorInMovement = null;
		return true;
	}

	void setCursorInMovement(SelectionCursor.Which which, int x, int y) {
		myCursorInMovement = which;
		myCursorInMovementPoint.X = x;
		myCursorInMovementPoint.Y = y;
	}

	SelectionCursor.Which getCursorInMovement() {
		return myCursorInMovement;
	}

	Point getCursorInMovementPoint() {
		return myCursorInMovementPoint;
	}

	boolean start(int x, int y) {
		clear();

		final ZLTextRegion region = myView.findRegion(
			x, y, myView.maxSelectionDistance(), ZLTextRegion.AnyRegionFilter
		);
		if (region == null) {
			return false;
		}

		myRightMostRegionSoul = myLeftMostRegionSoul = region.getSoul();
		return true;
	}

	void stop() {
		myCursorInMovement = null;
		if (myScroller != null) {
			myScroller.stop();
			myScroller = null;
		}
	}

	void expandTo(ZLTextPage page, int x, int y) {
		if (isEmpty()) {
			return;
		}

		final ZLTextElementAreaVector vector = page.TextElementMap;
		final ZLTextElementArea firstArea = vector.getFirstArea();
		final ZLTextElementArea lastArea = vector.getLastArea();
		if (firstArea != null && y < firstArea.YStart) {
			if (myScroller != null && myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(page, false, x, y);
				return;
			}
		//} else if (lastArea != null && y + ZLTextSelectionCursor.getHeight() / 2 + ZLTextSelectionCursor.getAccent() / 2 > lastArea.YEnd) {
		} else if (lastArea != null && y > lastArea.YEnd) {
			if (myScroller != null && !myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(page, true, x, y);
				return;
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

		ZLTextRegion region = myView.findRegion(x, y, myView.maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
		if (region == null) {
			final ZLTextElementAreaVector.RegionPair pair =
				myView.findRegionsPair(x, y, ZLTextRegion.AnyRegionFilter);
			if (pair.Before != null || pair.After != null) {
				final ZLTextRegion.Soul base =
					myCursorInMovement == SelectionCursor.Which.Right
						? myLeftMostRegionSoul : myRightMostRegionSoul;
				if (pair.Before != null) {
					if (base.compareTo(pair.Before.getSoul()) <= 0) {
						region = pair.Before;
					} else {
						region = pair.After;
					}
				} else {
					if (base.compareTo(pair.After.getSoul()) >= 0) {
						region = pair.After;
					} else {
						region = pair.Before;
					}
				}
			}
		}
		if (region == null) {
			return;
		}

		final ZLTextRegion.Soul soul = region.getSoul();
		if (myCursorInMovement == SelectionCursor.Which.Right) {
			if (myLeftMostRegionSoul.compareTo(soul) <= 0) {
				myRightMostRegionSoul = soul;
			} else {
				myRightMostRegionSoul = myLeftMostRegionSoul;
				myLeftMostRegionSoul = soul;
				myCursorInMovement = SelectionCursor.Which.Left;
			}
		} else {
			if (myRightMostRegionSoul.compareTo(soul) >= 0) {
				myLeftMostRegionSoul = soul;
			} else {
				myLeftMostRegionSoul = myRightMostRegionSoul;
				myRightMostRegionSoul = soul;
				myCursorInMovement = SelectionCursor.Which.Right;
			}
		}

		if (myCursorInMovement == SelectionCursor.Which.Right) {
			if (hasPartAfterPage(page)) {
				myView.turnPage(true, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
				myView.Application.getViewWidget().reset();
				myView.preparePaintInfo();
			}
		} else {
			if (hasPartBeforePage(page)) {
				myView.turnPage(false, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
				myView.Application.getViewWidget().reset();
				myView.preparePaintInfo();
			}
		}
	}

	@Override
	public ZLTextPosition getStartPosition() {
		if (isEmpty()) {
			return null;
		}
		return new ZLTextFixedPosition(
			myLeftMostRegionSoul.ParagraphIndex,
			myLeftMostRegionSoul.StartElementIndex,
			0
		);
	}

	@Override
	public ZLTextPosition getEndPosition() {
		if (isEmpty()) {
			return null;
		}
		final ZLTextParagraphCursor cursor = myView.cursor(myRightMostRegionSoul.ParagraphIndex);
		final ZLTextElement element = cursor.getElement(myRightMostRegionSoul.EndElementIndex);
		return new ZLTextFixedPosition(
			myRightMostRegionSoul.ParagraphIndex,
			myRightMostRegionSoul.EndElementIndex,
			element instanceof ZLTextWord ? ((ZLTextWord)element).Length : 0
		);
	}

	@Override
	public ZLTextElementArea getStartArea(ZLTextPage page) {
		if (isEmpty()) {
			return null;
		}
		final ZLTextElementAreaVector vector = page.TextElementMap;
		final ZLTextRegion region = vector.getRegion(myLeftMostRegionSoul);
		if (region != null) {
			return region.getFirstArea();
		}
		final ZLTextElementArea firstArea = vector.getFirstArea();
		if (firstArea != null && myLeftMostRegionSoul.compareTo(firstArea) <= 0) {
			return firstArea;
		}
		return null;
	}

	@Override
	public ZLTextElementArea getEndArea(ZLTextPage page) {
		if (isEmpty()) {
			return null;
		}
		final ZLTextElementAreaVector vector = page.TextElementMap;
		final ZLTextRegion region = vector.getRegion(myRightMostRegionSoul);
		if (region != null) {
			return region.getLastArea();
		}
		final ZLTextElementArea lastArea = vector.getLastArea();
		if (lastArea != null && myRightMostRegionSoul.compareTo(lastArea) >= 0) {
			return lastArea;
		}
		return null;
	}

	boolean hasPartBeforePage(ZLTextPage page) {
		if (isEmpty()) {
			return false;
		}
		final ZLTextElementArea firstPageArea = page.TextElementMap.getFirstArea();
		if (firstPageArea == null) {
			return false;
		}
		final int cmp = myLeftMostRegionSoul.compareTo(firstPageArea);
		return cmp < 0 || (cmp == 0 && !firstPageArea.isFirstInElement());
	}

	boolean hasPartAfterPage(ZLTextPage page) {
		if (isEmpty()) {
			return false;
		}
		final ZLTextElementArea lastPageArea = page.TextElementMap.getLastArea();
		if (lastPageArea == null) {
			return false;
		}
		final int cmp = myRightMostRegionSoul.compareTo(lastPageArea);
		return cmp > 0 || (cmp == 0 && !lastPageArea.isLastInElement());
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myView.getSelectionBackgroundColor();
	}

	@Override
	public ZLColor getForegroundColor() {
		return myView.getSelectionForegroundColor();
	}

	@Override
	public ZLColor getOutlineColor() {
		return null;
	}

	private class Scroller implements Runnable {
		private final ZLTextPage myPage;
		private final boolean myScrollForward;
		private int myX, myY;

		Scroller(ZLTextPage page, boolean forward, int x, int y) {
			myPage = page;
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
			myView.turnPage(myScrollForward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			myView.preparePaintInfo();
			expandTo(myPage, myX, myY);
			myView.Application.getViewWidget().reset();
			myView.Application.getViewWidget().repaint();
		}

		private void stop() {
			myView.Application.removeTimerTask(this);
		}
	}
}
