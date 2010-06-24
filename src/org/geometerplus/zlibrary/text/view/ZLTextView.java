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
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.hyphenation.*;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

public abstract class ZLTextView extends ZLTextViewBase {
	public interface ScrollingMode {
		int NO_OVERLAPPING = 0;
		int KEEP_LINES = 1;
		int SCROLL_LINES = 2;
		int SCROLL_PERCENTAGE = 3;
	};

	private ZLTextModel myModel;
	private final ZLTextSelectionModel mySelectionModel;

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

	public ZLTextView(ZLPaintContext context) {
		super(context);
 		mySelectionModel = new ZLTextSelectionModel(this);
	}

	public synchronized void setModel(ZLTextModel model) {
		ZLTextParagraphCursorCache.clear();
		mySelectionModel.clear();

		myModel = model;
		myCurrentPage.reset();
		myPreviousPage.reset();
		myNextPage.reset();
		setScrollingActive(false);
		if (myModel != null) {
			final int paragraphsNumber = myModel.getParagraphsNumber();
			if (paragraphsNumber > 0) {
				myCurrentPage.moveStartCursor(ZLTextParagraphCursor.cursor(myModel, 0));
			}
		}
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
		if ((myCurrentPage.StartCursor.getParagraphIndex() != mark.ParagraphIndex) || (myCurrentPage.StartCursor.getMark().compareTo(mark) > 0)) {
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
			ZLApplication.Instance().repaintView();
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
			ZLApplication.Instance().repaintView();
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
			ZLApplication.Instance().repaintView();
		}
	}

	public boolean findResultsAreEmpty() {
		return (myModel == null) || myModel.getMarks().isEmpty();
	}

	private volatile boolean myScrollingIsActive;
	protected boolean isScrollingActive() {
		return myScrollingIsActive;
	}
	protected void setScrollingActive(boolean active) {
		myScrollingIsActive = active;
	}

	public final synchronized void startAutoScrolling(int viewPage) {
		if (isScrollingActive()) {
			return;
		}

		setScrollingActive(true);
		ZLApplication.Instance().startViewAutoScrolling(viewPage);
	}

