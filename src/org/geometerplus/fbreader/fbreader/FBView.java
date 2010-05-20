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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

public final class FBView extends ZLTextView {
	private FBReader myReader;

	FBView(FBReader reader) {
		super(ZLibrary.Instance().getPaintContext());
		myReader = reader;
	}

	public void setModel(ZLTextModel model) {
		myIsManualScrollingActive = false;
		super.setModel(model);
	}

	final void doShortScroll(boolean forward) {
		if (!moveHyperlinkPointer(forward)) {
			scrollPage(forward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
		}

		ZLApplication.Instance().repaintView();
	}

	public void onScrollingFinished(int viewPage) {
		super.onScrollingFinished(viewPage);
	}

	final void doScrollPage(boolean forward) {
		final ScrollingPreferences preferences = ScrollingPreferences.Instance();
		if (preferences.AnimateOption.getValue()) {
			if (forward) {
				ZLTextWordCursor cursor = getEndCursor();
				if (cursor != null && !cursor.isNull() && !cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast()) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_RIGHT : PAGE_BOTTOM);
				}
			} else {
				ZLTextWordCursor cursor = getStartCursor();
				if (cursor != null && !cursor.isNull() && !cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst()) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_LEFT : PAGE_TOP);
				}
			}
		} else {
			scrollPage(forward, ZLTextView.ScrollingMode.NO_OVERLAPPING, 0);
			ZLApplication.Instance().repaintView();
		}
	}

	void followHyperlink(ZLTextHyperlink hyperlink) {
		switch (hyperlink.Type) {
			case FBHyperlinkType.EXTERNAL:
				ZLibrary.Instance().openInBrowser(hyperlink.Id);
				break;
			case FBHyperlinkType.INTERNAL:
				((FBReader)ZLApplication.Instance()).tryOpenFootnote(hyperlink.Id);
				break;
		}
	}

	private int myStartX;
	private int myStartY;
	private boolean myIsManualScrollingActive;

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		if (isScrollingActive()) {
			return false;
		}

		final ZLTextHyperlink hyperlink = findHyperlink(x, y, 10);
		if (hyperlink != null) {
			selectHyperlink(hyperlink);
			ZLApplication.Instance().repaintView();
			followHyperlink(hyperlink);
			return true;
		}

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
					if (cursor == null || cursor.isNull()) {
						return false;
					}
					if (!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst()) {
						ZLApplication.Instance().scrollViewTo(horizontal ? PAGE_LEFT : PAGE_TOP, diff);
					}
				} else if (diff < 0) {
					ZLTextWordCursor cursor = getEndCursor();
					if (cursor == null || cursor.isNull()) {
						return false;
					}
					if (!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast()) {
						ZLApplication.Instance().scrollViewTo(horizontal ? PAGE_RIGHT : PAGE_BOTTOM, -diff);
					}
				} else {
					ZLApplication.Instance().scrollViewTo(PAGE_CENTRAL, 0);
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
					if (cursor != null && !cursor.isNull()) {
						doScroll = !cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst();
					}
				} else if (diff < 0) {
					ZLTextWordCursor cursor = getEndCursor();
					if (cursor != null && !cursor.isNull()) {
						doScroll = !cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast();
					}
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
						ZLApplication.Instance().scrollViewTo(PAGE_CENTRAL, 0);
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

	@Override
	public int getLeftMargin() {
		return myReader.LeftMarginOption.getValue();
	}

	@Override
	public int getRightMargin() {
		return myReader.RightMarginOption.getValue();
	}

	@Override
	public int getTopMargin() {
		return myReader.TopMarginOption.getValue();
	}

	@Override
	public int getBottomMargin() {
		return myReader.BottomMarginOption.getValue();
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myReader.getColorProfile().BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedBackgroundColor() {
		return myReader.getColorProfile().SelectionBackgroundOption.getValue();
	}

	@Override
	public ZLColor getTextColor(byte hyperlinkType) {
		final ColorProfile profile = myReader.getColorProfile();
		switch (hyperlinkType) {
			default:
			case FBHyperlinkType.NONE:
				return profile.RegularTextOption.getValue();
			case FBHyperlinkType.INTERNAL:
			case FBHyperlinkType.EXTERNAL:
				return profile.HyperlinkTextOption.getValue();
		}
	}

	@Override
	public ZLColor getHighlightingColor() {
		return myReader.getColorProfile().HighlightingOption.getValue();
	}

	protected boolean isSelectionEnabled() {
		return myReader.SelectionEnabledOption.getValue();
	}
	
	void scrollToHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphIndex() == 0) {
			return;
		}
		gotoPosition(0, 0, 0);
		preparePaintInfo();
		ZLApplication.Instance().repaintView();
	}

	@Override
	public int scrollbarType() {
		return myReader.ScrollbarTypeOption.getValue();
	}
}
