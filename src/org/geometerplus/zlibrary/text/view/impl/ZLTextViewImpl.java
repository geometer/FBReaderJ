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

package org.geometerplus.zlibrary.text.view.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.hyphenation.*;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.style.*;

public abstract class ZLTextViewImpl extends ZLTextView {
	private ZLTextModel myModel;
	protected int myCurrentModelIndex;
	private ArrayList<ZLTextModel> myModelList;
	private final ZLTextSelectionModel mySelectionModel;

	protected static class Position {
		public int ParagraphIndex;
		public int WordIndex;
		public int CharIndex;
		public int ModelIndex;

		public Position(int modelIndex, int paragraphIndex, int wordIndex, int charIndex) {
			ParagraphIndex = paragraphIndex;
			WordIndex = wordIndex;
			CharIndex = charIndex;
			ModelIndex = modelIndex;
		}

		public Position(int modelIndex, ZLTextWordCursor cursor) {
			ModelIndex = modelIndex;
			set(cursor);
		}

		public void set(ZLTextWordCursor cursor) {
			if (!cursor.isNull()) {
				ParagraphIndex = cursor.getParagraphCursor().Index;
				WordIndex = cursor.getWordIndex();
				CharIndex = cursor.getCharIndex();
			}
		}

		public boolean equalsToCursor(ZLTextWordCursor cursor) {
			return
				(ParagraphIndex == cursor.getParagraphCursor().Index) &&
				(WordIndex == cursor.getWordIndex()) &&
				(CharIndex == cursor.getCharIndex());
		} 
	}

	private interface SizeUnit {
		int PIXEL_UNIT = 0;
		int LINE_UNIT = 1;
	};
	private int myScrollingMode;
	private int myOverlappingValue;

	private ZLTextPage myPreviousPage = new ZLTextPage();
	private ZLTextPage myCurrentPage = new ZLTextPage();
	private ZLTextPage myNextPage = new ZLTextPage();

	private final HashMap<ZLTextLineInfo,ZLTextLineInfo> myLineInfoCache = new HashMap<ZLTextLineInfo,ZLTextLineInfo>();

	private int[] myTextSize;

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;

	final ZLTextElementAreaVector myTextElementMap = new ZLTextElementAreaVector();

	public ZLTextViewImpl(ZLPaintContext context) {
		super(context);
		resetTextStyle();
 		mySelectionModel = new ZLTextSelectionModel(this);
	}

	public void setModels(ArrayList<ZLTextModel> models, int current) {
		myModelList = (models != null) ? models : new ArrayList<ZLTextModel>();
		myModel = (current >= 0 && current < myModelList.size()) ?
			(ZLTextModel)myModelList.get(current) : null;
		myCurrentModelIndex = current;
		setModelInternal();
	}

	private void setModelInternal() {
		ZLTextParagraphCursorCache.clear();

		if (myModel != null) {
			final int paragraphsNumber = myModel.getParagraphsNumber();
			if (paragraphsNumber > 0) {
				myTextSize = new int[paragraphsNumber + 1];
				myTextSize[0] = 0;
				for (int i = 0; i < paragraphsNumber; ++i) {
					myTextSize[i + 1] = myTextSize[i] + myModel.getParagraphTextLength(i);
				}
				myCurrentPage.moveStartCursor(ZLTextParagraphCursor.cursor(myModel, 0));
				myPreviousPage.reset();
				myNextPage.reset();
			}
		}
	}
	
	public void setModelIndex(int modelIndex) {
		if ((modelIndex != myCurrentModelIndex) && (modelIndex >= 0) &&
				(modelIndex < myModelList.size())) {
			myModel = (ZLTextModel)myModelList.get(modelIndex);
			myCurrentModelIndex = modelIndex;
			setModelInternal();
		}
	}
	
	//TODO: visibility
	public ZLTextModel getModel() {
		return myModel;
	}
	
	protected ArrayList<ZLTextModel> getModelList() {
		return myModelList;
	}
	
	public void highlightParagraph(int paragraphIndex) {
		myModel.selectParagraph(paragraphIndex);
		rebuildPaintInfo();
	}