	public synchronized void onScrollingFinished(int viewPage) {
		setScrollingActive(false);
		switch (viewPage) {
			case PAGE_CENTRAL:
				break;
			case PAGE_LEFT:
			case PAGE_TOP:
			{
				ZLTextPage swap = myNextPage;
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
				}
				break;
			}
			case PAGE_RIGHT:
			case PAGE_BOTTOM:
			{
				ZLTextPage swap = myPreviousPage;
				myPreviousPage = myCurrentPage;
				myCurrentPage = myNextPage;
				myNextPage = swap;
				myNextPage.reset();
				if (myCurrentPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
					preparePaintInfo(myPreviousPage);
					myCurrentPage.StartCursor.setCursor(myPreviousPage.EndCursor);
					myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
				}
				break;
			}
		}
	}

	public synchronized void paint(int viewPage) {
		Context.clear(getBackgroundColor());

		if ((myModel == null) || (myModel.getParagraphsNumber() == 0)) {
			return;
		}

		ZLTextPage page;
		switch (viewPage) {
			default:
			case PAGE_CENTRAL:
				page = myCurrentPage;
				break;
			case PAGE_TOP:
			case PAGE_LEFT:
				page = myPreviousPage;
				if (myPreviousPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
					preparePaintInfo(myCurrentPage);
					myPreviousPage.EndCursor.setCursor(myCurrentPage.StartCursor);
					myPreviousPage.PaintState = PaintStateEnum.END_IS_KNOWN;
				}
				break;
			case PAGE_BOTTOM:
			case PAGE_RIGHT:
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

		if (page == myCurrentPage) {
			mySelectionModel.update();
		}

		y = getTopMargin();
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			drawTextLine(page, info, labels[index], labels[index + 1], y);
			y += info.Height + info.Descent + info.VSpaceAfter;
			++index;
		}

		final ZLTextHyperlinkArea hyperlinkArea = getCurrentHyperlinkArea(page);
		if (hyperlinkArea != null) {
			hyperlinkArea.draw(Context);
		}
	}

	private ZLTextPage getPage(int viewPage) {
		switch (viewPage) {
			default:
			case PAGE_CENTRAL:
				return myCurrentPage;
			case PAGE_TOP:
			case PAGE_LEFT:
				return myPreviousPage;
			case PAGE_BOTTOM:
			case PAGE_RIGHT:
				return myNextPage;
		}
	}

	public static final int SCROLLBAR_HIDE = 0;
	public static final int SCROLLBAR_SHOW = 1;
	public static final int SCROLLBAR_SHOW_AS_PROGRESS = 2;
	public abstract int scrollbarType();

	public final boolean showScrollbar() {
		return scrollbarType() != SCROLLBAR_HIDE;
	}

	public final synchronized int getScrollbarFullSize() {
		if ((myModel == null) || (myModel.getParagraphsNumber() == 0)) {
			return 1;
		}
		return myModel.getTextLength(myModel.getParagraphsNumber() - 1);
	}

	public final synchronized int getScrollbarThumbPosition(int viewPage) {
		if ((myModel == null) || (myModel.getParagraphsNumber() == 0)) {
			return 0;
		}
		if (scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS) {
			return 0;
		}
		ZLTextPage page = getPage(viewPage);
		preparePaintInfo(page);
		return Math.max(0, sizeOfTextBeforeCursor(page.StartCursor));
	}

	public final synchronized int getScrollbarThumbLength(int viewPage) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 0;
		}
		ZLTextPage page = getPage(viewPage);
		preparePaintInfo(page);
		int start = (scrollbarType() == SCROLLBAR_SHOW_AS_PROGRESS) ? 0 : sizeOfTextBeforeCursor(page.StartCursor);
		if (start == -1) {
			start = 0;
		}
		int end = sizeOfTextBeforeCursor(page.EndCursor);
		if (end == -1) {
			end = myModel.getTextLength(myModel.getParagraphsNumber() - 1) - 1;
		}
		return Math.max(1, end - start);
	}

	private int sizeOfTextBeforeCursor(ZLTextWordCursor wordCursor) {
		final ZLTextWordCursor cursor = new ZLTextWordCursor(wordCursor);
		if (cursor.isEndOfParagraph() && !cursor.nextParagraph()) {
			return -1;
		}
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		if (paragraphCursor == null) {
			return -1;
		}
		final int paragraphIndex = paragraphCursor.Index;
		int sizeOfText = myModel.getTextLength(paragraphIndex - 1);
		final int paragraphLength = paragraphCursor.getParagraphLength();
		if (paragraphLength > 0) {
			sizeOfText +=
				(myModel.getTextLength(paragraphIndex) - sizeOfText)
				* cursor.getElementIndex()
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
		final float charsPerParagraph = ((float) totalTextSize) / num;

		final float charWidth = computeCharWidth();

		final int indentWidth = getElementWidth(ZLTextElement.IndentElement, 0);
		final float effectiveWidth = textWidth - (indentWidth + 0.5f * textWidth) / charsPerParagraph;
		float charsPerLine = Math.min(effectiveWidth / charWidth,
				charsPerParagraph * 1.2f);

		final int strHeight = getWordHeight() + Context.getDescent();
		final int effectiveHeight = (int) (textHeight - (getTextStyle().getSpaceBefore() 
				+ getTextStyle().getSpaceAfter()) / charsPerParagraph);
		final int linesPerPage = effectiveHeight / strHeight;

		/*System.err.println("PAGE: textWidth = " + textWidth);
		System.err.println("PAGE: textHeight = " + textHeight);
		System.err.println("PAGE: indentWidth = " + indentWidth);
		System.err.println("PAGE: strHeight = " + strHeight);
		System.err.println("PAGE: lang = " + getLanguage());
		System.err.println("PAGE: charWidth = " + charWidth);
		System.err.println("PAGE: effectiveWidth = " + effectiveWidth);
		System.err.println("PAGE: effectiveHeight = " + effectiveHeight);
		System.err.println("PAGE: linesPerPage = " + linesPerPage);
		System.err.println("PAGE: charsPerParagraph = " + charsPerParagraph + " (factor = " + 1.0f / charsPerParagraph + ")");
		System.err.println("PAGE: charsPerLine = " + charsPerLine);*/

		return charsPerLine * linesPerPage;
	}

	private synchronized int computeTextPageNumber(int textSize) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return 1;
		}

