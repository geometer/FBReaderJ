/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.application.ZLApplication;

final class ZLTextSelectionModel {
	final static class BoundElement extends ZLTextPosition {
		boolean Exists;
		int ParagraphIndex;
		int ElementIndex;
		int CharIndex;

		void copyFrom(BoundElement original) {
			Exists = original.Exists;
			ParagraphIndex = original.ParagraphIndex;
			ElementIndex = original.ElementIndex;
			CharIndex = original.CharIndex;
		}

		public boolean equalsTo(BoundElement be) {
			return
				(Exists == be.Exists) &&
				(ParagraphIndex == be.ParagraphIndex) &&
				(ElementIndex == be.ElementIndex) &&
				(CharIndex == be.CharIndex);
		}

		public final int getParagraphIndex() {
			return ParagraphIndex;
		}

		public final int getElementIndex() {
			return ElementIndex;
		}

		public final int getCharIndex() {
			return CharIndex;
		}
	};

	private final static class Bound {
		final BoundElement Before = new BoundElement();
		final BoundElement After = new BoundElement();

		void copyFrom(Bound original) {
			Before.copyFrom(original.Before);
			After.copyFrom(original.After);
		}

		boolean isLessThan(Bound bound) {
			if (!Before.Exists) {
				return true;
			}	
			if (!bound.Before.Exists) {
				return false;
			}	
			if (!After.Exists) {
				return false;
			}	
			if (!bound.After.Exists) {
				return true;
			}	

			return Before.compareTo(bound.Before) < 0;
		}
	};

	final static class Range {
		final BoundElement Left = new BoundElement();
		final BoundElement Right = new BoundElement();

		Range(BoundElement left, BoundElement right) {
			Left.copyFrom(left);
			Right.copyFrom(right);
		}
	};

	private final ZLTextView myView;

	private boolean myIsActive;
	private boolean myIsEmpty = true;
	private boolean myDoUpdate;
	//private boolean myTextIsUpToDate = true;

	private int myStoredX;
	private int myStoredY;

	private final Bound myFirstBound = new Bound();
	private final Bound mySecondBound = new Bound();

	private final StringBuilder myText = new StringBuilder();

	ZLTextSelectionModel(ZLTextView view) {
		myView = view;
	}

	void activate(int x, int y) {
		if (myView.myCurrentPage.TextElementMap.isEmpty()) {
			return;
		}

		myIsActive = true;
		myIsEmpty = false;
		setBound(myFirstBound, x, y);
		mySecondBound.copyFrom(myFirstBound);
		myText.delete(0, myText.length());
		//myTextIsUpToDate = true;
	}

	boolean extendTo(int x, int y) {
		if (!myIsActive || myView.myCurrentPage.TextElementMap.isEmpty()) {
			return false;
		}

		final Range oldRange = getRange();
		setBound(mySecondBound, x, y);
		final Range newRange = getRange();
		myStoredX = x;
		myStoredY = y;

		if (!mySecondBound.Before.Exists) {
			startSelectionScrolling(false);
		} else if (!mySecondBound.After.Exists) {
			startSelectionScrolling(true);
		} else {
			stopSelectionScrolling();
		}

		if (!oldRange.Left.equalsTo(newRange.Left) || !oldRange.Right.equalsTo(newRange.Right)) {
			//myTextIsUpToDate = false;
			myText.delete(0, myText.length());
			return true;
		}
		return false;
	}

	void update() {
		if (!myView.isSelectionEnabled()) {
			clear();
		} else if (myDoUpdate) {
			myDoUpdate = false;
			setBound(mySecondBound, myStoredX, myStoredY);
			//myView.copySelectedTextToClipboard(ZLDialogManager::CLIPBOARD_SELECTION);
			//myTextIsUpToDate = false;
			myText.delete(0, myText.length());
		}
	}

	void deactivate() {
		stopSelectionScrolling();
		myIsActive = false;
		myDoUpdate = false;
	}

