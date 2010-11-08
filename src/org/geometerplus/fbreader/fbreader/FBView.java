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
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

public final class FBView extends ZLTextView {
	private FBReaderApp myReader;

	FBView(FBReaderApp reader) {
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

		myReader.repaintView();
	}

	public void onScrollingFinished(int viewPage) {
		super.onScrollingFinished(viewPage);
	}

	final void doScrollPage(boolean forward) {
		final ScrollingPreferences preferences = ScrollingPreferences.Instance();
		if (preferences.AnimateOption.getValue()) {
			if (forward) {
				ZLTextWordCursor cursor = getEndCursor();
				if (cursor != null &&
					!cursor.isNull() &&
					(!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast())) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_RIGHT : PAGE_BOTTOM);
				}
			} else {
				ZLTextWordCursor cursor = getStartCursor();
				if (cursor != null &&
					!cursor.isNull() &&
					(!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst())) {
					startAutoScrolling(preferences.HorizontalOption.getValue() ? PAGE_LEFT : PAGE_TOP);
				}
			}
		} else {
			scrollPage(forward, ZLTextView.ScrollingMode.NO_OVERLAPPING, 0);
			myReader.repaintView();
		}
	}

	void followHyperlink(ZLTextHyperlink hyperlink) {
		switch (hyperlink.Type) {
			case FBHyperlinkType.EXTERNAL:
				ZLibrary.Instance().openInBrowser(hyperlink.Id);
				break;
			case FBHyperlinkType.INTERNAL:
				myReader.tryOpenFootnote(hyperlink.Id);
				break;
		}
	}

	private int myStartX;
	private int myStartY;
	private boolean myIsManualScrollingActive;
	private boolean myIsBrightnessAdjustmentInProgress;
	private int myStartBrightness;

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		if (isScrollingActive()) {
			return false;
		}

		if (myReader.FooterIsSensitiveOption.getValue()) {
			Footer footer = getFooterArea();
			if (footer != null && y > myContext.getHeight() - footer.getTapHeight()) {
				footer.setProgress(x);
				return true;
			}
		}

		final ZLTextHyperlink hyperlink = findHyperlink(x, y, 10);
		if (hyperlink != null) {
			selectHyperlink(hyperlink);
			myReader.repaintView();
			followHyperlink(hyperlink);
			return true;
		}

		if (myReader.AllowScreenBrightnessAdjustmentOption.getValue() && x < myContext.getWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
			myStartBrightness = ZLibrary.Instance().getScreenBrightness();
			System.err.println("starting on level: " + myStartBrightness);
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
				if (x <= myContext.getWidth() / 3) {
					doScrollPage(false);
				} else if (x >= myContext.getWidth() * 2 / 3) {
					doScrollPage(true);
				}
			} else {
				if (y <= myContext.getHeight() / 3) {
					doScrollPage(false);
				} else if (y >= myContext.getHeight() * 2 / 3) {
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
			if (myIsBrightnessAdjustmentInProgress) {
				if (x >= myContext.getWidth() / 5) {
					myIsBrightnessAdjustmentInProgress = false;
				} else {
					final int delta = (myStartBrightness + 30) * (myStartY - y) / myContext.getHeight();
					System.err.println("adjusting to level: " + (myStartBrightness + delta));
					ZLibrary.Instance().setScreenBrightness(myStartBrightness + delta);
					System.err.println("adjusted to level: " + ZLibrary.Instance().getScreenBrightness());
					return true;
				}
			}

			if (isScrollingActive() && myIsManualScrollingActive) {
				final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
				final int diff = horizontal ? x - myStartX : y - myStartY;
				if (diff > 0) {
					ZLTextWordCursor cursor = getStartCursor();
					if (cursor == null || cursor.isNull()) {
						return false;
					}
					if (!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst()) {
						myReader.scrollViewTo(horizontal ? PAGE_LEFT : PAGE_TOP, diff);
					}
				} else if (diff < 0) {
					ZLTextWordCursor cursor = getEndCursor();
					if (cursor == null || cursor.isNull()) {
						return false;
					}
					if (!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast()) {
						myReader.scrollViewTo(horizontal ? PAGE_RIGHT : PAGE_BOTTOM, -diff);
					}
				} else {
					myReader.scrollViewTo(PAGE_CENTRAL, 0);
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
			myIsBrightnessAdjustmentInProgress = false;
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
					final int h = myContext.getHeight();
					final int w = myContext.getWidth();
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
						myReader.scrollViewTo(PAGE_CENTRAL, 0);
						onScrollingFinished(viewPage);
						myReader.repaintView();
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
			myReader.doAction(ActionCode.TRACKBALL_SCROLL_FORWARD);
		} else if (diffY < 0) {
			myReader.doAction(ActionCode.TRACKBALL_SCROLL_BACKWARD);
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

	private class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				ZLApplication.Instance().repaintView();
			}
		};

		public int getHeight() {
			return myReader.FooterHeightOption.getValue();
		}

		public void paint(ZLPaintContext context) {
			final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(FBHyperlinkType.NONE);

			final int width = context.getWidth();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			context.setFont(
				myReader.FooterFontOption.getValue(),
				height <= 10 ? height + 3 : height + 1,
				height > 10, false, false
			);

			final int pagesProgress = computeCurrentPage();
			final int bookLength = computePageNumber();

			final StringBuilder info = new StringBuilder();
			if (myReader.FooterShowProgressOption.getValue()) {
				info.append(pagesProgress);
				info.append("/");
				info.append(bookLength);
			}
			if (myReader.FooterShowBatteryOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(myReader.getBatteryLevel());
				info.append("%");
			}
			if (myReader.FooterShowClockOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			final String infoString = info.toString();

			final int infoWidth = context.getStringWidth(infoString);
			context.clear(bgColor);

			// draw info text
			context.setTextColor(fgColor);
			context.drawString(width - infoWidth, height - delta, infoString);

			// draw gauge
			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			final int gaugeRight = width - ((infoWidth == 0) ? 0 : infoWidth + 10) - 2;
			myGaugeWidth = gaugeRight;
			context.drawLine(lineWidth, lineWidth, lineWidth, height - lineWidth);
			context.drawLine(lineWidth, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, lineWidth, lineWidth);

			final int gaugeInternalRight = 1 + 2 * lineWidth + (int)(1.0 * (gaugeRight - 2 - 3 * lineWidth) * pagesProgress / bookLength);
			context.drawLine(1 + 2 * lineWidth, 1 + 2 * lineWidth, 1 + 2 * lineWidth, height - 1 - 2 * lineWidth);
			context.drawLine(1 + 2 * lineWidth, height - 1 - 2 * lineWidth, gaugeInternalRight, height - 1 - 2 * lineWidth);
			context.drawLine(gaugeInternalRight, height - 1 - 2 * lineWidth, gaugeInternalRight, 1 + 2 * lineWidth);
			context.drawLine(gaugeInternalRight, 1 + 2 * lineWidth, 1 + 2 * lineWidth, 1 + 2 * lineWidth);
		}

		// TODO: remove
		int myGaugeWidth = 1;
		public int getGaugeWidth() {
			return myGaugeWidth;
		}

		public int getTapHeight() {
			return 30;
		}

		public void setProgress(int x) {
			// set progress according to tap coordinate
			int gaugeWidth = getGaugeWidth();
			float progress = 1.0f * Math.min(x, gaugeWidth) / gaugeWidth;
			int page = (int)(progress * computePageNumber());
			if (page <= 1) {
				gotoHome();
			} else {
				gotoPage(page);
			}
			myReader.repaintView();
		}
	}

	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		if (myReader.ScrollbarTypeOption.getValue() == SCROLLBAR_SHOW_AS_FOOTER) {
			if (myFooter == null) {
				myFooter = new Footer();
				ZLApplication.Instance().addTimerTask(myFooter.UpdateTask, 15000);
			}
		} else {
			if (myFooter != null) {
				ZLApplication.Instance().removeTimerTask(myFooter.UpdateTask);
				myFooter = null;
			}
		}
		return myFooter;
	}

	@Override
	protected boolean isSelectionEnabled() {
		return myReader.SelectionEnabledOption.getValue();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;

	@Override
	public int scrollbarType() {
		return myReader.ScrollbarTypeOption.getValue();
	}
}
