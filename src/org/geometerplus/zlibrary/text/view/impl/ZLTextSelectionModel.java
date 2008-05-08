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

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.ZLTextView;

final class ZLTextSelectionModel {
	final static class BoundElement {
		boolean Exists;
		int ParagraphIndex;
		int TextElementIndex;
		int CharIndex;

		void copyFrom(BoundElement original) {
			Exists = original.Exists;
			ParagraphIndex = original.ParagraphIndex;
			TextElementIndex = original.TextElementIndex;
			CharIndex = original.CharIndex;
		}

		public boolean equalsTo(BoundElement be) {
			return
				(Exists == be.Exists) &&
				(ParagraphIndex == be.ParagraphIndex) &&
				(TextElementIndex == be.TextElementIndex) &&
				(CharIndex == be.CharIndex);
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

			{
				int diff = Before.ParagraphIndex - bound.Before.ParagraphIndex;
				if (diff != 0) {
					return diff < 0;
				}
			}

			{
				int diff = Before.TextElementIndex - bound.Before.TextElementIndex;
				if (diff != 0) {
					return diff < 0;
				}
			}

			return Before.CharIndex < bound.Before.CharIndex;
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

	private final ZLTextViewImpl myView;
	private final ZLApplication myApplication;

	private boolean myIsActive;
	private boolean myIsEmpty = true;
	private boolean myDoUpdate;
	private boolean myTextIsUpToDate = true;

	private int myStoredX;
	private int myStoredY;

	private final Bound myFirstBound = new Bound();
	private final Bound mySecondBound = new Bound();

	private final HashSet myCursors = new HashSet();
	private final StringBuilder myText = new StringBuilder();

	ZLTextSelectionModel(ZLTextViewImpl view, ZLApplication application) {
		myView = view;
		myApplication = application;
	}

	void activate(int x, int y) {
		if (myView.myTextElementMap.isEmpty()) {
			return;
		}

		myIsActive = true;
		myIsEmpty = false;
		setBound(myFirstBound, x, y);
		mySecondBound.copyFrom(myFirstBound);
		myCursors.clear();
		myText.delete(0, myText.length());
		myTextIsUpToDate = true;
	}

	boolean extendTo(int x, int y) {
		if (!myIsActive || myView.myTextElementMap.isEmpty()) {
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
			myTextIsUpToDate = false;
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
			myTextIsUpToDate = false;
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
		myCursors.clear();
		myText.delete(0, myText.length());
		myTextIsUpToDate = true;
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
		final ZLTextRectangularAreaVector areaVector = myView.myTextElementMap;
		if (areaVector.isEmpty()) {
			return;
		}

		final int areaVectorSize = areaVector.size();
		// TODO: replace by binary search inside ZLTextElementAreaVector
		int areaIndex = 0;
		ZLTextRectangularArea area = null;
		for (; areaIndex < areaVectorSize; ++areaIndex) {
			area = areaVector.get(areaIndex);
			if ((area.YStart > y) || ((area.YEnd > y) && (area.XEnd > x))) {
				break;
			}
		}

		final ZLTextElementArea elementArea = (ZLTextElementArea)area;
		if (areaIndex < areaVectorSize) {
			bound.After.ParagraphIndex = elementArea.ParagraphIndex;
			bound.After.TextElementIndex = elementArea.TextElementIndex;
			bound.After.Exists = true;
			bound.After.CharIndex = elementArea.StartCharIndex;
			if (elementArea.contains(x, y)) {
				bound.Before.ParagraphIndex = bound.After.ParagraphIndex;
				bound.Before.TextElementIndex = bound.After.TextElementIndex;
				bound.Before.Exists = true;
				if (elementArea.Element instanceof ZLTextWord) {
					myView.setTextStyle(elementArea.Style);
					final ZLTextWord word = (ZLTextWord)elementArea.Element;
					final int deltaX = x - elementArea.XStart;
					final int start = elementArea.StartCharIndex;
					final int len = elementArea.Length;
					int diff = deltaX;
					int previousDiff = diff;
					int index;
					for (index = 0; (index < len) && (diff > 0); ++index) {
						previousDiff = diff;
						diff = deltaX - ZLTextViewImpl.getWordWidth(myView.Context, word, start, index + 1);
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
				final ZLTextElementArea previous = (ZLTextElementArea)areaVector.get(areaIndex - 1);
				bound.Before.ParagraphIndex = previous.ParagraphIndex;
				bound.Before.TextElementIndex = previous.TextElementIndex;
				bound.Before.CharIndex = previous.StartCharIndex + previous.Length;
				bound.Before.Exists = true;
			}
		} else {
			bound.Before.ParagraphIndex = elementArea.ParagraphIndex;
			bound.Before.TextElementIndex = elementArea.TextElementIndex;
			bound.Before.CharIndex = elementArea.StartCharIndex + elementArea.Length;
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
				myView.Application.refreshWindow();
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
