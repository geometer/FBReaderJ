/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.hyphenation.*;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

public abstract class ZLTextView extends ZLTextViewBase {
	public static final int MAX_SELECTION_DISTANCE = 10;

	public interface ScrollingMode {
		int NO_OVERLAPPING = 0;
		int KEEP_LINES = 1;
		int SCROLL_LINES = 2;
		int SCROLL_PERCENTAGE = 3;
	};

	private ZLTextModel myModel;

	private interface SizeUnit {
		int PIXEL_UNIT = 0;
		int LINE_UNIT = 1;
	};

	private int myScrollingMode;
	private int myOverlappingValue;

	private ZLTextPage myPreviousPage = new ZLTextPage();
	ZLTextPage myCurrentPage = new ZLTextPage();
	private ZLTextPage myNextPage = new ZLTextPage();

	private final HashMap<ZLTextLineInfo,ZLTextLineInfo> myLineInfoCache = new HashMap<ZLTextLineInfo,ZLTextLineInfo>();

	private ZLTextRegion.Soul mySelectedRegionSoul;
	private boolean myHighlightSelectedRegion = true;

	private ZLTextSelection mySelection;
	private ZLTextHighlighting myHighlighting;

	public ZLTextView(ZLApplication application) {
		super(application);
		mySelection = new ZLTextSelection(this);
		myHighlighting = new ZLTextHighlighting();
	}

	public synchronized void setModel(ZLTextModel model) {
		ZLTextParagraphCursorCache.clear();

		myModel = model;
		myCurrentPage.reset();
		myPreviousPage.reset();
		myNextPage.reset();
		if (myModel != null) {
			final int paragraphsNumber = myModel.getParagraphsNumber();
			if (paragraphsNumber > 0) {
				myCurrentPage.moveStartCursor(ZLTextParagraphCursor.cursor(myModel, 0));
			}
		}
		Application.getViewWidget().reset();
	}

	public ZLTextModel getModel() {
		return myModel;
	}

