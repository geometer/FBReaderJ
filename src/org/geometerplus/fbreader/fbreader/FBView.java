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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;

public final class FBView extends ZLTextView {
	private FBReaderApp myReader;

	FBView(FBReaderApp reader) {
		myReader = reader;
	}

	public void setModel(ZLTextModel model) {
		myIsManualScrollingActive = false;
		super.setModel(model);
	}

	public void onScrollingFinished(int viewPage) {
		super.onScrollingFinished(viewPage);
	}

	final void doScrollPage(boolean forward) {
		final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
		if (getAnimationType() != Animation.none) {
			if (forward) {
				ZLTextWordCursor cursor = getEndCursor();
				if (cursor != null &&
					!cursor.isNull() &&
					(!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast())) {
					startAutoScrolling(horizontal ? PAGE_RIGHT : PAGE_BOTTOM);
				}
			} else {
				ZLTextWordCursor cursor = getStartCursor();
				if (cursor != null &&
					!cursor.isNull() &&
					(!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst())) {
					startAutoScrolling(horizontal ? PAGE_LEFT : PAGE_TOP);
				}
			}
		} else {
			scrollPage(forward, ZLTextView.ScrollingMode.NO_OVERLAPPING, 0);
			myReader.repaintView();
		}
	}

	private int myStartX;
	private int myStartY;
	private boolean myIsManualScrollingActive;
	private boolean myIsBrightnessAdjustmentInProgress;
	private int myStartBrightness;