	void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		Context.setFont(style.getFontFamily(), style.getFontSize(), style.isBold(), style.isItalic());
	}

	private void resetTextStyle() {
		setTextStyle(ZLTextStyleCollection.getInstance().getBaseStyle());
	}

	private void applyControl(ZLTextControlElement control) {
		final ZLTextStyle textStyle = myTextStyle;
		if (control.IsStart) {
			ZLTextStyleDecoration decoration = ZLTextStyleCollection.getInstance().getDecoration(control.Kind);
			setTextStyle(decoration.createDecoratedStyle(textStyle));
		} else {
			setTextStyle(textStyle.getBase());
		}
	}

	private void applyControls(ZLTextParagraphCursor cursor, int index, int end) {
		for (; index != end; ++index) {
			final ZLTextElement element = cursor.getElement(index);
			if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
			}	
		}
	}

	private int getElementWidth(ZLPaintContext context, ZLTextElement element, int charIndex) {
		if (element instanceof ZLTextWord) {
			return getWordWidth(context, (ZLTextWord)element, charIndex);
		} else if (element instanceof ZLTextImageElement) {
			return context.imageWidth(((ZLTextImageElement)element).ImageData);
		} else if (element == ZLTextElement.IndentElement) {
			return myTextStyle.getFirstLineIndentDelta();
		} else if (element instanceof ZLTextFixedHSpaceElement) {
			return context.getSpaceWidth() * ((ZLTextFixedHSpaceElement)element).Length;
		}
		return 0; 
	}

	private int getElementHeight(ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			int wordHeight = myWordHeight;
			if (wordHeight == -1) {
				final ZLTextStyle textStyle = myTextStyle;
				wordHeight = (int)(Context.getStringHeight() * textStyle.getLineSpacePercent() / 100) + textStyle.getVerticalShift();
				myWordHeight = wordHeight;
			}
			return wordHeight;
		} else if (element instanceof ZLTextImageElement) {
			final ZLPaintContext context = Context;
			return context.imageHeight(((ZLTextImageElement)element).ImageData) + 
				Math.max(context.getStringHeight() * (myTextStyle.getLineSpacePercent() - 100) / 100, 3);
		}
		return 0;
	}
	
	private static int getElementDescent(ZLPaintContext context, ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			return context.getDescent();
		}
		return 0;
	}

	private int getTextAreaHeight() {
		return Context.getHeight() - getTopMargin() - getBottomMargin();
	}

	private int getBottomLine() {
		return Context.getHeight() - getBottomMargin();
	}

	static int getWordWidth(ZLPaintContext context, ZLTextWord word, int start) {
		return
			(start == 0) ?
				word.getWidth(context) :
				context.getStringWidth(word.Data, word.Offset + start, word.Length - start);
	}
	
	static int getWordWidth(ZLPaintContext context, ZLTextWord word, int start, int length) {
		return context.getStringWidth(word.Data, word.Offset + start, length);
	}
	
	private char[] myWordPartArray = new char[20];

	private int getWordWidth(ZLPaintContext context, ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if (length == -1) {
			if (start == 0) {
				return word.getWidth(context);
			}	
			length = word.Length - start;
		}
		if (!addHyphenationSign) {
			return context.getStringWidth(word.Data, word.Offset + start, length);
		}
		char[] part = myWordPartArray;
		if (length + 1 > part.length) {
			part = new char[length + 1];
			myWordPartArray = part;
		}
		System.arraycopy(word.Data, word.Offset + start, part, 0, length);
		part[length] = '-';
		return context.getStringWidth(part, 0, length + 1);
	}
	
	private void moveStartCursor(int paragraphIndex) {
		moveStartCursor(paragraphIndex, 0, 0);
	}
		
	private void moveStartCursor(int paragraphIndex, int wordIndex, int charIndex) {
		myCurrentPage.moveStartCursor(paragraphIndex, wordIndex, charIndex);
		myPreviousPage.reset();
		myNextPage.reset();
	}

	private void moveEndCursor(int paragraphIndex) {
		moveEndCursor(paragraphIndex, 0, 0);
	}

	private void moveEndCursor(int paragraphIndex, int wordIndex, int charIndex) {
		myCurrentPage.moveEndCursor(paragraphIndex, wordIndex, charIndex);
		myPreviousPage.reset();
		myNextPage.reset();
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

	public void gotoMark(ZLTextMark mark) {
		myPreviousPage.reset();
		myNextPage.reset();
		if (mark.ParagraphIndex < 0) {
			return;
		}
		boolean doRepaint = false;
		if (myCurrentPage.StartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.StartCursor.isNull()) {
			return;
		}
		final Position position = new Position(myCurrentModelIndex, myCurrentPage.StartCursor);
		if ((myCurrentPage.StartCursor.getParagraphCursor().Index != mark.ParagraphIndex) || (myCurrentPage.StartCursor.getPosition().compareTo(mark) > 0)) {
			doRepaint = true;
			gotoParagraph(mark.ParagraphIndex, false);
			preparePaintInfo(myCurrentPage);
		}
		if (myCurrentPage.EndCursor.isNull()) {
			preparePaintInfo(myCurrentPage);
		}
		while (mark.compareTo(myCurrentPage.EndCursor.getPosition()) > 0) { 
			doRepaint = true;
			scrollPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo(myCurrentPage);
		}
		if (doRepaint) {
			if (myCurrentPage.StartCursor.isNull()) {
				preparePaintInfo(myCurrentPage);
			}
	/*		if (!position.equalsToCursor(myCurrentPage.StartCursor)) {
				savePosition(position);
			}
		*/	
			savePosition(position, myCurrentModelIndex, myCurrentPage.StartCursor);
			ZLApplication.Instance().refreshWindow();
		}
	}

	protected void savePosition(Position position) {
	}
	
	protected final void savePosition(Position position, int modelIndexToCheck, ZLTextWordCursor cursorToCheck) {
//		System.out.println("trying to save " + position.ModelIndex + " " + position.ParagraphIndex);
		if ((position.ModelIndex != modelIndexToCheck) || !position.equalsToCursor(cursorToCheck)) {
			savePosition(position);
	//		System.out.println("saved");
		}
	}
	
	public void search(final String text, boolean ignoreCase, boolean wholeText, boolean backward, boolean thisSectionOnly) {
		if (text.length() == 0) {
			return;
		}
		int startIndex = 0;
		int endIndex = myModel.getParagraphsNumber();
		if (thisSectionOnly) {
			//To be written
		}
		myModel.search(text, startIndex, endIndex, ignoreCase);
		myPreviousPage.reset();
		myNextPage.reset();
		if (!myCurrentPage.StartCursor.isNull()) {
			rebuildPaintInfo();
			ZLTextMark position = myCurrentPage.StartCursor.getPosition();
			gotoMark(wholeText ? 
				(backward ? myModel.getLastMark() : myModel.getFirstMark()) :
				(backward ? myModel.getPreviousMark(position) : myModel.getNextMark(position)));
			ZLApplication.Instance().refreshWindow();
		}
	}

	public boolean canFindNext() {
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		return !end.isNull() && (myModel != null) && (myModel.getNextMark(end.getPosition()).ParagraphIndex > -1);
	}

	public void findNext() {
		final ZLTextWordCursor end = myCurrentPage.EndCursor;
		if (!end.isNull()) {
			gotoMark(myModel.getNextMark(end.getPosition()));
		}
	}

	public boolean canFindPrevious() {
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		return !start.isNull() && (myModel != null) && (myModel.getPreviousMark(start.getPosition()).ParagraphIndex > -1);
	}

	public void findPrevious() {
		final ZLTextWordCursor start = myCurrentPage.StartCursor;
		if (!start.isNull()) {
			gotoMark(myModel.getPreviousMark(start.getPosition()));
		}
	}

	public synchronized void onScrollingFinished(int dx, int dy) {
		if ((dx < 0) || (dy < 0)) {
			ZLTextPage swap = myNextPage;
			myNextPage = myCurrentPage;
			myCurrentPage = myPreviousPage;
			myPreviousPage = swap;
			myPreviousPage.reset();
			if (myCurrentPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myNextPage);
				myCurrentPage.EndCursor.setCursor(myNextPage.StartCursor);
				myCurrentPage.PaintState = PaintStateEnum.START_IS_KNOWN;
			}
		} else if ((dx > 0) || (dy > 0)) {
			ZLTextPage swap = myPreviousPage;
			myPreviousPage = myCurrentPage;
			myCurrentPage = myNextPage;
			myNextPage = swap;
			myNextPage.reset();
			if (myCurrentPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myPreviousPage);
				myCurrentPage.StartCursor.setCursor(myPreviousPage.EndCursor);
				myCurrentPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			}
		}
	}

	public synchronized void paint(int dx, int dy) {
		System.err.println("paint " + dx + ' ' + dy);
		myTextElementMap.clear();

		final ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().getBaseStyle();
		final ZLPaintContext context = Context;
		context.clear(baseStyle.BackgroundColorOption.getValue());

		if ((myModel == null) || (myModel.getParagraphsNumber() == 0)) {
			return;
		}

		ZLTextPage page;
		if ((dx < 0) || (dy < 0)) {
			page = myPreviousPage;
			if (myPreviousPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myCurrentPage);
				myPreviousPage.EndCursor.setCursor(myCurrentPage.StartCursor);
				myPreviousPage.PaintState = PaintStateEnum.END_IS_KNOWN;
			}
		} else if ((dx > 0) || (dy > 0)) {
			page = myNextPage;
			if (myNextPage.PaintState == PaintStateEnum.NOTHING_TO_PAINT) {
				preparePaintInfo(myCurrentPage);
				myNextPage.StartCursor.setCursor(myCurrentPage.EndCursor);
				myNextPage.PaintState = PaintStateEnum.START_IS_KNOWN;
			}
		} else {
			page = myCurrentPage;
		}

		preparePaintInfo(page);

		if (page.StartCursor.isNull() || page.EndCursor.isNull()) {
			return;
		}

		{
			final int fullScrollBarSize = myTextSize[myTextSize.length - 1];
			final int scrollBarStart = sizeOfTextBeforeCursor(page.StartCursor);
			final int scrollBarEnd = sizeOfTextBeforeCursor(page.EndCursor);
			setVerticalScrollbarParameters(
				fullScrollBarSize,
				scrollBarStart,
				(scrollBarEnd != -1) ? scrollBarEnd : fullScrollBarSize
			);
		}

		final ArrayList<ZLTextLineInfo> lineInfos = page.LineInfos;
		final int[] labels = new int[lineInfos.size() + 1];
		context.moveYTo(getTopMargin());
		int index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			final int y = Math.min(context.getY() + info.Height, getBottomLine());
			prepareTextLine(info, y);
			context.moveY(info.Height + info.Descent + info.VSpaceAfter);
			labels[++index] = myTextElementMap.size();
		}

		mySelectionModel.update();

		context.moveYTo(getTopMargin());
		index = 0;
		for (ZLTextLineInfo info : lineInfos) {
			drawTextLine(context, info, labels[index], labels[index + 1]);
			++index;
		}
	}

	private int sizeOfTextBeforeCursor(ZLTextWordCursor wordCursor) {
		final ZLTextWordCursor cursor = new ZLTextWordCursor(wordCursor);
		if (cursor.isEndOfParagraph() && !cursor.nextParagraph()) {
			return -1;
		}
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		final int paragraphIndex = paragraphCursor.Index;
		int sizeOfText = myTextSize[paragraphIndex];
		final int paragraphLength = paragraphCursor.getParagraphLength();
		if (paragraphLength > 0) {
			sizeOfText +=
				(myTextSize[paragraphIndex + 1] - sizeOfText)
				* cursor.getWordIndex()
				/ paragraphLength;
		}
		return sizeOfText;
	}

	private ZLTextElementArea findLast(int from, int to, ZLTextSelectionModel.BoundElement bound) {
		final int boundElementIndex = bound.TextElementIndex;
		final int boundCharIndex = bound.CharIndex;
		final ZLTextElementAreaVector textAreas = myTextElementMap;
		ZLTextElementArea elementArea = textAreas.get(from);
		if ((elementArea.TextElementIndex < boundElementIndex) ||
				((elementArea.TextElementIndex == boundElementIndex) &&
				 (elementArea.StartCharIndex <= boundCharIndex))) {
			for (++from; from < to; ++from) {
				elementArea = textAreas.get(from);
				if ((elementArea.TextElementIndex > boundElementIndex) ||
						((elementArea.TextElementIndex == boundElementIndex) &&
						 (elementArea.StartCharIndex > boundCharIndex))) {
					return textAreas.get(from - 1);
				}
			}
		}
		return elementArea;
	}

	private int getAreaLength(ZLTextParagraphCursor paragraph, ZLTextElementArea area, int toCharIndex) {
		setTextStyle(area.Style);
		final ZLTextWord word = (ZLTextWord)paragraph.getElement(area.TextElementIndex);
		int length = toCharIndex - area.StartCharIndex;
		boolean selectHyphenationSign = false;
		if (length >= area.Length) {
			selectHyphenationSign = area.AddHyphenationSign;
			length = area.Length;
		}
		if (length > 0) {
			return getWordWidth(Context, word, area.StartCharIndex, length, selectHyphenationSign);
		}
		return 0;
	}

	private void drawTextLine(ZLPaintContext context, ZLTextLineInfo info, int from, int to) {
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;

		if (!mySelectionModel.isEmpty() && (from != to)) {
			final int paragraphIndex = paragraph.Index;
			final ZLTextSelectionModel.Range range = mySelectionModel.getRange();
			final ZLTextSelectionModel.BoundElement lBound = range.Left;
			final ZLTextSelectionModel.BoundElement rBound = range.Right;

			int left = getViewWidth() + getLeftMargin() - 1;
			if (paragraphIndex > lBound.ParagraphIndex) {
				left = getLeftMargin();
			} else if (paragraphIndex == lBound.ParagraphIndex) {
				final int boundElementIndex = lBound.TextElementIndex;
				if (info.StartWordIndex > boundElementIndex) {
					left = getLeftMargin();
				} else if ((info.EndWordIndex > boundElementIndex) ||
									 ((info.EndWordIndex == boundElementIndex) &&
										(info.EndCharIndex >= lBound.CharIndex))) {
					final ZLTextElementArea elementArea = findLast(from, to, lBound);
					left = elementArea.XStart;
					if (elementArea.Element instanceof ZLTextWord) {
						left += getAreaLength(paragraph, elementArea, lBound.CharIndex);
					}
				}
			}

			final int top = context.getY() + 1;
			int bottom = context.getY() + info.Height + info.Descent;
			int right = getLeftMargin();
			if (paragraphIndex < rBound.ParagraphIndex) {
				right = getViewWidth() + getLeftMargin() - 1;
				bottom += info.VSpaceAfter;
			} else if (paragraphIndex == rBound.ParagraphIndex) {
				final int boundElementIndex = rBound.TextElementIndex;
				if ((info.EndWordIndex < boundElementIndex) ||
						((info.EndWordIndex == boundElementIndex) &&
						 (info.EndCharIndex < rBound.CharIndex))) {
					right = getViewWidth() + getLeftMargin() - 1;
					bottom += info.VSpaceAfter;
				} else if ((info.StartWordIndex < boundElementIndex) ||
									 ((info.StartWordIndex == boundElementIndex) &&
										(info.StartCharIndex <= rBound.CharIndex))) {
					final ZLTextElementArea elementArea = findLast(from, to, rBound);
					if (elementArea.Element instanceof ZLTextWord) {
						right = elementArea.XStart + getAreaLength(paragraph, elementArea, rBound.CharIndex) - 1;
					} else {
						right = elementArea.XEnd;
					}
				}
			}

			if (left < right) {
				context.setFillColor(ZLTextStyleCollection.getInstance().getBaseStyle().SelectionBackgroundColorOption.getValue());
				context.fillRectangle(left, top, right, bottom);
			}
		}

		context.moveY(info.Height);
		int maxY = getBottomLine();
		if (context.getY() > maxY) {
			context.moveYTo(maxY);
		}

		int index = from;
		final int endWordIndex = info.EndWordIndex;
		int charIndex = info.RealStartCharIndex;
		for (int wordIndex = info.RealStartWordIndex; wordIndex != endWordIndex; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				final ZLTextElementArea area = myTextElementMap.get(index++);
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - getElementDescent(context, element) - myTextStyle.getVerticalShift();
				if (element instanceof ZLTextWord) {
					drawWord(x, y, (ZLTextWord)element, charIndex, -1, false);
				} else {
					context.drawImage(x, y, ((ZLTextImageElement)element).ImageData);
				}
			}
		}
		if (index != to) {
			ZLTextElementArea area = myTextElementMap.get(index++);
			if (area.ChangeStyle) {
				setTextStyle(area.Style);
			}
			int len = info.EndCharIndex;
			final ZLTextWord word = (ZLTextWord)paragraph.getElement(info.EndWordIndex);
			final int x = area.XStart;
			final int y = area.YEnd - context.getDescent() - myTextStyle.getVerticalShift();
			drawWord(x, y, word, 0, len, area.AddHyphenationSign);
		}
		context.moveY(info.Descent + info.VSpaceAfter);
	}

	private void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		final ZLPaintContext context = Context;
		context.setColor(myTextStyle.getColor());	
		if ((start == 0) && (length == -1)) {
			drawString(x, y, word.Data, word.Offset, word.Length, word.getMark(), 0);
		} else {
			if (length == -1) {
				length = word.Length - start;
			}
			if (!addHyphenationSign) {
				drawString(x, y, word.Data, word.Offset + start, length, word.getMark(), start);
			} else {
				char[] part = myWordPartArray;
				if (length + 1 > part.length) {
					part = new char[length + 1];
					myWordPartArray = part;
				}
				System.arraycopy(word.Data, word.Offset + start, part, 0, length);
				part[length] = '-';
				drawString(x, y, part, 0, length + 1, word.getMark(), start);	
			}	
		}
	}

	private void drawString(int x, int y, char[] str, int offset, int length, ZLTextWord.Mark mark, int shift) {
		final ZLPaintContext context = Context;
		context.setColor(myTextStyle.getColor());
		if (mark == null) {
			context.drawString(x, y, str, offset, length);
		} else {
			int pos = 0;
			for (; (mark != null) && (pos < length); mark = mark.getNext()) {
				int markStart = mark.Start - shift;
				int markLen = mark.Length;

				if (markStart < pos) {
					markLen += markStart - pos;
					markStart = pos;
				}

				if (markLen <= 0) {
					continue;
				}

				if (markStart > pos) {
					int endPos = Math.min(markStart, length);
					context.drawString(x, y, str, offset + pos, endPos - pos);
					x += context.getStringWidth(str, offset + pos, endPos - pos);
				}

				if (markStart < length) {
					context.setColor(ZLTextStyleCollection.getInstance().getBaseStyle().HighlightedTextColorOption.getValue());
					int endPos = Math.min(markStart + markLen, length);
					context.drawString(x, y, str, offset + markStart, endPos - markStart);
					x += context.getStringWidth(str, offset + markStart, endPos - markStart);
					context.setColor(myTextStyle.getColor());
				}
				pos = markStart + markLen;
			}

			if (pos < length) {
				context.drawString(x, y, str, offset + pos, length - pos);
			}
		}
	}

	private void buildInfos(ZLTextPage page, ZLTextWordCursor start, ZLTextWordCursor result) {
		result.setCursor(start);
		final ZLPaintContext context = Context;
		int textAreaHeight = getTextAreaHeight();
		page.LineInfos.clear();
		int counter = 0;
		do {
			resetTextStyle();
			final ZLTextParagraphCursor paragraphCursor = result.getParagraphCursor();
			final int wordIndex = result.getWordIndex();
			applyControls(paragraphCursor, 0, wordIndex);	
			ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, wordIndex, result.getCharIndex(), myTextStyle);
			final int endIndex = info.ParagraphCursorLength;
			while (info.EndWordIndex != endIndex) {
				info = processTextLine(context, paragraphCursor, info.EndWordIndex, info.EndCharIndex, endIndex);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				result.moveTo(info.EndWordIndex, info.EndCharIndex);
				page.LineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (result.isEndOfParagraph() && result.nextParagraph() && !result.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		resetTextStyle();
	}

	private ZLTextLineInfo processTextLine(final ZLPaintContext context, final ZLTextParagraphCursor paragraphCursor, 
		final int startIndex, final int startCharIndex, final int endIndex) {
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharIndex, myTextStyle);
		final ZLTextLineInfo cachedInfo = myLineInfoCache.get(info);
		if (cachedInfo != null) {
			applyControls(paragraphCursor, startIndex, cachedInfo.EndWordIndex);
			return cachedInfo;
		}

		int currentWordIndex = startIndex;
		int currentCharIndex = startCharIndex;
		final boolean isFirstLine = (startIndex == 0) && (startCharIndex == 0);

		if (isFirstLine) {
			ZLTextElement element = paragraphCursor.getElement(currentWordIndex);
			while (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
				++currentWordIndex;
				currentCharIndex = 0;
				if (currentWordIndex == endIndex) {
					break;
				}
				element = paragraphCursor.getElement(currentWordIndex);
			}
			info.StartStyle = myTextStyle;
			info.RealStartWordIndex = currentWordIndex;
			info.RealStartCharIndex = currentCharIndex;
		}	

		ZLTextStyle storedStyle = myTextStyle;		
		
		info.LeftIndent = myTextStyle.getLeftIndent();	
		if (isFirstLine) {
			info.LeftIndent += myTextStyle.getFirstLineIndentDelta();
		}	
		
		info.Width = info.LeftIndent;
		
		if (info.RealStartWordIndex == endIndex) {
			info.EndWordIndex = info.RealStartWordIndex;
			info.EndCharIndex = info.RealStartCharIndex;
			return info;
		}

		int newWidth = info.Width;
		int newHeight = info.Height;
		int newDescent = info.Descent;
		int maxWidth = context.getWidth() - getLeftMargin() - getRightMargin() - myTextStyle.getRightIndent();
		boolean wordOccurred = false;
		boolean isVisible = false;
		int lastSpaceWidth = 0;
		int internalSpaceCounter = 0;
		boolean removeLastSpace = false;

		do {
			ZLTextElement element = paragraphCursor.getElement(currentWordIndex); 
			newWidth += getElementWidth(context, element, currentCharIndex);
			{
				final int eltHeight = getElementHeight(element);
				if (newHeight < eltHeight) {
					newHeight = eltHeight;
				}
			}
			{
				final int eltDescent = getElementDescent(context, element);
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
			if ((newWidth > maxWidth) && (info.EndWordIndex != startIndex)) {
				break;
			}
			ZLTextElement previousElement = element;
			++currentWordIndex;
			currentCharIndex = 0;
			boolean allowBreak = currentWordIndex == endIndex;
			if (!allowBreak) {
				element = paragraphCursor.getElement(currentWordIndex); 
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
				info.EndWordIndex = currentWordIndex;
				info.EndCharIndex = currentCharIndex;
				info.SpaceCounter = internalSpaceCounter;
				storedStyle = myTextStyle;
				removeLastSpace = !wordOccurred && (internalSpaceCounter > 0);
			}	
		} while (currentWordIndex != endIndex);

		if ((currentWordIndex != endIndex) 
			&& (ZLTextStyleCollection.getInstance().getBaseStyle().AutoHyphenationOption.getValue()) 
			&& (myTextStyle.allowHyphenations())) {
			ZLTextElement element = paragraphCursor.getElement(currentWordIndex);
			if (element instanceof ZLTextWord) { 
				final ZLTextWord word = (ZLTextWord)element;
				newWidth -= getWordWidth(context, word, currentCharIndex);
				int spaceLeft = maxWidth - newWidth;
				if ((word.Length > 3) && (spaceLeft > 2 * Context.getSpaceWidth())) {
					ZLTextHyphenationInfo hyphenationInfo = ZLTextHyphenator.getInstance().getInfo(word);
					int hyphenationPosition = word.Length - 1;
					int subwordWidth = 0;
					for(; hyphenationPosition > 0; hyphenationPosition--) {
						if (hyphenationInfo.isHyphenationPossible(hyphenationPosition)) {
							subwordWidth = getWordWidth(context, word, 0, hyphenationPosition, 
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
						info.EndWordIndex = currentWordIndex;
						info.EndCharIndex = hyphenationPosition;
						info.SpaceCounter = internalSpaceCounter;
						storedStyle = myTextStyle;
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
			info.VSpaceAfter = myTextStyle.getSpaceAfter();
		}		

		if ((info.EndWordIndex != endIndex) || (endIndex == info.ParagraphCursorLength)) {
			myLineInfoCache.put(info, info);
		}

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info, int y) {
		final ZLPaintContext context = Context;
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;

		setTextStyle(info.StartStyle);
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		int x = getLeftMargin() + info.LeftIndent;
		final int maxWidth = context.getWidth() - getLeftMargin() - getRightMargin();
		switch (myTextStyle.getAlignment()) {
			case ZLTextAlignmentType.ALIGN_RIGHT: {
				x += maxWidth - myTextStyle.getRightIndent() - info.Width;
				break;
			} 
			case ZLTextAlignmentType.ALIGN_CENTER: {
				x += (maxWidth - myTextStyle.getRightIndent() - info.Width) / 2;
				break;
			} 
			case ZLTextAlignmentType.ALIGN_JUSTIFY: {
				if (!endOfParagraph && (paragraphCursor.getElement(info.EndWordIndex) != ZLTextElement.AfterParagraph)) {
					fullCorrection = maxWidth - myTextStyle.getRightIndent() - info.Width;
				}
				break;
			}
			case ZLTextAlignmentType.ALIGN_LEFT: 
			case ZLTextAlignmentType.ALIGN_UNDEFINED: {
				break;
			}
		}
	
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
		int paragraphIndex = paragraph.Index;
		final int endWordIndex = info.EndWordIndex;
		int charIndex = info.RealStartCharIndex;
		for (int wordIndex = info.RealStartWordIndex; wordIndex != endWordIndex; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			final int width = getElementWidth(context, element, charIndex);
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					int correction = fullCorrection / spaceCounter;
					x += context.getSpaceWidth() + correction;
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}	
			} else if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				final int height = getElementHeight(element);
				final int descent = getElementDescent(context, element);
				final int length = (element instanceof ZLTextWord) ? ((ZLTextWord)element).Length : 0;
				myTextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, charIndex, 
					length - charIndex, false, changeStyle, myTextStyle, element, x, x + width - 1, y - height + 1, y + descent));
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
				final int wordIndex = info.EndWordIndex;
				final ZLTextWord word = (ZLTextWord)paragraph.getElement(wordIndex);
				final boolean addHyphenationSign = word.Data[word.Offset + len - 1] != '-';
				final int width = getWordWidth(context, word, 0, len, addHyphenationSign);
				final int height = getElementHeight(word);
				final int descent = context.getDescent();
				myTextElementMap.add(
					new ZLTextElementArea(
						paragraphIndex, wordIndex, 0,
						len, addHyphenationSign,
						changeStyle, myTextStyle, word,
						x, x + width - 1, y - height + 1, y + descent
					)
				);
			}	
		}
	}
	
	public void scrollPage(boolean forward, int scrollingMode, int value) {
		preparePaintInfo(myCurrentPage);
		myPreviousPage.reset();
		myNextPage.reset();
		if (myCurrentPage.PaintState == PaintStateEnum.READY) {
			myCurrentPage.PaintState = forward ? PaintStateEnum.TO_SCROLL_FORWARD : PaintStateEnum.TO_SCROLL_BACKWARD;
			myScrollingMode = scrollingMode;
			myOverlappingValue = value;
		}
	}

	public final void gotoPosition(Position position) {
		gotoPosition(position.ModelIndex, position.ParagraphIndex, position.WordIndex, position.CharIndex);
	}

	public final void gotoPosition(int paragraphIndex, int wordIndex, int charIndex) {
		final int maxParagraphIndex = myModel.getParagraphsNumber() - 1;
		if (paragraphIndex > maxParagraphIndex) {
			paragraphIndex = maxParagraphIndex;
		}
		if (paragraphIndex < 0) {
			paragraphIndex = 0;
		}
		myCurrentPage.moveStartCursor(paragraphIndex, wordIndex, charIndex);
		myPreviousPage.reset();
		myNextPage.reset();
	}
	
	public final void gotoPosition(int modelIndex, int paragraphIndex, int wordIndex, int charIndex) {
		setModelIndex(modelIndex);
		gotoPosition(paragraphIndex, wordIndex, charIndex);
	}

	public void gotoParagraph(int num, boolean last) {
		if (myModel == null) {
			return;
		}

		if (last) {
			if ((num > 0) && (num <= (int)myModel.getParagraphsNumber())) {
				moveEndCursor(num);
			}
		} else {
			if ((num >= 0) && (num < (int)myModel.getParagraphsNumber())) {
				moveStartCursor(num);
			}
		}
	}

	private int getViewWidth() {
		return Math.max(Context.getWidth() - getLeftMargin() - getRightMargin(), 1);
	}

	protected synchronized void preparePaintInfo() {
		myPreviousPage.reset();
		myNextPage.reset();
		preparePaintInfo(myCurrentPage);
	}

	private synchronized void preparePaintInfo(ZLTextPage page) {
		int newWidth = getViewWidth();
		int newHeight = getTextAreaHeight();
		if ((newWidth != page.OldWidth) || (newHeight != page.OldHeight)) {
			page.OldWidth = newWidth;
			page.OldHeight = newHeight;
			if (page.PaintState != PaintStateEnum.NOTHING_TO_PAINT) {
				page.LineInfos.clear();
				if (!page.StartCursor.isNull()) {
					page.EndCursor.reset();
					page.PaintState = PaintStateEnum.START_IS_KNOWN;
				} else if (!page.EndCursor.isNull()) {
					page.StartCursor.reset();
					page.PaintState = PaintStateEnum.END_IS_KNOWN;
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
				
					if (!startCursor.isNull() && startCursor.equalsToCursor(page.StartCursor)) {
						page.findLineFromStart(startCursor, 1);
					}

					if (!startCursor.isNull()) {
						final ZLTextWordCursor endCursor = new ZLTextWordCursor();
						buildInfos(page, startCursor, endCursor);
						if (!page.isEmptyPage() && ((myScrollingMode != ScrollingMode.KEEP_LINES) || (!endCursor.equalsToCursor(page.EndCursor)))) {
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
							if (!endCursor.isNull() && endCursor.equalsToCursor(page.EndCursor)) {
								page.findLineFromEnd(endCursor, 1);
							}
							if (!endCursor.isNull()) {
								ZLTextWordCursor startCursor = findStart(endCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight());
								if (startCursor.equalsToCursor(page.StartCursor)) {
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
			onPreparePaintInfo();
		}
	}

	protected void onPreparePaintInfo() {
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
		final ZLPaintContext context = Context;
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		final int endWordIndex =
			beforeCurrentPosition ? cursor.getWordIndex() : paragraphCursor.getParagraphLength();
		
		resetTextStyle();

		int size = 0;

		int wordIndex = 0;
		int charIndex = 0;
		while (wordIndex != endWordIndex) {
			ZLTextLineInfo info = processTextLine(context, paragraphCursor, wordIndex, charIndex, endWordIndex);
			wordIndex = info.EndWordIndex;
			charIndex = info.EndCharIndex;
			size += infoSize(info, unit);
		}

		return size;
	}

	private void skip(ZLTextWordCursor cursor, int unit, int size) {
		final ZLPaintContext context = Context;
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		final int endWordIndex = paragraphCursor.getParagraphLength();

		resetTextStyle();
		applyControls(paragraphCursor, 0, cursor.getWordIndex());

		while (!cursor.isEndOfParagraph() && (size > 0)) {
			ZLTextLineInfo info = processTextLine(context, paragraphCursor, cursor.getWordIndex(), cursor.getCharIndex(), endWordIndex);
			cursor.moveTo(info.EndWordIndex, info.EndCharIndex);
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
			boolean sameStart = start.equalsToCursor(end);
			if (!sameStart && start.isEndOfParagraph() && end.isStartOfParagraph()) {
				ZLTextWordCursor startCopy = start;
				startCopy.nextParagraph();
				sameStart = startCopy.equalsToCursor(end);
			}
			if (sameStart) {
				start.setCursor(findStart(end, SizeUnit.LINE_UNIT, 1));
			}
		}

		return start;
	}

	protected ZLTextElementArea getElementByCoordinates(int x, int y) {
		return myTextElementMap.binarySearch(x, y);
	}

	protected int getParagraphIndexByCoordinate(int y) {
		ZLTextElementArea area = myTextElementMap.binarySearch(y);
		return (area != null) ? area.ParagraphIndex : -1;
	}

	private static int lowerBound(int[] array, int value) {
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
	}

	public boolean onStylusMovePressed(int x, int y) {
		if (mySelectionModel.extendTo(x, y)) {
			//copySelectedTextToClipboard(ZLDialogManager::CLIPBOARD_SELECTION);
			ZLApplication.Instance().refreshWindow();
			return true;
		}
		return false;
	}

	public boolean onStylusRelease(int x, int y) {
		mySelectionModel.deactivate();
		return false;
	}

	protected abstract boolean isSelectionEnabled();

	protected void activateSelection(int x, int y) {
		if (isSelectionEnabled()) {
			mySelectionModel.activate(x, y);
			ZLApplication.Instance().refreshWindow();
		}
	}

}