	public ZLTextWordCursor getStartCursor() {
		if (myCurrentPage.StartCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		return myCurrentPage.StartCursor;
	}

	public ZLTextWordCursor getEndCursor() {
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		return myCurrentPage.EndCursor;
	}

	private synchronized void gotoMark(ZLTextMark mark) {
		if (mark == null) {
			return;
		}

		myPreviousPage.reset();
		myNextPage.reset();
		boolean doRepaint = false;
		if (myCurrentPage.StartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.StartCursor.isNull()) {
			return;
		}
		if (myCurrentPage.StartCursor.getParagraphIndex() != mark.ParagraphIndex ||
			myCurrentPage.StartCursor.getMark().compareTo(mark) > 0) {
			doRepaint = true;
			gotoPosition(mark.ParagraphIndex, 0, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		while (mark.compareTo(myCurrentPage.EndCursor.getMark()) > 0) {
			doRepaint = true;
			scrollPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (doRepaint) {
			if (myCurrentPage.StartCursor.isNull()) {
				preparePaintInfo(myCurrentPage);
			}
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public synchronized int search(final String text, boolean ignoreCase, boolean wholeText, boolean backward, boolean thisSectionOnly) {
		if (text.length() == 0) {
			return 0;
		}
		int startIndex = 0;
		int endIndex = myModel.getParagraphsNumber();
		if (thisSectionOnly) {
			// TODO: implement
		}
		int count = myModel.search(text, startIndex, endIndex, ignoreCase);
		myPreviousPage.reset();
		myNextPage.reset();
		if (!myCurrentPage.StartCursor.isNull()) {
			rebuildPaintInfo();
			if (count > 0) {
				ZLTextMark mark = myCurrentPage.StartCursor.getMark();
				gotoMark(wholeText ?
					(backward ? myModel.getLastMark() : myModel.getFirstMark()) :
					(backward ? myModel.getPreviousMark(mark) : myModel.getNextMark(mark)));
			}
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
		return count;
	}

	public boolean canFindNext() {
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		return !end.isNull() && (myModel != null) && (myModel.getNextMark(end.getMark()) != null);
	}

	public synchronized void findNext() {
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		if (!end.isNull()) {
			gotoMark(myModel.getNextMark(end.getMark()));
		}
	}

	public boolean canFindPrevious() {
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		return !start.isNull() && (myModel != null) && (myModel.getPreviousMark(start.getMark()) != null);
	}

	public synchronized void findPrevious() {
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		if (!start.isNull()) {
			gotoMark(myModel.getPreviousMark(start.getMark()));
		}
	}

	public void clearFindResults() {
		if (!findResultsAreEmpty()) {
			myModel.removeAllMarks();
			rebuildPaintInfo();
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public boolean findResultsAreEmpty() {
		return (myModel == null) || myModel.getMarks().isEmpty();
	}

	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex) {
		switch (pageIndex) {
			case current:
				break;
			case previous:
			{
				final ZLTextPage swap = myNextPage;
				myNextPage = myCurrentPage;
				myCurrentPage = myPreviousPage;
				myPreviousPage = swap;
				myPreviousPage.reset();
				if (myCurrentPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
					preparePaintInfo(myNextPage);
					myCurrentPage.EndCursor.setCursor(myNextPage.StartCursor);
					myCurrentPage.PaintState = PaintStateEnum.END_IS_KNOWN;
				} else if (!myCurrentPage.EndCursor.isNull() &&
						   !myNextPage.StartCursor.isNull() &&
						   !myCurrentPage.EndCursor.samePositionAs(myNextPage.StartCursor)) {
					myNextPage.reset();
					myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
					myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
					Application.getViewWidget().reset();
				}
				break;
			}
			case next:
			{
				final ZLTextPage swap = myPreviousPage;
				myPreviousPage = myCurrentPage;
				myCurrentPage = myNextPage;
				myNextPage = swap;
				myNextPage.reset();
				switch (myCurrentPage.PaintState) {
					case PaintStateEnum.NOTHING_TO_PAINT:
						preparePaintInfo(myPreviousPage);
						myCurrentPage.StartCursor.setCursor(myPreviousPage.EndCursor);
						myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
						break;
					case PaintStateEnum.READY:
						myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
						myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
						break;
				}
				break;
			}
		}
	}

	public void highlight(ZLTextPosition start, ZLTextPosition end) {
		myHighlighting.setup(start, end);
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	public void clearHighlighting() {
		if (myHighlighting.clear()) {
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	protected void moveSelectionCursorTo(ZLTextSelectionCursor cursor, int x, int y) {
		y -= ZLTextSelectionCursor.getHeight() / 2 + ZLTextSelectionCursor.getAccent() / 2;
		mySelection.setCursorInMovement(cursor, x, y);
		mySelection.expandTo(x, y);
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	protected void releaseSelectionCursor() {
		mySelection.stop();
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
	}

	protected ZLTextSelectionCursor getSelectionCursorInMovement() {
		return mySelection.getCursorInMovement();
	}

	private ZLTextSelection.Point getSelectionCursorPoint(ZLTextPage page, ZLTextSelectionCursor cursor) {
		if (cursor == ZLTextSelectionCursor.None) {
			return null;
		}

		if (cursor == mySelection.getCursorInMovement()) {
			return mySelection.getCursorInMovementPoint();
		}

		if (cursor == ZLTextSelectionCursor.Left) {	
			if (mySelection.hasAPartBeforePage(page)) {
				return null;
			}
			final ZLTextElementArea selectionStartArea = mySelection.getStartArea(page);
			if (selectionStartArea != null) {
				return new ZLTextSelection.Point(selectionStartArea.XStart, selectionStartArea.YEnd);
			}
		} else {
			if (mySelection.hasAPartAfterPage(page)) {
				return null;
			}
			final ZLTextElementArea selectionEndArea = mySelection.getEndArea(page);
			if (selectionEndArea != null) {
				return new ZLTextSelection.Point(selectionEndArea.XEnd, selectionEndArea.YEnd);
			}
		}
		return null;
	}

	private int distanceToCursor(int x, int y, ZLTextSelection.Point cursorPoint) {
		if (cursorPoint == null) {
			return Integer.MAX_VALUE;
		}

		final int dX, dY;

		final int w = ZLTextSelectionCursor.getWidth() / 2;
		if (x < cursorPoint.X - w) {
			dX = cursorPoint.X - w - x;
		} else if (x > cursorPoint.X + w) {
			dX = x - cursorPoint.X - w;
		} else {
			dX = 0;
		}

		final int h = ZLTextSelectionCursor.getHeight();
		if (y < cursorPoint.Y) {
			dY = cursorPoint.Y - y;
		} else if (y > cursorPoint.Y + h) {
			dY = y - cursorPoint.Y - h;
		} else {
			dY = 0;
		}

		return Math.max(dX, dY);
	}

	protected ZLTextSelectionCursor findSelectionCursor(int x, int y) {
		return findSelectionCursor(x, y, Integer.MAX_VALUE);
	}

	protected ZLTextSelectionCursor findSelectionCursor(int x, int y, int maxDistance) {
		if (mySelection.isEmpty()) {
			return ZLTextSelectionCursor.None;
		}

		final int leftDistance = distanceToCursor(
			x, y, getSelectionCursorPoint(myCurrentPage, ZLTextSelectionCursor.Left)
		);
		final int rightDistance = distanceToCursor(
			x, y, getSelectionCursorPoint(myCurrentPage, ZLTextSelectionCursor.Right)
		);

		if (rightDistance < leftDistance) {
			return rightDistance <= maxDistance ? ZLTextSelectionCursor.Right : ZLTextSelectionCursor.None;
		} else {
			return leftDistance <= maxDistance ? ZLTextSelectionCursor.Left : ZLTextSelectionCursor.None;
		}
	}

	private void drawSelectionCursor(ZLPaintContext context, ZLTextSelection.Point pt) {
		if (pt == null) {
			return;
		}

		final int w = ZLTextSelectionCursor.getWidth() / 2;
		final int h = ZLTextSelectionCursor.getHeight();
		final int a = ZLTextSelectionCursor.getAccent();
		final int[] xs = { pt.X, pt.X + w, pt.X + w, pt.X - w, pt.X - w };
		final int[] ys = { pt.Y - a, pt.Y, pt.Y + h, pt.Y + h, pt.Y };
		context.setFillColor(context.getBackgroundColor(), 192);
		context.fillPolygon(xs, ys);
		context.setLineColor(getTextColor(ZLTextHyperlink.NO_LINK));
		context.drawPolygonalLine(xs, ys);
	}

	@Override
	public synchronized void preparePage(ZLPaintContext context, PageIndex pageIndex) {
		myContext = context;
		preparePaintInfo(getPage(pageIndex));
	}

	@Override
	public synchronized void paint(ZLPaintContext context, PageIndex pageIndex) {
		myContext = context;
		final ZLFile wallpaper = getWallpaperFile();
		if (wallpaper != null) {
			context.clear(wallpaper, getWallpaperMode());
		} else {
			context.clear(getBackgroundColor());
		}

		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return;
		}

		ZLTextPage page;
		switch (pageIndex) {
			default:
			case current:
				page = myCurrentPage;
				break;
			case previous:
				page = myPreviousPage;
				if (myPreviousPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
					preparePaintInfo(myCurrentPage);
					myPreviousPage.EndCursor.setCursor(myCurrentPage.StartCursor);
					myPreviousPage.PaintState = PaintStateEnum.END_IS_KNOWN;
				}
				break;
			case next:
				page = myNextPage;
				if (myNextPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
					preparePaintInfo(myCurrentPage);
					myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
					myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
				}
		}

		page.TextElementMap.clear();

		preparePaintInfo(page);

		if (page.StartCursor.isNull() || page.EndCursor.isNull()) {
			return;
		}

		final ArrayList<ZLTextLineInfo> lineInfos = page.LineInfos;
		final int[] labels = new int[lineInfos.size() + 1];
		int y = getTopMargin();
		int index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			prepareTextLine(page, info, y);
			y += info.Height + info.Descent + info.VSpaceAfter;
			labels[++index] = page.TextElementMap.size();
		}

		y = getTopMargin();
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			drawTextLine(page, info, labels[index], labels[index + 1], y);
			y += info.Height + info.Descent + info.VSpaceAfter;
			++index;
		}

		final ZLTextRegion selectedElementRegion = getSelectedRegion(page);
		if (selectedElementRegion != null && myHighlightSelectedRegion) {
			selectedElementRegion.draw(context);
		}

		drawSelectionCursor(context, getSelectionCursorPoint(page, ZLTextSelectionCursor.Left));
		drawSelectionCursor(context, getSelectionCursorPoint(page, ZLTextSelectionCursor.Right));
	}

	private ZLTextPage getPage(PageIndex pageIndex) {
		switch (pageIndex) {
			default:
			case current:
				return myCurrentPage;
			case previous:
				return myPreviousPage;
			case next:
				return myNextPage;
		}
	}

	public static final int SCROLLBAR_HIDE = 0;
	public static final int SCROLLBAR_SHOW = 1;
	public static final int SCROLLBAR_SHOW_AS_PROGRESS = 2;

	public abstract int scrollbarType();

	public final boolean isScrollbarShown() {
		return scrollbarType() == SCROLLBAR_SHOW || scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS;
	}

	protected final synchronized int sizeOfTextBeforeParagraph(int paragraphIndex) {
		return myModel != null ? myModel.getTextLength(paragraphIndex - 1) : 0;
	}

	protected final synchronized int sizeOfFullText() {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 1;
		}
		return myModel.getTextLength(myModel.getParagraphsNumber() - 1);
	}

	private final synchronized int getCurrentCharNumber(PageIndex pageIndex, boolean startNotEndOfPage) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 0;
		}
		final ZLTextPage page = getPage(pageIndex);
		preparePaintInfo(page);
		if (startNotEndOfPage) {
			return Math.max(0, sizeOfTextBeforeCursor(page.StartCursor));
		} else {
			int end = sizeOfTextBeforeCursor(page.EndCursor);
			if (end == -1) {
				end = myModel.getTextLength(myModel.getParagraphsNumber() - 1) - 1;
			}
			return Math.max(1, end);
		}
	}

	@Override
	public final synchronized int getScrollbarFullSize() {
		return sizeOfFullText();
	}

	@Override
	public final synchronized int getScrollbarThumbPosition(PageIndex pageIndex) {
		return scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS ? 0 : getCurrentCharNumber(pageIndex, true);
	}

	@Override
	public final synchronized int getScrollbarThumbLength(PageIndex pageIndex) {
		int start = scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS
			? 0 : getCurrentCharNumber(pageIndex, true);
		int end = getCurrentCharNumber(pageIndex, false);
		return Math.max(1, end - start);
	}

	private int sizeOfTextBeforeCursor(ZLTextWordCursor wordCursor) {
		final ZLTextParagraphCursor paragraphCursor = wordCursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return -1;
		}
		final int paragraphIndex = paragraphCursor.Index;
		int sizeOfText = myModel.getTextLength(paragraphIndex - 1);
		final int paragraphLength = paragraphCursor.getParagraphLength();
		if (paragraphLength > 0) {
			sizeOfText +=
				(myModel.getTextLength(paragraphIndex) - sizeOfText)
				* wordCursor.getElementIndex()
				/ paragraphLength;
		}
		return sizeOfText;
	}

	// Can be called only when (myModel.getParagraphsNumber() != 0)
	private synchronized float computeCharsPerPage() {
		setTextStyle(ZLTextStyleCollection.Instance().getBaseStyle());

		final int textWidth = getTextAreaWidth();
		final int textHeight = getTextAreaHeight();

		final int num = myModel.getParagraphsNumber();
		final int totalTextSize = myModel.getTextLength(num - 1);
		final float charsPerParagraph = ((float)totalTextSize) / num;

		final float charWidth = computeCharWidth();

		final int indentWidth = getElementWidth(ZLTextElement.Indent, 0);
		final float effectiveWidth = textWidth - (indentWidth + 0.5f * textWidth) / charsPerParagraph;
		float charsPerLine = Math.min(effectiveWidth / charWidth,
				charsPerParagraph * 1.2f);

		final int strHeight = getWordHeight() + myContext.getDescent();
		final int effectiveHeight = (int) (textHeight - (getTextStyle().getSpaceBefore()
				+ getTextStyle().getSpaceAfter()) / charsPerParagraph);
		final int linesPerPage = effectiveHeight / strHeight;

		return charsPerLine * linesPerPage;
	}

	private synchronized int computeTextPageNumber(int textSize) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 1;
		}

		final float factor = 1.0f / computeCharsPerPage();
		final float pages = textSize * factor;
		return Math.max((int)(pages + 1.0f - 0.5f * factor), 1);
	}

	private static final char[] ourDefaultLetters = "System developers have used modeling languages for decades to specify, visualize, construct, and document systems. The Unified Modeling Language (UML) is one of those languages. UML makes it possible for team members to collaborate by providing a common language that applies to a multitude of different systems. Essentially, it enables you to communicate solutions in a consistent, tool-supported language.".toCharArray();

	private final char[] myLettersBuffer = new char[512];
	private int myLettersBufferLength = 0;
	private ZLTextModel myLettersModel = null;
	private float myCharWidth = -1f;

	private final float computeCharWidth() {
		if (myLettersModel != myModel) {
			myLettersModel = myModel;
			myLettersBufferLength = 0;
			myCharWidth = -1f;

			int paragraph = 0;
			final int textSize = myModel.getTextLength(myModel.getParagraphsNumber() - 1);
			if (textSize > myLettersBuffer.length) {
				paragraph = myModel.findParagraphByTextLength((textSize - myLettersBuffer.length) / 2);
			}
			while (paragraph < myModel.getParagraphsNumber()
					&& myLettersBufferLength < myLettersBuffer.length) {
				ZLTextParagraph.EntryIterator it = myModel.getParagraph(paragraph++).iterator();
				while (it.hasNext()
						&& myLettersBufferLength < myLettersBuffer.length) {
					it.next();
					if (it.getType() == ZLTextParagraph.Entry.TEXT) {
						final int len = Math.min(it.getTextLength(),
								myLettersBuffer.length - myLettersBufferLength);
						System.arraycopy(it.getTextData(), it.getTextOffset(),
								myLettersBuffer, myLettersBufferLength, len);
						myLettersBufferLength += len;
					}
				}
			}

			if (myLettersBufferLength == 0) {
				myLettersBufferLength = Math.min(myLettersBuffer.length, ourDefaultLetters.length);
				System.arraycopy(ourDefaultLetters, 0, myLettersBuffer, 0, myLettersBufferLength);
			}
		}

		if (myCharWidth < 0f) {
			myCharWidth = computeCharWidth(myLettersBuffer, myLettersBufferLength);
		}
		return myCharWidth;
	}

	private final float computeCharWidth(char[] pattern, int length) {
		return myContext.getStringWidth(pattern, 0, length) / ((float)length);
	}

	public static class PagePosition {
		public final int Current;
		public final int Total;

		PagePosition(int current, int total) {
			Current = current;
			Total = total;
		}
	}

	public final synchronized PagePosition pagePosition() {
		int current = computeTextPageNumber(getCurrentCharNumber(PageIndex.current, false));
		int total = computeTextPageNumber(sizeOfFullText());

		if (total > 3) {
			return new PagePosition(current, total);
		}

		preparePaintInfo(myCurrentPage);
		ZLTextWordCursor cursor = myCurrentPage.StartCursor;
		if (cursor == null || cursor.isNull()) {
			return new PagePosition(current, total);
		}

		if (cursor.isStartOfText()) {
			current = 1;
		} else {
			ZLTextWordCursor prevCursor = myPreviousPage.StartCursor;
			if (prevCursor == null || prevCursor.isNull()) {
				preparePaintInfo(myPreviousPage);
				prevCursor = myPreviousPage.StartCursor;
			}
			if (prevCursor != null && !prevCursor.isNull()) {
				current = prevCursor.isStartOfText() ? 2 : 3;
			}
		}

		total = current;
		cursor = myCurrentPage.EndCursor;
		if (cursor == null || cursor.isNull()) {
			return new PagePosition(current, total);
		}
		if (!cursor.isEndOfText()) {
			ZLTextWordCursor nextCursor = myNextPage.EndCursor;
			if (nextCursor == null || nextCursor.isNull()) {
				preparePaintInfo(myNextPage);
				nextCursor = myNextPage.EndCursor;
			}
			if (nextCursor != null) {
				total += nextCursor.isEndOfText() ? 1 : 2;
			}
		}

		return new PagePosition(current, total);
	}

	public final synchronized void gotoPage(int page) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return;
		}

		final float factor = computeCharsPerPage();
		final float textSize = page * factor;

		int intTextSize = (int) textSize;
		int paragraphIndex = myModel.findParagraphByTextLength(intTextSize);

		if (paragraphIndex > 0 && myModel.getTextLength(paragraphIndex) > intTextSize) {
			--paragraphIndex;
		}
		intTextSize = myModel.getTextLength(paragraphIndex);

		int sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		while (paragraphIndex > 0 && intTextSize == sizeOfTextBefore) {
			--paragraphIndex;
			intTextSize = sizeOfTextBefore;
			sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		}

		final int paragraphLength = intTextSize - sizeOfTextBefore;

		final int wordIndex;
		if (paragraphLength == 0) {
			wordIndex = 0;
		} else {
			preparePaintInfo(myCurrentPage);
			final ZLTextWordCursor cursor = new ZLTextWordCursor(myCurrentPage.EndCursor);
			cursor.moveToParagraph(paragraphIndex);
			wordIndex = cursor.getParagraphCursor().getParagraphLength();
		}

		gotoPositionByEnd(paragraphIndex, wordIndex, 0);
	}

	public void gotoHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphIndex() == 0) {
			return;
		}
		gotoPosition(0, 0, 0);
		preparePaintInfo();
	}

	private void drawBackgroung(
		ZLTextAbstractHighlighting highligting, ZLColor color,
		ZLTextPage page, ZLTextLineInfo info, int from, int to, int y
	) {
		if (!highligting.isEmpty() && from != to) {
			final ZLTextElementArea fromArea = page.TextElementMap.get(from);
			final ZLTextElementArea toArea = page.TextElementMap.get(to - 1);
			final ZLTextElementArea selectionStartArea = highligting.getStartArea(page);
			final ZLTextElementArea selectionEndArea = highligting.getEndArea(page);
			if (selectionStartArea != null
				&& selectionEndArea != null
				&& selectionStartArea.compareTo(toArea) <= 0
				&& selectionEndArea.compareTo(fromArea) >= 0) {
				final int top = y + 1;
				int left, right, bottom = y + info.Height + info.Descent;
				if (selectionStartArea.compareTo(fromArea) < 0) {
					left = getLeftMargin();
				} else {
					left = selectionStartArea.XStart;
				}
				if (selectionEndArea.compareTo(toArea) > 0) {
					right = getRightLine();
					bottom += info.VSpaceAfter;
				} else {
					right = selectionEndArea.XEnd;
				}
				myContext.setFillColor(color);
				myContext.fillRectangle(left, top, right, bottom);
			}
		}
	}

	private static final char[] SPACE = new char[] { ' ' };
	private void drawTextLine(ZLTextPage page, ZLTextLineInfo info, int from, int to, int y) {
		drawBackgroung(mySelection, getSelectedBackgroundColor(), page, info, from, to, y);
		drawBackgroung(myHighlighting, getHighlightingColor(), page, info, from, to, y);

		final ZLPaintContext context = myContext;
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		int index = from;
		final int endElementIndex = info.EndElementIndex;
		int charIndex = info.RealStartCharIndex;
		for (int wordIndex = info.RealStartElementIndex; wordIndex != endElementIndex && index < to; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final ZLTextElementArea area = page.TextElementMap.get(index);
			if (element == area.Element) {
				++index;
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int areaX = area.XStart;
				final int areaY = area.YEnd - getElementDescent(element) - getTextStyle().getVerticalShift();
				if (element instanceof ZLTextWord) {
					drawWord(
						areaX, areaY, (ZLTextWord)element, charIndex, -1, false,
						mySelection.isAreaSelected(area)
							? getSelectedForegroundColor() : getTextColor(getTextStyle().Hyperlink)
					);
				} else if (element instanceof ZLTextImageElement) {
					final ZLTextImageElement imageElement = (ZLTextImageElement)element;
					context.drawImage(
						areaX, areaY,
						imageElement.ImageData,
						getTextAreaSize(),
						getScalingType(imageElement)
					);
				} else if (element == ZLTextElement.HSpace) {
					final int cw = context.getSpaceWidth();
					/*
					context.setFillColor(getHighlightingColor());
					context.fillRectangle(
						area.XStart, areaY - context.getStringHeight(),
						area.XEnd - 1, areaY + context.getDescent()
					);
					*/
					for (int len = 0; len < area.XEnd - area.XStart; len += cw) {
						context.drawString(areaX + len, areaY, SPACE, 0, 1);
					}
				}
			}
		}
		if (index != to) {
			ZLTextElementArea area = page.TextElementMap.get(index++);
			if (area.ChangeStyle) {
				setTextStyle(area.Style);
			}
			final int start = info.StartElementIndex == info.EndElementIndex
				? info.StartCharIndex : 0;
			final int len = info.EndCharIndex - start;
			final ZLTextWord word = (ZLTextWord)paragraph.getElement(info.EndElementIndex);
			drawWord(
				area.XStart, area.YEnd - context.getDescent() - getTextStyle().getVerticalShift(),
				word, start, len, area.AddHyphenationSign,
				mySelection.isAreaSelected(area)
					? getSelectedForegroundColor() : getTextColor(getTextStyle().Hyperlink)
			);
		}
	}

	private void buildInfos(ZLTextPage page, ZLTextWordCursor start, ZLTextWordCursor result) {
		result.setCursor(start);
		int textAreaHeight = getTextAreaHeight();
		page.LineInfos.clear();
		int counter = 0;
		do {
			resetTextStyle();
			final ZLTextParagraphCursor paragraphCursor = result.getParagraphCursor();
			final int wordIndex = result.getElementIndex();
			applyStyleChanges(paragraphCursor, 0, wordIndex);
			ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, wordIndex, result.getCharIndex(), getTextStyle());
			final int endIndex = info.ParagraphCursorLength;
			while (info.EndElementIndex != endIndex) {
				info = processTextLine(paragraphCursor, info.EndElementIndex, info.EndCharIndex, endIndex);
				textAreaHeight -= info.Height + info.Descent;
				if (textAreaHeight < 0 && counter > 0) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				result.moveTo(info.EndElementIndex, info.EndCharIndex);
				page.LineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (result.isEndOfParagraph() && result.nextParagraph() && !result.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		resetTextStyle();
	}

	private boolean isHyphenationPossible() {
		return ZLTextStyleCollection.Instance().getBaseStyle().AutoHyphenationOption.getValue()
			&& getTextStyle().allowHyphenations();
	}

	private ZLTextLineInfo processTextLine(
		ZLTextParagraphCursor paragraphCursor,
		final int startIndex,
		final int startCharIndex,
		final int endIndex
	) {
		final ZLPaintContext context = myContext;
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharIndex, getTextStyle());
		final ZLTextLineInfo cachedInfo = myLineInfoCache.get(info);
		if (cachedInfo != null) {
			applyStyleChanges(paragraphCursor, startIndex, cachedInfo.EndElementIndex);
			return cachedInfo;
		}

		int currentElementIndex = startIndex;
		int currentCharIndex = startCharIndex;
		final boolean isFirstLine = startIndex == 0 && startCharIndex == 0;

		if (isFirstLine) {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			while (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
				++currentElementIndex;
				currentCharIndex = 0;
				if (currentElementIndex == endIndex) {
					break;
				}
				element = paragraphCursor.getElement(currentElementIndex);
			}
			info.StartStyle = getTextStyle();
			info.RealStartElementIndex = currentElementIndex;
			info.RealStartCharIndex = currentCharIndex;
		}

		ZLTextStyle storedStyle = getTextStyle();

		info.LeftIndent = getTextStyle().getLeftIndent();
		if (isFirstLine) {
			info.LeftIndent += getTextStyle().getFirstLineIndentDelta();
		}

		info.Width = info.LeftIndent;

		if (info.RealStartElementIndex == endIndex) {
			info.EndElementIndex = info.RealStartElementIndex;
			info.EndCharIndex = info.RealStartCharIndex;
			return info;
		}

		int newWidth = info.Width;
		int newHeight = info.Height;
		int newDescent = info.Descent;
		int maxWidth = getTextAreaWidth() - getTextStyle().getRightIndent();
		boolean wordOccurred = false;
		boolean isVisible = false;
		int lastSpaceWidth = 0;
		int internalSpaceCounter = 0;
		boolean removeLastSpace = false;

		do {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			newWidth += getElementWidth(element, currentCharIndex);
			newHeight = Math.max(newHeight, getElementHeight(element));
			newDescent = Math.max(newDescent, getElementDescent(element));
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred) {
					wordOccurred = false;
					internalSpaceCounter++;
					lastSpaceWidth = context.getSpaceWidth();
					newWidth += lastSpaceWidth;
				}
			} else if (element instanceof ZLTextWord) {
				wordOccurred = true;
				isVisible = true;
			} else if (element instanceof ZLTextImageElement) {
				wordOccurred = true;
				isVisible = true;
			} else if (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
			}
			if (newWidth > maxWidth) {
				if (info.EndElementIndex != startIndex || element instanceof ZLTextWord) {
					break;
				}
			}
			ZLTextElement previousElement = element;
			++currentElementIndex;
			currentCharIndex = 0;
			boolean allowBreak = currentElementIndex == endIndex;
			if (!allowBreak) {
				element = paragraphCursor.getElement(currentElementIndex);
				allowBreak = ((!(element instanceof ZLTextWord) || previousElement instanceof ZLTextWord) &&
						!(element instanceof ZLTextImageElement) &&
						!(element instanceof ZLTextControlElement));
			}
			if (allowBreak) {
				info.IsVisible = isVisible;
				info.Width = newWidth;
				if (info.Height < newHeight) {
					info.Height = newHeight;
				}
				if (info.Descent < newDescent) {
					info.Descent = newDescent;
				}
				info.EndElementIndex = currentElementIndex;
				info.EndCharIndex = currentCharIndex;
				info.SpaceCounter = internalSpaceCounter;
				storedStyle = getTextStyle();
				removeLastSpace = !wordOccurred && (internalSpaceCounter > 0);
			}
		} while (currentElementIndex != endIndex);

		if (currentElementIndex != endIndex &&
			(isHyphenationPossible() || info.EndElementIndex == startIndex)) {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord)element;
				newWidth -= getWordWidth(word, currentCharIndex);
				int spaceLeft = maxWidth - newWidth;
				if ((word.Length > 3 && spaceLeft > 2 * context.getSpaceWidth())
					|| info.EndElementIndex == startIndex) {
					ZLTextHyphenationInfo hyphenationInfo = ZLTextHyphenator.Instance().getInfo(word);
					int hyphenationPosition = word.Length - 1;
					int subwordWidth = 0;
					for(; hyphenationPosition > currentCharIndex; hyphenationPosition--) {
						if (hyphenationInfo.isHyphenationPossible(hyphenationPosition)) {
							subwordWidth = getWordWidth(
								word,
								currentCharIndex,
								hyphenationPosition - currentCharIndex,
								word.Data[word.Offset + hyphenationPosition - 1] != '-'
							);
							if (subwordWidth <= spaceLeft) {
								break;
							}
						}
					}
					if (hyphenationPosition == currentCharIndex && info.EndElementIndex == startIndex) {
						hyphenationPosition = word.Length == currentCharIndex + 1 ? word.Length : word.Length - 1;
						subwordWidth = getWordWidth(word, currentCharIndex, word.Length - currentCharIndex, false);
						for(; hyphenationPosition > currentCharIndex + 1; hyphenationPosition--) {
							subwordWidth = getWordWidth(
								word,
								currentCharIndex,
								hyphenationPosition - currentCharIndex,
								word.Data[word.Offset + hyphenationPosition - 1] != '-'
							);
							if (subwordWidth <= spaceLeft) {
								break;
							}
						}
					}
					if (hyphenationPosition > currentCharIndex) {
						info.IsVisible = true;
						info.Width = newWidth + subwordWidth;
						if (info.Height < newHeight) {
							info.Height = newHeight;
						}
						if (info.Descent < newDescent) {
							info.Descent = newDescent;
						}
						info.EndElementIndex = currentElementIndex;
						info.EndCharIndex = hyphenationPosition;
						info.SpaceCounter = internalSpaceCounter;
						storedStyle = getTextStyle();
						removeLastSpace = false;
					}
				}
			}
		}

		if (removeLastSpace) {
			info.Width -= lastSpaceWidth;
			info.SpaceCounter--;
		}

		setTextStyle(storedStyle);

		if (isFirstLine) {
			info.Height += info.StartStyle.getSpaceBefore();
		}
		if (info.isEndOfParagraph()) {
			info.VSpaceAfter = getTextStyle().getSpaceAfter();
		}

		if (info.EndElementIndex != endIndex || endIndex == info.ParagraphCursorLength) {
			myLineInfoCache.put(info, info);
		}

		return info;
	}

	private void prepareTextLine(ZLTextPage page, ZLTextLineInfo info, int y) {
		y = Math.min(y + info.Height, getBottomLine());

		final ZLPaintContext context = myContext;
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;

		setTextStyle(info.StartStyle);
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		int x = getLeftMargin() + info.LeftIndent;
		final int maxWidth = getTextAreaWidth();
		switch (getTextStyle().getAlignment()) {
			case ZLTextAlignmentType.ALIGN_RIGHT:
				x += maxWidth - getTextStyle().getRightIndent() - info.Width;
				break;
			case ZLTextAlignmentType.ALIGN_CENTER:
				x += (maxWidth - getTextStyle().getRightIndent() - info.Width) / 2;
				break;
			case ZLTextAlignmentType.ALIGN_JUSTIFY:
				if (!endOfParagraph && (paragraphCursor.getElement(info.EndElementIndex) != ZLTextElement.AfterParagraph)) {
					fullCorrection = maxWidth - getTextStyle().getRightIndent() - info.Width;
				}
				break;
			case ZLTextAlignmentType.ALIGN_LEFT:
			case ZLTextAlignmentType.ALIGN_UNDEFINED:
				break;
		}

		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		final int paragraphIndex = paragraph.Index;
		final int endElementIndex = info.EndElementIndex;
		int charIndex = info.RealStartCharIndex;
		ZLTextElementArea spaceElement = null;
		for (int wordIndex = info.RealStartElementIndex; wordIndex != endElementIndex; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final int width = getElementWidth(element, charIndex);
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					final int correction = fullCorrection / spaceCounter;
					final int spaceLength = context.getSpaceWidth() + correction;
					if (getTextStyle().isUnderline()) {
						spaceElement = new ZLTextElementArea(
							paragraphIndex, wordIndex, 0,
							0, // length
							true, // is last in element
							false, // add hyphenation sign
							false, // changed style
							getTextStyle(), element, x, x + spaceLength, y, y
						);
					} else {
						spaceElement = null;
					}
					x += spaceLength;
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}
			} else if (element instanceof ZLTextWord || element instanceof ZLTextImageElement) {
				final int height = getElementHeight(element);
				final int descent = getElementDescent(element);
				final int length = element instanceof ZLTextWord ? ((ZLTextWord)element).Length : 0;
				if (spaceElement != null) {
					page.TextElementMap.add(spaceElement);
					spaceElement = null;
				}
				page.TextElementMap.add(new ZLTextElementArea(
					paragraphIndex, wordIndex, charIndex,
					length - charIndex,
					true, // is last in element
					false, // add hyphenation sign
					changeStyle, getTextStyle(), element,
					x, x + width - 1, y - height + 1, y + descent
				));
				changeStyle = false;
				wordOccurred = true;
			} else if (isStyleChangeElement(element)) {
				applyStyleChangeElement(element);
				changeStyle = true;
			}
			x += width;
		}
		if (!endOfParagraph) {
			final int len = info.EndCharIndex;
			if (len > 0) {
				final int wordIndex = info.EndElementIndex;
				final ZLTextWord word = (ZLTextWord)paragraph.getElement(wordIndex);
				final boolean addHyphenationSign = word.Data[word.Offset + len - 1] != '-';
				final int width = getWordWidth(word, 0, len, addHyphenationSign);
				final int height = getElementHeight(word);
				final int descent = context.getDescent();
				page.TextElementMap.add(
					new ZLTextElementArea(
						paragraphIndex, wordIndex, 0,
						len,
						false, // is last in element
						addHyphenationSign,
						changeStyle, getTextStyle(), word,
						x, x + width - 1, y - height + 1, y + descent
					)
				);
			}
		}
	}

	public synchronized final void scrollPage(boolean forward, int scrollingMode, int value) {
		preparePaintInfo(myCurrentPage);
		myPreviousPage.reset();
		myNextPage.reset();
		if (myCurrentPage.PaintState == PaintStateEnum.READY) {
			myCurrentPage.PaintState = forward ? PaintStateEnum.TO_SCROLL_FORWARD : PaintStateEnum.TO_SCROLL_BACKWARD;
			myScrollingMode = scrollingMode;
			myOverlappingValue = value;
		}
	}

	public final synchronized void gotoPosition(ZLTextPosition position) {
		if (position != null) {
			gotoPosition(position.getParagraphIndex(), position.getElementIndex(), position.getCharIndex());
		}
	}

	public final synchronized void gotoPosition(int paragraphIndex, int wordIndex, int charIndex) {
		if (myModel != null && myModel.getParagraphsNumber() > 0) {
			Application.getViewWidget().reset();
			myCurrentPage.moveStartCursor(paragraphIndex, wordIndex, charIndex);
			myPreviousPage.reset();
			myNextPage.reset();
			preparePaintInfo(myCurrentPage);
			if (myCurrentPage.isEmptyPage()) {
				scrollPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			}
		}
	}

	private final synchronized void gotoPositionByEnd(int paragraphIndex, int wordIndex, int charIndex) {
		if (myModel != null && myModel.getParagraphsNumber() > 0) {
			myCurrentPage.moveEndCursor(paragraphIndex, wordIndex, charIndex);
			myPreviousPage.reset();
			myNextPage.reset();
			preparePaintInfo(myCurrentPage);
			if (myCurrentPage.isEmptyPage()) {
				scrollPage(false, ScrollingMode.NO_OVERLAPPING, 0);
			}
		}
	}

	protected synchronized void preparePaintInfo() {
		myPreviousPage.reset();
		myNextPage.reset();
		preparePaintInfo(myCurrentPage);
	}

	private synchronized void preparePaintInfo(ZLTextPage page) {
		int newWidth = getTextAreaWidth();
		int newHeight = getTextAreaHeight();
		if (newWidth != page.OldWidth || newHeight != page.OldHeight) {
			page.OldWidth = newWidth;
			page.OldHeight = newHeight;
			if (page.PaintState != PaintStateEnum.NOTHING_TO_PAINT) {
				page.LineInfos.clear();
				if (page == myPreviousPage) {
					if (!page.EndCursor.isNull()) {
						page.StartCursor.reset();
						page.PaintState = PaintStateEnum.END_IS_KNOWN;
					} else if (!page.StartCursor.isNull()) {
						page.EndCursor.reset();
						page.PaintState = PaintStateEnum.START_IS_KNOWN;
					}
				} else {
					if (!page.StartCursor.isNull()) {
						page.EndCursor.reset();
						page.PaintState = PaintStateEnum.START_IS_KNOWN;
					} else if (!page.EndCursor.isNull()) {
						page.StartCursor.reset();
						page.PaintState = PaintStateEnum.END_IS_KNOWN;
					}
				}
			}
		}

		if (page.PaintState == PaintStateEnum.NOTHING_TO_PAINT || page.PaintState == PaintStateEnum.READY) {
			return;
		}
		final int oldState = page.PaintState;

		final HashMap<ZLTextLineInfo,ZLTextLineInfo> cache = myLineInfoCache;
		for (ZLTextLineInfo info : page.LineInfos) {
			cache.put(info, info);
		}

		switch (page.PaintState) {
			default:
				break;
			case PaintStateEnum.TO_SCROLL_FORWARD:
				if (!page.EndCursor.getParagraphCursor().isLast() || !page.EndCursor.isEndOfParagraph()) {
					final ZLTextWordCursor startCursor = new ZLTextWordCursor();
					switch (myScrollingMode) {
						case ScrollingMode.NO_OVERLAPPING:
							break;
						case ScrollingMode.KEEP_LINES:
							page.findLineFromEnd(startCursor, myOverlappingValue);
							break;
						case ScrollingMode.SCROLL_LINES:
							page.findLineFromStart(startCursor, myOverlappingValue);
							if (startCursor.isEndOfParagraph()) {
								startCursor.nextParagraph();
							}
							break;
						case ScrollingMode.SCROLL_PERCENTAGE:
							page.findPercentFromStart(startCursor, getTextAreaHeight(), myOverlappingValue);
							break;
					}

					if (!startCursor.isNull() && startCursor.samePositionAs(page.StartCursor)) {
						page.findLineFromStart(startCursor, 1);
					}

					if (!startCursor.isNull()) {
						final ZLTextWordCursor endCursor = new ZLTextWordCursor();
						buildInfos(page, startCursor, endCursor);
						if (!page.isEmptyPage() && (myScrollingMode != ScrollingMode.KEEP_LINES || !endCursor.samePositionAs(page.EndCursor))) {
							page.StartCursor.setCursor(startCursor);
							page.EndCursor.setCursor(endCursor);
							break;
						}
					}

					page.StartCursor.setCursor(page.EndCursor);
					buildInfos(page, page.StartCursor, page.EndCursor);
				}
				break;
			case PaintStateEnum.TO_SCROLL_BACKWARD:
				if (!page.StartCursor.getParagraphCursor().isFirst() || !page.StartCursor.isStartOfParagraph()) {
					switch (myScrollingMode) {
						case ScrollingMode.NO_OVERLAPPING:
							page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							break;
						case ScrollingMode.KEEP_LINES:
						{
							ZLTextWordCursor endCursor = new ZLTextWordCursor();
							page.findLineFromStart(endCursor, myOverlappingValue);
							if (!endCursor.isNull() && endCursor.samePositionAs(page.EndCursor)) {
								page.findLineFromEnd(endCursor, 1);
							}
							if (!endCursor.isNull()) {
								ZLTextWordCursor startCursor = findStart(endCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight());
								if (startCursor.samePositionAs(page.StartCursor)) {
									page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
								} else {
									page.StartCursor.setCursor(startCursor);
								}
							} else {
								page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							}
							break;
						}
						case ScrollingMode.SCROLL_LINES:
							page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.LINE_UNIT, myOverlappingValue));
							break;
						case ScrollingMode.SCROLL_PERCENTAGE:
							page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight() * myOverlappingValue / 100));
							break;
					}
					buildInfos(page, page.StartCursor, page.EndCursor);
					if (page.isEmptyPage()) {
						page.StartCursor.setCursor(findStart(page.StartCursor, SizeUnit.LINE_UNIT, 1));
						buildInfos(page, page.StartCursor, page.EndCursor);
					}
				}
				break;
			case PaintStateEnum.START_IS_KNOWN:
				buildInfos(page, page.StartCursor, page.EndCursor);
				break;
			case PaintStateEnum.END_IS_KNOWN:
				page.StartCursor.setCursor(findStart(page.EndCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
				buildInfos(page, page.StartCursor, page.EndCursor);
				break;
		}
		page.PaintState = PaintStateEnum.READY;
		// TODO: cache?
		myLineInfoCache.clear();

		if (page == myCurrentPage) {
			if (oldState != PaintStateEnum.START_IS_KNOWN) {
				myPreviousPage.reset();
			}
			if (oldState != PaintStateEnum.END_IS_KNOWN) {
				myNextPage.reset();
			}
		}
	}

	public void clearCaches() {
		resetMetrics();
		rebuildPaintInfo();
		Application.getViewWidget().reset();
		myCharWidth = -1;
	}

	protected void rebuildPaintInfo() {
		myPreviousPage.reset();
		myNextPage.reset();
		ZLTextParagraphCursorCache.clear();

		if (myCurrentPage.PaintState != PaintStateEnum.NOTHING_TO_PAINT) {
			myCurrentPage.LineInfos.clear();
			if (!myCurrentPage.StartCursor.isNull()) {
				myCurrentPage.StartCursor.rebuild();
				myCurrentPage.EndCursor.reset();
				myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
			} else if (!myCurrentPage.EndCursor.isNull()) {
				myCurrentPage.EndCursor.rebuild();
				myCurrentPage.StartCursor.reset();
				myCurrentPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			}
		}

		myLineInfoCache.clear();
	}

	private int infoSize(ZLTextLineInfo info, int unit) {
		return (unit == SizeUnit.PIXEL_UNIT) ? (info.Height + info.Descent + info.VSpaceAfter) : (info.IsVisible ? 1 : 0);
	}

	private int paragraphSize(ZLTextWordCursor cursor, boolean beforeCurrentPosition, int unit) {
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return 0;
		}
		final int endElementIndex =
			beforeCurrentPosition ? cursor.getElementIndex() : paragraphCursor.getParagraphLength();

		resetTextStyle();

		int size = 0;

		int wordIndex = 0;
		int charIndex = 0;
		while (wordIndex != endElementIndex) {
			ZLTextLineInfo info = processTextLine(paragraphCursor, wordIndex, charIndex, endElementIndex);
			wordIndex = info.EndElementIndex;
			charIndex = info.EndCharIndex;
			size += infoSize(info, unit);
		}

		return size;
	}

	private void skip(ZLTextWordCursor cursor, int unit, int size) {
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return;
		}
		final int endElementIndex = paragraphCursor.getParagraphLength();

		resetTextStyle();
		applyStyleChanges(paragraphCursor, 0, cursor.getElementIndex());

		while (!cursor.isEndOfParagraph() && (size > 0)) {
			ZLTextLineInfo info = processTextLine(paragraphCursor, cursor.getElementIndex(), cursor.getCharIndex(), endElementIndex);
			cursor.moveTo(info.EndElementIndex, info.EndCharIndex);
			size -= infoSize(info, unit);
		}
	}

	private ZLTextWordCursor findStart(ZLTextWordCursor end, int unit, int size) {
		final ZLTextWordCursor start = new ZLTextWordCursor(end);
		size -= paragraphSize(start, true, unit);
		boolean positionChanged = !start.isStartOfParagraph();
		start.moveToParagraphStart();
		while (size > 0) {
			if (positionChanged && start.getParagraphCursor().isEndOfSection()) {
				break;
			}
			if (!start.previousParagraph()) {
				break;
			}
			if (!start.getParagraphCursor().isEndOfSection()) {
				positionChanged = true;
			}
			size -= paragraphSize(start, false, unit);
		}
		skip(start, unit, -size);

		if (unit == SizeUnit.PIXEL_UNIT) {
			boolean sameStart = start.samePositionAs(end);
			if (!sameStart && start.isEndOfParagraph() && end.isStartOfParagraph()) {
				ZLTextWordCursor startCopy = start;
				startCopy.nextParagraph();
				sameStart = startCopy.samePositionAs(end);
			}
			if (sameStart) {
				start.setCursor(findStart(end, SizeUnit.LINE_UNIT, 1));
			}
		}

		return start;
	}

	protected ZLTextElementArea getElementByCoordinates(int x, int y) {
		return myCurrentPage.TextElementMap.binarySearch(x, y);
	}

	@Override
	public boolean onFingerMove(int x, int y) {
		return false;
	}

	@Override
	public boolean onFingerRelease(int x, int y) {
		return false;
	}

	public void hideSelectedRegionBorder() {
		myHighlightSelectedRegion = false;
		Application.getViewWidget().reset();
	}

	private ZLTextRegion getSelectedRegion(ZLTextPage page) {
		return page.TextElementMap.getRegion(mySelectedRegionSoul);
	}

	public ZLTextRegion getSelectedRegion() {
		return getSelectedRegion(myCurrentPage);
	}

	protected ZLTextRegion findRegion(int x, int y, ZLTextRegion.Filter filter) {
		return findRegion(x, y, Integer.MAX_VALUE - 1, filter);
	}

	protected ZLTextRegion findRegion(int x, int y, int maxDistance, ZLTextRegion.Filter filter) {
		return myCurrentPage.TextElementMap.findRegion(x, y, maxDistance, filter);
	}

	public void selectRegion(ZLTextRegion region) {
		final ZLTextRegion.Soul soul = region != null ? region.getSoul() : null;
		if (soul == null || !soul.equals(mySelectedRegionSoul)) {
			myHighlightSelectedRegion = true;
		}
		mySelectedRegionSoul = soul;
	}

	protected boolean initSelection(int x, int y) {
		y -= ZLTextSelectionCursor.getHeight() / 2 + ZLTextSelectionCursor.getAccent() / 2;
		if (!mySelection.start(x, y)) {
			return false;
		}
		Application.getViewWidget().reset();
		Application.getViewWidget().repaint();
		return true;
	}

	public void clearSelection() {
		if (mySelection.clear()) {
			Application.getViewWidget().reset();
			Application.getViewWidget().repaint();
		}
	}

	public int getSelectionStartY() {
		if (mySelection.isEmpty()) {
			return 0;
		}
		final ZLTextElementArea selectionStartArea = mySelection.getStartArea(myCurrentPage);
		if (selectionStartArea != null) {
			return selectionStartArea.YStart;
		}
		if (mySelection.hasAPartBeforePage(myCurrentPage)) {
			final ZLTextElementArea firstArea = myCurrentPage.TextElementMap.getFirstArea();
			return firstArea != null ? firstArea.YStart : 0;
		} else {
			final ZLTextElementArea lastArea = myCurrentPage.TextElementMap.getLastArea();
			return lastArea != null ? lastArea.YEnd : 0;
		}
	}

	public int getSelectionEndY() {
		if (mySelection.isEmpty()) {
			return 0;
		}
		final ZLTextElementArea selectionEndArea = mySelection.getEndArea(myCurrentPage);
		if (selectionEndArea != null) {
			return selectionEndArea.YEnd;
		}
		if (mySelection.hasAPartAfterPage(myCurrentPage)) {
			final ZLTextElementArea lastArea = myCurrentPage.TextElementMap.getLastArea();
			return lastArea != null ? lastArea.YEnd : 0;
		} else {
			final ZLTextElementArea firstArea = myCurrentPage.TextElementMap.getFirstArea();
			return firstArea != null ? firstArea.YStart : 0;
		}
	}

	public ZLTextPosition getSelectionStartPosition() {
		return mySelection.getStartPosition();
	}

	public ZLTextPosition getSelectionEndPosition() {
		return mySelection.getEndPosition();
	}

	public boolean isSelectionEmpty() {
		return mySelection.isEmpty();
	}

	public void resetRegionPointer() {
		mySelectedRegionSoul = null;
		myHighlightSelectedRegion = true;
	}

	public ZLTextRegion nextRegion(Direction direction, ZLTextRegion.Filter filter) {
		return myCurrentPage.TextElementMap.nextRegion(getSelectedRegion(), direction, filter);
	}

	@Override
	public boolean canScroll(PageIndex index) {
		switch (index) {
			default:
				return true;
			case next:
			{
				final ZLTextWordCursor cursor = getEndCursor();
				return
					cursor != null &&
					!cursor.isNull() &&
					(!cursor.isEndOfParagraph() || !cursor.getParagraphCursor().isLast());
			}
			case previous:
			{
				final ZLTextWordCursor cursor = getStartCursor();
				return
					cursor != null &&
					!cursor.isNull() &&
					(!cursor.isStartOfParagraph() || !cursor.getParagraphCursor().isFirst());
			}
		}
	}
}