	public boolean onFingerSingleTap(int x, int y) {
		if (super.onFingerSingleTap(x, y)) {
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

		final ZLTextElementRegion region = findRegion(x, y, 10, ZLTextHyperlinkRegion.Filter);
		if (region != null) {
			selectRegion(region);
			myReader.repaintView();
			myReader.doAction(ActionCode.PROCESS_HYPERLINK);
			return true;
		}

		final ScrollingPreferences preferences = ScrollingPreferences.Instance();
		final ScrollingPreferences.FingerScrolling fingerScrolling =
			preferences.FingerScrollingOption.getValue();
		if (fingerScrolling == ScrollingPreferences.FingerScrolling.byTap ||
			fingerScrolling == ScrollingPreferences.FingerScrolling.byTapAndFlick) {
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

		return true;
	}

	@Override
	public boolean isDoubleTapSupported() {
		return myReader.EnableDoubleTapOption.getValue();
	}

	@Override
	public boolean onFingerDoubleTap(int x, int y) {
		if (super.onFingerDoubleTap(x, y)) {
			return true;
		}
		if (y <= myContext.getHeight() / 2) {
			myReader.doAction(ActionCode.SHOW_NAVIGATION);
		} else {
			myReader.doAction(ActionCode.SHOW_MENU);
		}
		return true;
	}

	public boolean onFingerPress(int x, int y) {
		if (super.onFingerPress(x, y)) {
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

		if (myReader.AllowScreenBrightnessAdjustmentOption.getValue() && x < myContext.getWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
			myStartBrightness = ZLibrary.Instance().getScreenBrightness();
			return true;
		}

		if (getAnimationType() != Animation.none) {
			final ScrollingPreferences.FingerScrolling fingerScrolling =
				ScrollingPreferences.Instance().FingerScrollingOption.getValue();
			if (fingerScrolling == ScrollingPreferences.FingerScrolling.byFlick ||
				fingerScrolling == ScrollingPreferences.FingerScrolling.byTapAndFlick) {
				myStartX = x;
				myStartY = y;
				setScrollingActive(true);
				myIsManualScrollingActive = true;
			}
		}

		return true;
	}

	public boolean onFingerMove(int x, int y) {
		if (super.onFingerMove(x, y)) {
			return true;
		}

		synchronized (this) {
			if (myIsBrightnessAdjustmentInProgress) {
				if (x >= myContext.getWidth() / 5) {
					myIsBrightnessAdjustmentInProgress = false;
				} else {
					final int delta = (myStartBrightness + 30) * (myStartY - y) / myContext.getHeight();
					ZLibrary.Instance().setScreenBrightness(myStartBrightness + delta);
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

	public boolean onFingerRelease(int x, int y) {
		if (super.onFingerRelease(x, y)) {
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
					if (getAnimationType() != Animation.none) {
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

	public boolean onFingerLongPress(int x, int y) {
		if (super.onFingerLongPress(x, y)) {
			return true;
		}

		if (myReader.DictionaryTappingActionOption.getValue() !=
			FBReaderApp.DictionaryTappingAction.doNothing) {
			final ZLTextElementRegion region = findRegion(x, y, 10, ZLTextElementRegion.Filter);
			if (region != null) {
				selectRegion(region);
				myReader.repaintView();
				return true;
			}
		}

		return false;
	}

	public boolean onFingerMoveAfterLongPress(int x, int y) {
		if (super.onFingerMoveAfterLongPress(x, y)) {
			return true;
		}

		if (myReader.DictionaryTappingActionOption.getValue() !=
			FBReaderApp.DictionaryTappingAction.doNothing) {
			final ZLTextElementRegion region = findRegion(x, y, 10, ZLTextElementRegion.Filter);
			if (region != null) {
				selectRegion(region);
				myReader.repaintView();
			}
		}
		return true;
	}

	public boolean onFingerReleaseAfterLongPress(int x, int y) {
		if (super.onFingerReleaseAfterLongPress(x, y)) {
			return true;
		}

		if (myReader.DictionaryTappingActionOption.getValue() ==
			FBReaderApp.DictionaryTappingAction.openDictionary) {
			final ZLTextElementRegion region = currentRegion();
			if (region instanceof ZLTextWordRegion) {
				myReader.doAction(ActionCode.PROCESS_HYPERLINK);
			}
			return true;
		}

		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffX == 0 && diffY == 0) {
			return true;
		}

		final int direction = (diffY != 0) ?
			(diffY > 0 ? Direction.DOWN : Direction.UP) :
			(diffX > 0 ? Direction.RIGHT : Direction.LEFT);

		ZLTextElementRegion region = currentRegion();
		final ZLTextElementRegion.Filter filter =
			region instanceof ZLTextWordRegion || myReader.NavigateAllWordsOption.getValue()
				? ZLTextElementRegion.Filter : ZLTextHyperlinkRegion.Filter;
		region = nextRegion(direction, filter);
		if (region != null) {
			selectRegion(region);
		} else {
			if (direction == Direction.DOWN) {
				scrollPage(true, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			} else if (direction == Direction.UP) {
				scrollPage(false, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			}
		}

		myReader.repaintView();

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
	public ZLFile getWallpaperFile() {
		final String filePath = myReader.getColorProfile().WallpaperOption.getValue();
		if ("".equals(filePath)) {
			return null;
		}
		
		final ZLFile file = ZLFile.createFileByPath(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		return file;
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
			final FBReaderApp reader = myReader;
			if (reader == null) {
				return;
			}
			final BookModel model = reader.Model;
			if (model == null) {
				return;
			}

			final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(FBHyperlinkType.NONE);
			final ZLColor fillColor = reader.getColorProfile().FooterFillOption.getValue();

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			context.setFont(
				reader.FooterFontOption.getValue(),
				height <= 10 ? height + 3 : height + 1,
				height > 10, false, false
			);

			final int pagesProgress = computeCurrentPage();
			final int bookLength = computePageNumber();

			final StringBuilder info = new StringBuilder();
			if (reader.FooterShowProgressOption.getValue()) {
				info.append(pagesProgress);
				info.append("/");
				info.append(bookLength);
			}
			if (reader.FooterShowBatteryOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(reader.getBatteryLevel());
				info.append("%");
			}
			if (reader.FooterShowClockOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			final String infoString = info.toString();

			final int infoWidth = context.getStringWidth(infoString);
			final ZLFile wallpaper = getWallpaperFile();
			if (wallpaper != null) {
				context.clear(wallpaper);
			} else {
				context.clear(getBackgroundColor());
			}

			// draw info text
			context.setTextColor(fgColor);
			context.drawString(right - infoWidth, height - delta, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			myGaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * myGaugeWidth * pagesProgress / bookLength);

			context.setFillColor(fillColor);
			context.fillRectangle(left + lineWidth, height - 2 * lineWidth, gaugeInternalRight, 2 * lineWidth);

			if (reader.FooterShowTOCMarksOption.getValue()) {
				TOCTree toc = model.TOCTree;
				if (toc != null) {
					final int fullLength = sizeOfFullText();
					for (TOCTree tocItem : toc) {
						TOCTree.Reference reference = tocItem.getReference();
						if (reference != null) {
							final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
							final int xCoord =
								left + 2 * lineWidth + (int)(1.0 * myGaugeWidth * refCoord / fullLength);
							context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
						}
					}
				}
			}
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

	@Override
	public Animation getAnimationType() {
		return ScrollingPreferences.Instance().AnimationOption.getValue();
	}
}
