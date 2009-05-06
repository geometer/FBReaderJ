/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.impl.*;

public abstract class FBView extends ZLTextViewImpl {
	private static ZLIntegerRangeOption ourLeftMarginOption;
	private static ZLIntegerRangeOption ourRightMarginOption;
	private static ZLIntegerRangeOption ourTopMarginOption;
	private static ZLIntegerRangeOption ourBottomMarginOption;
	
	private static ZLBooleanOption ourSelectionOption;

	private static ZLIntegerRangeOption createMarginOption(String name, int defaultValue) {
		return new ZLIntegerRangeOption(
			"Options", name, 0, 1000, defaultValue
		);
	}

	FBView(ZLPaintContext context) {
		super(context);
	}

	final void doScrollPage(boolean forward) {
		final ScrollingPreferences preferences = ScrollingPreferences.Instance();
		if (preferences.AnimateOption.getValue()) {
			if (forward) {
				ZLTextWordCursor cursor = getEndCursor();
				if (!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast()) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_RIGHT : PAGE_BOTTOM);
				}
			} else {
				ZLTextWordCursor cursor = getStartCursor();
				if (!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst()) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_LEFT : PAGE_TOP);
				}
			}
		} else {
			scrollPage(forward, ZLTextView.ScrollingMode.NO_OVERLAPPING, 0);
			ZLApplication.Instance().repaintView();
		}
	}

	private int myStartX;
	private int myStartY;
	private boolean myIsManualScrollingActive;

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		if (!isScrollingActive()) {
			final ScrollingPreferences preferences = ScrollingPreferences.Instance();
			if (preferences.FlickOption.getValue()) {
				myStartX = x;
				myStartY = y;
				setScrollingActive(true);
				myIsManualScrollingActive = true;
			} else {
				if (preferences.HorizontalOption.getValue()) {
					if (x <= Context.getWidth() / 3) {
						doScrollPage(false);
					} else if (x >= Context.getWidth() * 2 / 3) {
						doScrollPage(true);
					}
				} else {
					if (y <= Context.getHeight() / 3) {
						doScrollPage(false);
					} else if (y >= Context.getHeight() * 2 / 3) {
						doScrollPage(true);
					}
				}
			}
		}

		//activateSelection(x, y);
		return true;
	}

	public boolean onStylusMovePressed(int x, int y) {
		if (super.onStylusMovePressed(x, y)) {
			return true;
		}

		synchronized (this) {
			if (isScrollingActive() && myIsManualScrollingActive) {
				final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
				final int diff = horizontal ? x - myStartX : y - myStartY;
				if (diff > 0) {
					ZLTextWordCursor cursor = getStartCursor();
					if (!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst()) {
						scrollTo(horizontal ? PAGE_LEFT : PAGE_TOP, diff);
					}
				} else if (diff < 0) {
					ZLTextWordCursor cursor = getEndCursor();
					if (!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast()) {
						scrollTo(horizontal ? PAGE_RIGHT : PAGE_BOTTOM, -diff);
					}
				} else {
					scrollTo(PAGE_CENTRAL, 0);
				}
				return true;
			}
		}

		return false;
	}

	public boolean onStylusRelease(int x, int y) {
		if (super.onStylusRelease(x, y)) {
			return true;
		}

		synchronized (this) {
			if (isScrollingActive() && myIsManualScrollingActive) {
				setScrollingActive(false);
				myIsManualScrollingActive = false;
				final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
				final int diff = horizontal ? x - myStartX : y - myStartY;
				boolean doScroll = false;
				if (diff > 0) {
					ZLTextWordCursor cursor = getStartCursor();
					doScroll = !cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst();
				} else if (diff < 0) {
					ZLTextWordCursor cursor = getEndCursor();
					doScroll = !cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast();
				}
				if (doScroll) {
					final int h = Context.getHeight();
					final int w = Context.getWidth();
					final int minDiff = horizontal ?
						((w > h) ? w / 4 : w / 3) :
						((h > w) ? h / 4 : h / 3);
					int viewPage = PAGE_CENTRAL;
					if (Math.abs(diff) > minDiff) {
						viewPage = horizontal ?
							((diff < 0) ? PAGE_RIGHT : PAGE_LEFT) :
							((diff < 0) ? PAGE_BOTTOM : PAGE_TOP);
					}
					if (ScrollingPreferences.Instance().AnimateOption.getValue()) {
						startAutoScrolling(viewPage);
					} else {
						scrollTo(PAGE_CENTRAL, 0);
						onScrollingFinished(viewPage);
						ZLApplication.Instance().repaintView();
						setScrollingActive(false);
					}
				}
				return true;
			}
		}
		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffY > 0) {
			ZLApplication.Instance().doAction(ActionCode.TRACKBALL_SCROLL_FORWARD);
		} else if (diffY < 0) {
			ZLApplication.Instance().doAction(ActionCode.TRACKBALL_SCROLL_BACKWARD);
		}
		return true;
	}

	public static final ZLIntegerRangeOption getLeftMarginOption() {
		if (ourLeftMarginOption == null) {
			ourLeftMarginOption = createMarginOption("LeftMargin", 4);
		}
		return ourLeftMarginOption;
	}
	public int getLeftMargin() {
		return getLeftMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getRightMarginOption() {
		if (ourRightMarginOption == null) {
			ourRightMarginOption = createMarginOption("RightMargin", 4);
		}
		return ourRightMarginOption;
	}
	public int getRightMargin() {
		return getRightMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getTopMarginOption() {
		if (ourTopMarginOption == null) {
			ourTopMarginOption = createMarginOption("TopMargin", 0);
		}
		return ourTopMarginOption;
	}
	public int getTopMargin() {
		return getTopMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getBottomMarginOption() {
		if (ourBottomMarginOption == null) {
			ourBottomMarginOption = createMarginOption("BottomMargin", 4);
		}
		return ourBottomMarginOption;
	}
	public int getBottomMargin() {
		return getBottomMarginOption().getValue();
	}

	public static ZLBooleanOption selectionOption() {
		if (ourSelectionOption == null) {
			ourSelectionOption = new ZLBooleanOption("Options", "IsSelectionEnabled", true);
		}
		return ourSelectionOption;
	}

	protected boolean isSelectionEnabled() {
		return selectionOption().getValue();
	}
}