//		System.err.println(">------------------------------------------->");

		final float factor = 1.0f / computeCharsPerPage();
		final float pages = textSize * factor;
		int result = Math.max((int) (pages + 1.0f - 0.5f * factor), 1);

		/*System.err.println("PAGE: textSize = " + textSize);
		System.err.println("PAGE: factor = " + factor);
		System.err.println("PAGE: pages = " + pages);
		System.err.println("PAGE: result = " + result);
		System.err.println("<-------------------------------------------<");*/
		return result;
	}

	private static final char[] ourDefaultLetters = "System developers have used modeling languages for decades to specify, visualize, construct, and document systems. The Unified Modeling Language (UML) is one of those languages. UML makes it possible for team members to collaborate by providing a common language that applies to a multitude of different systems. Essentially, it enables you to communicate solutions in a consistent, tool-supported language.".toCharArray();

	private final char[] myLettersBuffer = new char[512];
	private int myLettersBufferLength = 0;
	private ZLTextModel myLettersModel = null;

	private final float computeCharWidth() {
		if (myLettersModel != myModel) {
			myLettersModel = myModel;
			myLettersBufferLength = 0;

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

		final float charWidth = computeCharWidth(myLettersBuffer, myLettersBufferLength);
		System.err.println("CHARWIDTH: charWidth = " + charWidth);
		return charWidth;
	}

	private final float computeCharWidth(char[] pattern, int length) {
		return Context.getStringWidth(pattern, 0, length) / ((float) length);
	}

	public final synchronized int computePageNumber() {
		return computeTextPageNumber(getScrollbarFullSize());
	}

	public final synchronized int computeCurrentPage() {
		return computeTextPageNumber(
			getScrollbarThumbPosition(PAGE_CENTRAL) 
			+ getScrollbarThumbLength(PAGE_CENTRAL)
		);
	}

	public final synchronized void gotoPage(int page) {
		if (myModel == null || myModel.getParagraphsNumber() == 0) {
			return;
		}

		final float factor = computeCharsPerPage();
		final float textSize = page * factor;

		/*System.err.println(">------------------------------------------->");
		System.err.println("SETPAGE: page = " + page);
		System.err.println("SETPAGE: factor = " + factor);
		System.err.println("SETPAGE: textSize = " + textSize);*/

		int intTextSize = (int) textSize;
		int paragraphIndex = myModel.findParagraphByTextLength(intTextSize);

		if (paragraphIndex > 0 && myModel.getTextLength(paragraphIndex) > intTextSize) {
			/*System.err.println("SETPAGE: drop overlength paragraph: " + paragraphIndex + " (" + myModel.getTextLength(paragraphIndex) + ")");*/
			--paragraphIndex;
		}
		intTextSize = myModel.getTextLength(paragraphIndex);

		int sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		while (paragraphIndex > 0 && intTextSize == sizeOfTextBefore) {
			/*System.err.println("SETPAGE: drop empty paragraph: " + paragraphIndex);*/
			--paragraphIndex;
			intTextSize = sizeOfTextBefore;
			sizeOfTextBefore = myModel.getTextLength(paragraphIndex - 1);
		}

		final int paragraphLength = intTextSize - sizeOfTextBefore;

		/*System.err.println("SETPAGE: paragraphIndex = " + paragraphIndex);
		System.err.println("SETPAGE: intTextSize = " + intTextSize);
		System.err.println("SETPAGE: sizeOfTextBefore = " + sizeOfTextBefore);
		System.err.println("SETPAGE: paragraphLength = " + paragraphLength);*/

		final int wordIndex;
		if (paragraphLength == 0) {
			wordIndex = 0;
		} else {
			preparePaintInfo(myCurrentPage);
			final ZLTextWordCursor cursor = new ZLTextWordCursor(myCurrentPage.EndCursor);
			cursor.moveToParagraph(paragraphIndex);
			wordIndex = cursor.getParagraphCursor().getParagraphLength();
		}

		/*System.err.println("SETPAGE: wordIndex = " + wordIndex);
		System.err.println("<-------------------------------------------<");*/

		gotoPositionByEnd(paragraphIndex, wordIndex, 0);
	}

	private static final char[] SPACE = new char[] { ' ' };
	private void drawTextLine(ZLTextPage page, ZLTextLineInfo info, int from, int to, int y) {
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		final ZLPaintContext context = Context;

		if ((page == myCurrentPage) && !mySelectionModel.isEmpty() && (from != to)) {
			final int paragraphIndex = paragraph.Index;
			final ZLTextSelectionModel.Range range = mySelectionModel.getRange();
			final ZLTextSelectionModel.BoundElement lBound = range.Left;
			final ZLTextSelectionModel.BoundElement rBound = range.Right;

			int left = getRightLine();
			if (paragraphIndex > lBound.ParagraphIndex) {
				left = getLeftMargin();
			} else if (paragraphIndex == lBound.ParagraphIndex) {
				final int boundElementIndex = lBound.ElementIndex;
				if (info.StartElementIndex > boundElementIndex) {
					left = getLeftMargin();
				} else if ((info.EndElementIndex > boundElementIndex) ||
									 ((info.EndElementIndex == boundElementIndex) &&
										(info.EndCharIndex >= lBound.CharIndex))) {
					final ZLTextElementArea elementArea = page.findLast(from, to, lBound);
					left = elementArea.XStart;
					if (elementArea.Element instanceof ZLTextWord) {
						left += getAreaLength(paragraph, elementArea, lBound.CharIndex);
					}
				}
			}

			final int top = y + 1;
			int bottom = y + info.Height + info.Descent;
			int right = getLeftMargin();
			if (paragraphIndex < rBound.ParagraphIndex) {
				right = getRightLine();
				bottom += info.VSpaceAfter;
			} else if (paragraphIndex == rBound.ParagraphIndex) {
				final int boundElementIndex = rBound.ElementIndex;
				if ((info.EndElementIndex < boundElementIndex) ||
						((info.EndElementIndex == boundElementIndex) &&
						 (info.EndCharIndex < rBound.CharIndex))) {
					right = getRightLine();
					bottom += info.VSpaceAfter;
				} else if ((info.StartElementIndex < boundElementIndex) ||
									 ((info.StartElementIndex == boundElementIndex) &&
										(info.StartCharIndex <= rBound.CharIndex))) {
					final ZLTextElementArea elementArea = page.findLast(from, to, rBound);
					if (elementArea.Element instanceof ZLTextWord) {
						right = elementArea.XStart + getAreaLength(paragraph, elementArea, rBound.CharIndex) - 1;
					} else {
						right = elementArea.XEnd;
					}
				}
			}

			if (left < right) {
				context.setFillColor(getSelectedBackgroundColor());
				context.fillRectangle(left, top, right, bottom);
			}
		}

		int index = from;
		final int endElementIndex = info.EndElementIndex;
		int charIndex = info.RealStartCharIndex;
		for (int wordIndex = info.RealStartElementIndex; (wordIndex != endElementIndex) && (index < to); ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final ZLTextElementArea area = page.TextElementMap.get(index);
			//if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
			if (element == area.Element) {
				index++;
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int areaX = area.XStart;
				final int areaY = area.YEnd - getElementDescent(element) - getTextStyle().getVerticalShift();
				if (element instanceof ZLTextWord) {
					drawWord(areaX, areaY, (ZLTextWord)element, charIndex, -1, false);
				} else if (element instanceof ZLTextImageElement) {
					context.drawImage(areaX, areaY, ((ZLTextImageElement)element).ImageData);
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
			int len = info.EndCharIndex;
			final ZLTextWord word = (ZLTextWord)paragraph.getElement(info.EndElementIndex);
			drawWord(
				area.XStart, area.YEnd - context.getDescent() - getTextStyle().getVerticalShift(),
				word, 0, len, area.AddHyphenationSign
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
			applyControls(paragraphCursor, 0, wordIndex);	
			ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, wordIndex, result.getCharIndex(), getTextStyle());
			final int endIndex = info.ParagraphCursorLength;
			while (info.EndElementIndex != endIndex) {
				info = processTextLine(paragraphCursor, info.EndElementIndex, info.EndCharIndex, endIndex);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
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

	private ZLTextLineInfo processTextLine(ZLTextParagraphCursor paragraphCursor, 
		final int startIndex, final int startCharIndex, final int endIndex) {
		final ZLPaintContext context = Context;
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharIndex, getTextStyle());
		final ZLTextLineInfo cachedInfo = myLineInfoCache.get(info);
		if (cachedInfo != null) {
			applyControls(paragraphCursor, startIndex, cachedInfo.EndElementIndex);
			return cachedInfo;
		}

		int currentElementIndex = startIndex;
		int currentCharIndex = startCharIndex;
		final boolean isFirstLine = (startIndex == 0) && (startCharIndex == 0);

		if (isFirstLine) {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			while (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
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
			{
				final int eltHeight = getElementHeight(element);
				if (newHeight < eltHeight) {
					newHeight = eltHeight;
				}
			}
			{
				final int eltDescent = getElementDescent(element);
				if (newDescent < eltDescent) {
					newDescent = eltDescent;
				}
			}
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
			} else if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
			} else if (element instanceof ZLTextImageElement) {
				wordOccurred = true;
				isVisible = true;
			}			
			if ((newWidth > maxWidth) && (info.EndElementIndex != startIndex)) {
				break;
			}
			ZLTextElement previousElement = element;
			++currentElementIndex;
			currentCharIndex = 0;
			boolean allowBreak = currentElementIndex == endIndex;
			if (!allowBreak) {
				element = paragraphCursor.getElement(currentElementIndex); 
				allowBreak = (((!(element instanceof ZLTextWord)) || (previousElement instanceof ZLTextWord)) && 
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

		if ((currentElementIndex != endIndex) 
			&& (ZLTextStyleCollection.Instance().getBaseStyle().AutoHyphenationOption.getValue()) 
			&& (getTextStyle().allowHyphenations())) {
			ZLTextElement element = paragraphCursor.getElement(currentElementIndex);
			if (element instanceof ZLTextWord) { 
				final ZLTextWord word = (ZLTextWord)element;
				newWidth -= getWordWidth(word, currentCharIndex);
				int spaceLeft = maxWidth - newWidth;
				if ((word.Length > 3) && (spaceLeft > 2 * Context.getSpaceWidth())) {
					ZLTextHyphenationInfo hyphenationInfo = ZLTextHyphenator.Instance().getInfo(word);
					int hyphenationPosition = word.Length - 1;
					int subwordWidth = 0;
					for(; hyphenationPosition > 0; hyphenationPosition--) {
						if (hyphenationInfo.isHyphenationPossible(hyphenationPosition)) {
							subwordWidth = getWordWidth(word, 0, hyphenationPosition, 
								word.Data[word.Offset + hyphenationPosition - 1] != '-');
							if (subwordWidth <= spaceLeft) {
								break;
							}
						}
					}
					if (hyphenationPosition > 0) {
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

		if ((info.EndElementIndex != endIndex) || (endIndex == info.ParagraphCursorLength)) {
			myLineInfoCache.put(info, info);
		}

		return info;	
	}

	private void prepareTextLine(ZLTextPage page, ZLTextLineInfo info, int y) {
		y = Math.min(y + info.Height, getBottomLine());

		final ZLPaintContext context = Context;
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
							0, false, false, getTextStyle(), element, x, x + spaceLength, y, y
						);
					} else {
						spaceElement = null;
					}
					x += spaceLength;
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}	
			} else if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				final int height = getElementHeight(element);
				final int descent = getElementDescent(element);
				final int length = (element instanceof ZLTextWord) ? ((ZLTextWord)element).Length : 0;
				if (spaceElement != null) {
					page.TextElementMap.add(spaceElement);
					spaceElement = null;
				}
				page.TextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, charIndex, 
					length - charIndex, false, changeStyle, getTextStyle(), element, x, x + width - 1, y - height + 1, y + descent));
				changeStyle = false;
				wordOccurred = true;
			} else if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
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
						len, addHyphenationSign,
						changeStyle, getTextStyle(), word,
						x, x + width - 1, y - height + 1, y + descent
					)
				);
			}	
		}
	}
	
	public synchronized final void scrollPage(boolean forward, int scrollingMode, int value) {
		if (isScrollingActive()) {
			return;
		}

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
		if ((newWidth != page.OldWidth) || (newHeight != page.OldHeight)) {
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

		if ((page.PaintState == PaintStateEnum.NOTHING_TO_PAINT) || (page.PaintState == PaintStateEnum.READY)) {
			return;
		}

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
						if (!page.isEmptyPage() && ((myScrollingMode != ScrollingMode.KEEP_LINES) || (!endCursor.samePositionAs(page.EndCursor)))) {
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
			myPreviousPage.reset();
			myNextPage.reset();
		}
	}

	public void clearCaches() {
		rebuildPaintInfo();
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
		applyControls(paragraphCursor, 0, cursor.getElementIndex());

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

	/*
	protected List<ZLTextElementArea> allElements() {
		return myCurrentPage.TextElementMap;
	}
	*/

	protected ZLTextElementArea getElementByCoordinates(int x, int y) {
		return myCurrentPage.TextElementMap.binarySearch(x, y);
	}

	/*private static int lowerBound(int[] array, int value) {
		int leftIndex = 0;
		int rightIndex = array.length - 1;
		if (array[rightIndex] <= value) {
			return rightIndex;
		}
		while (leftIndex < rightIndex - 1) {
			int middleIndex = (leftIndex + rightIndex) / 2;
			if (array[middleIndex] <= value) {
				leftIndex = middleIndex;
			} else {
				rightIndex = middleIndex;
			}
		}
		return leftIndex;
	}*/

	@Override
	public boolean onStylusMovePressed(int x, int y) {
		if (mySelectionModel.extendTo(x, y)) {
			ZLApplication.Instance().repaintView();
			return true;
		}
		return false;
	}

	@Override
	public boolean onStylusRelease(int x, int y) {
		mySelectionModel.deactivate();
		return false;
	}

	protected abstract boolean isSelectionEnabled();

	protected void activateSelection(int x, int y) {
		if (isSelectionEnabled()) {
			mySelectionModel.activate(x, y);
			ZLApplication.Instance().repaintView();
		}
	}

	private ZLTextHyperlinkArea myCurrentHyperlink;

	private ZLTextHyperlinkArea getCurrentHyperlinkArea(ZLTextPage page) {
		final ArrayList<ZLTextHyperlinkArea> hyperlinkAreas = page.TextElementMap.HyperlinkAreas;
		final int index = hyperlinkAreas.indexOf(myCurrentHyperlink);
		if (index == -1) {
			return null;
		}
		return hyperlinkAreas.get(index);
	}

	public ZLTextHyperlink getCurrentHyperlink() {
		final ZLTextHyperlinkArea area = getCurrentHyperlinkArea(myCurrentPage);
		return (area != null) ? area.Hyperlink : null;
	}

	protected ZLTextHyperlink findHyperlink(int x, int y, int maxDistance) {
		ZLTextHyperlinkArea area = null;
		int distance = Integer.MAX_VALUE;
		for (ZLTextHyperlinkArea a : myCurrentPage.TextElementMap.HyperlinkAreas) {
			final int d = a.distanceTo(x, y);
			if ((d < distance) && (d <= maxDistance)) {
				area = a;
				distance = d;
			}
		}
		return (area != null) ? area.Hyperlink : null;
	}

	protected void selectHyperlink(ZLTextHyperlink hyperlink) {
		for (ZLTextHyperlinkArea area : myCurrentPage.TextElementMap.HyperlinkAreas) {
			if (area.Hyperlink == hyperlink) {
				myCurrentHyperlink = area;
				break;
			}
		}
	}

	protected boolean moveHyperlinkPointer(boolean forward) {
		final ArrayList<ZLTextHyperlinkArea> hyperlinkAreas = myCurrentPage.TextElementMap.HyperlinkAreas;
		if (!hyperlinkAreas.isEmpty()) {
			final int index = hyperlinkAreas.indexOf(myCurrentHyperlink);
			if (index == -1) {
				myCurrentHyperlink = hyperlinkAreas.get(forward ? 0 : hyperlinkAreas.size() - 1);
				return true;
			} else {
				if (forward) {
					if (index + 1 < hyperlinkAreas.size()) {
						myCurrentHyperlink = hyperlinkAreas.get(index + 1);
						return true;
					}
				} else {
					if (index > 0) {
						myCurrentHyperlink = hyperlinkAreas.get(index - 1);
						return true;
					}
				}
			}
		}
		return false;
	}
}