	void clear() {
		stopSelectionScrolling();
		myIsEmpty = true;
		myIsActive = false;
		myDoUpdate = false;
		myText.delete(0, myText.length());
		//myTextIsUpToDate = true;
	}

	Range getRange() {
		return mySecondBound.isLessThan(myFirstBound) ?
			new Range(mySecondBound.After, myFirstBound.Before) :
			new Range(myFirstBound.After, mySecondBound.Before);
	}

	String getText() {
		// TODO: implement
		return myText.toString();
	}

	boolean isEmpty() {
		if (myIsEmpty) {
			return true;
		}
		Range range = getRange();
		return !range.Left.Exists || !range.Right.Exists || (range.Left.equalsTo(range.Right));
	}

	private void setBound(Bound bound, int x, int y) {
		final ZLTextElementAreaVector areaVector = myView.myCurrentPage.TextElementMap;
		if (areaVector.isEmpty()) {
			return;
		}

		final int areaVectorSize = areaVector.size();
		// TODO: replace by binary search inside ZLTextElementAreaVector
		int areaIndex = 0;
		ZLTextElementArea area = null;
		for (; areaIndex < areaVectorSize; ++areaIndex) {
			area = areaVector.get(areaIndex);
			if ((area.YStart > y) || ((area.YEnd > y) && (area.XEnd > x))) {
				break;
			}
		}

		final ZLTextElementArea elementArea = area;
		if (areaIndex < areaVectorSize) {
			bound.After.ParagraphIndex = elementArea.ParagraphIndex;
			bound.After.ElementIndex = elementArea.ElementIndex;
			bound.After.Exists = true;
			bound.After.CharIndex = elementArea.CharIndex;
			if (elementArea.contains(x, y)) {
				bound.Before.ParagraphIndex = bound.After.ParagraphIndex;
				bound.Before.ElementIndex = bound.After.ElementIndex;
				bound.Before.Exists = true;
				if (elementArea.Element instanceof ZLTextWord) {
					myView.setTextStyle(elementArea.Style);
					final ZLTextWord word = (ZLTextWord)elementArea.Element;
					final int deltaX = x - elementArea.XStart;
					final int start = elementArea.CharIndex;
					final int len = elementArea.Length;
					int diff = deltaX;
					int previousDiff = diff;
					int index;
					for (index = 0; (index < len) && (diff > 0); ++index) {
						previousDiff = diff;
						diff = deltaX - myView.getWordWidth(word, start, index + 1);
					}
					if (previousDiff + diff < 0) {
						--index;
					}
					bound.After.CharIndex = start + index;
					bound.Before.CharIndex = bound.After.CharIndex;
				}
			} else if (areaIndex == 0) {
				bound.Before.Exists = false;
			} else {
				final ZLTextElementArea previous = areaVector.get(areaIndex - 1);
				bound.Before.ParagraphIndex = previous.ParagraphIndex;
				bound.Before.ElementIndex = previous.ElementIndex;
				bound.Before.CharIndex = previous.CharIndex + previous.Length;
				bound.Before.Exists = true;
			}
		} else {
			bound.Before.ParagraphIndex = elementArea.ParagraphIndex;
			bound.Before.ElementIndex = elementArea.ElementIndex;
			bound.Before.CharIndex = elementArea.CharIndex + elementArea.Length;
			bound.Before.Exists = true;
			bound.After.Exists = false;
		}
	}

	private final Timer myTimer = new Timer();
	private TimerTask myScrollingTask;

	private void startSelectionScrolling(final boolean forward) {
		stopSelectionScrolling();
		myScrollingTask = new TimerTask() {
			public void run() {
				myView.scrollPage(forward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
				myDoUpdate = true;
				ZLApplication.Instance().repaintView();
			}
		};
		myTimer.schedule(myScrollingTask, 200, 400);
	}

	private void stopSelectionScrolling() {
		if (myScrollingTask != null) {
			myScrollingTask.cancel();
		}
		myScrollingTask = null;
	}
}
