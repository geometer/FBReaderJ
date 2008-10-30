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

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.hyphenation.*;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.style.*;

public abstract class ZLTextViewImpl extends ZLTextView {
	private ZLTextModel myModel;
	protected int myCurrentModelIndex;
	private ArrayList/*<ZLTextModel>*/ myModelList;
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
	private interface PaintState {
		int NOTHING_TO_PAINT = 0;
		int READY = 1;
		int START_IS_KNOWN = 2;
		int END_IS_KNOWN = 3;
		int TO_SCROLL_FORWARD = 4;
		int TO_SCROLL_BACKWARD = 5;
	};
	private int myPaintState = PaintState.NOTHING_TO_PAINT;
	private int myScrollingMode;
	private int myOverlappingValue;

	private int myOldWidth;
	private int myOldHeight;

	public final ZLTextWordCursor StartCursor = new ZLTextWordCursor();
	private final ZLTextWordCursor EndCursor = new ZLTextWordCursor();

	private final ZLTextLineInfoVector myLineInfos = new ZLTextLineInfoVector();
	private final ZLTextLineInfoCache myLineInfoCache = new ZLTextLineInfoCache();

	private int[] myTextSize;

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;

	private boolean myTreeStateIsFrozen = false;

	final ZLTextRectangularAreaVector myTextElementMap
		= new ZLTextRectangularAreaVector();
	private final ZLTextRectangularAreaVector myTreeNodeMap
		= new ZLTextRectangularAreaVector();

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		resetTextStyle();
 		mySelectionModel = new ZLTextSelectionModel(this, application);
	}

	public void setModels(ArrayList models, int current) {
		myModelList = (models != null) ? models : new ArrayList();
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
				StartCursor.setCursor(ZLTextParagraphCursor.cursor(myModel, 0));
				EndCursor.reset();
				myPaintState = PaintState.START_IS_KNOWN;
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
	
	protected ArrayList getModelList() {
		return myModelList;
	}
	
	public void highlightParagraph(int paragraphIndex) {
		myModel.selectParagraph(paragraphIndex);
		rebuildPaintInfo(true);
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
		return Context.getHeight() - getTopMargin() - getBottomMargin() - getIndicatorInfo().getFullHeight();
	}

	private int getBottomLine() {
		return Context.getHeight() - getBottomMargin() - getIndicatorInfo().getFullHeight();
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
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;
		}
		if (StartCursor.isNull()) {
			StartCursor.setCursor(EndCursor);
		}
		StartCursor.moveToParagraph(paragraphIndex);
		StartCursor.moveTo(wordIndex, charIndex);
		EndCursor.reset();
		myLineInfos.clear();
		myPaintState = PaintState.START_IS_KNOWN;
	}

	private void moveEndCursor(int paragraphIndex) {
		moveEndCursor(paragraphIndex, 0, 0);
	}

	private void moveEndCursor(int paragraphIndex, int wordIndex, int charIndex) {
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;	
		}
		if (EndCursor.isNull()) {
			EndCursor.setCursor(StartCursor);
		}
		EndCursor.moveToParagraph(paragraphIndex);
		if ((paragraphIndex > 0) && (wordIndex == 0) && (charIndex == 0)) {
			EndCursor.previousParagraph();
			EndCursor.moveToParagraphEnd();
		} else {
			EndCursor.moveTo(wordIndex, charIndex);
		}
		StartCursor.reset();
		myLineInfos.clear();
		myPaintState = PaintState.END_IS_KNOWN;
	}

	public void gotoMark(ZLTextMark mark) {
		if (mark.ParagraphIndex < 0) {
			return;
		}
		boolean doRepaint = false;
		if (StartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo();
		}
		if (StartCursor.isNull()) {
			return;
		}
		final Position position = new Position(myCurrentModelIndex, StartCursor);
		if ((StartCursor.getParagraphCursor().Index != mark.ParagraphIndex) || (StartCursor.getPosition().compareTo(mark) > 0)) {
			doRepaint = true;
			gotoParagraph(mark.ParagraphIndex, false);
			preparePaintInfo();
		}
		if (EndCursor.isNull()) {
			preparePaintInfo();
		}
		while (mark.compareTo(EndCursor.getPosition()) > 0) { 
			doRepaint = true;
			scrollPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo();
		}
		if (doRepaint) {
			if (StartCursor.isNull()) {
				preparePaintInfo();
			}
	/*		if (!position.equalsToCursor(StartCursor)) {
				savePosition(position);
			}
		*/	
			savePosition(position, myCurrentModelIndex, StartCursor);
			Application.refreshWindow();
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
		if (!StartCursor.isNull()) {
			rebuildPaintInfo(true);
			ZLTextMark position = StartCursor.getPosition();
			gotoMark(wholeText ? 
				(backward ? myModel.getLastMark() : myModel.getFirstMark()) :
				(backward ? myModel.getPreviousMark(position) : myModel.getNextMark(position)));
			Application.refreshWindow();
		}
	}

	public boolean canFindNext() {
		return !EndCursor.isNull() && (myModel != null) && (myModel.getNextMark(EndCursor.getPosition()).ParagraphIndex > -1);
	}

	public void findNext() {
		if (!EndCursor.isNull()) {
			gotoMark(myModel.getNextMark(EndCursor.getPosition()));
		}
	}

	public boolean canFindPrevious() {
		return !StartCursor.isNull() && (myModel != null) && (myModel.getPreviousMark(StartCursor.getPosition()).ParagraphIndex > -1);
	}

	public void findPrevious() {
		if (!StartCursor.isNull()) {
			gotoMark(myModel.getPreviousMark(StartCursor.getPosition()));
		}
	}

	public synchronized void paint() {
		//android.os.Debug.startMethodTracing("/tmp/paint");
		preparePaintInfo();

		myTextElementMap.clear();
		myTreeNodeMap.clear();

		final ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().getBaseStyle();
		final ZLPaintContext context = Context;
		context.clear(baseStyle.BackgroundColorOption.getValue());

		if ((myModel == null) || (myModel.getParagraphsNumber() == 0)) {
			return;
		}

		final ZLTextLineInfoVector lineInfos = myLineInfos;
		final int lineInfosSize = lineInfos.size();
		final int[] labels = new int[lineInfosSize + 1];
		context.moveYTo(getTopMargin());
		for (int i = 0; i < lineInfosSize; ) {
			prepareTextLine(lineInfos.getInfo(i));
			labels[++i] = myTextElementMap.size();
		}

		mySelectionModel.update();

		context.moveYTo(getTopMargin());
		for (int i = 0; i < lineInfosSize; ++i) {
			drawTextLine(context, lineInfos.getInfo(i), labels[i], labels[i + 1]);
		}
		//android.os.Debug.stopMethodTracing();

		ZLTextIndicatorInfo indicatorInfo = getIndicatorInfo();
		if (indicatorInfo.isVisible()) {
			final int yBottom = context.getHeight() - getBottomMargin() - 1;
			final int yTop = yBottom - indicatorInfo.getHeight() + 1;
			final int xLeft = getLeftMargin();
			final int xRight = context.getWidth() - getRightMargin() - 1;
			context.setColor(baseStyle.getColor());
			context.drawLine(xLeft, yBottom, xRight, yBottom);
			context.drawLine(xLeft, yTop, xRight, yTop);
			context.drawLine(xLeft, yBottom, xLeft, yTop);
			context.drawLine(xRight, yBottom, xRight, yTop);
			final long fullWidth = xRight - xLeft - 2;
			long width = fullWidth;

			final ZLTextWordCursor wordCursor = new ZLTextWordCursor(EndCursor);
			if (!wordCursor.isEndOfParagraph() || wordCursor.nextParagraph()) {
				final ZLTextParagraphCursor paragraphCursor = wordCursor.getParagraphCursor();
				final int paragraphIndex = paragraphCursor.Index;
				final int[] textSizeVector = myTextSize;
				int fullTextSize = textSizeVector[textSizeVector.length - 1];
				if (fullTextSize > 0) {
					int textSizeBeforeCursor = textSizeVector[paragraphIndex];
					final int paragraphLength = paragraphCursor.getParagraphLength();
					if (paragraphLength > 0) {
						textSizeBeforeCursor +=
							(textSizeVector[paragraphIndex + 1] - textSizeBeforeCursor)
							* wordCursor.getWordIndex()
							/ paragraphLength;
					}
					width = fullWidth * textSizeBeforeCursor / fullTextSize;
					if (width < 0) {
						width = 0;
					}
					if (width > fullWidth) {
						width = fullWidth;
					}
				}
			}
			context.setFillColor(indicatorInfo.getColor());
			context.fillRectangle(xLeft + 1, yTop + 1, xLeft + 1 + (int)width, yBottom - 1);
		}
	}

	private void drawTreeLines(ZLPaintContext context, ZLTextLineInfo.TreeNodeInfo info, int height, int vSpaceAfter) {
		context.setColor(ZLTextStyleCollection.getInstance().getBaseStyle().TreeLinesColorOption.getValue());

		int x = context.getX();
		int y = context.getY();

		final int qstep = (context.getStringHeight() + 2) / 3;

		final boolean[] stack = info.VerticalLinesStack;
		final int depth = stack.length;
		for (int i = depth - 1; i >= 0; --i) {
			if (stack[i]) {
				context.drawLine(x + 2 * qstep, y + vSpaceAfter, x + 2 * qstep, y - height + 1);
			}
			x += 4 * qstep;
		}

		if (info.IsFirstLine) {
			if ((depth > 0) && !stack[0]) {
				context.drawLine(x - 2 * qstep, y - qstep, x - 2 * qstep, y - height + 1);
			}

			if (info.IsLeaf) {
				if (depth > 0) {
					context.drawLine(x - 2 * qstep, y - qstep, x + 3 * qstep, y - qstep);
				}
			} else {
				int space = Math.max(qstep * 2 / 5, 2);
				if (depth > 0) {
					context.drawLine(x - 2 * qstep, y - qstep, x + qstep, y - qstep);
				}
				final int x0 = x + qstep;
				final int x1 = x + 3 * qstep;
				final int y0 = y;
				final int y1 = y - 2 * qstep;
				context.drawLine(x0, y0, x0, y1);
				context.drawLine(x1, y0, x1, y1);
				context.drawLine(x0, y0, x1, y0);
				context.drawLine(x0, y1, x1, y1);
				context.drawLine(x0 + space, y - qstep, x1 - space, y - qstep);
				if (info.IsOpen) {
					context.drawLine(x + 2 * qstep, y + vSpaceAfter, x + 2 * qstep, y);
				} else {
					context.drawLine(x + 2 * qstep, y0 - space, x + 2 * qstep, y1 + space);
				}
				myTreeNodeMap.add(
					new ZLTextTreeNodeArea(
						info.ParagraphIndex,
						x, x + 4 * qstep,
						y - height + 1, y
					)
				);
			}
		} else if (!info.IsLeaf && info.IsOpen) {
			context.drawLine(x + 2 * qstep, y + vSpaceAfter, x + 2 * qstep, y - height + 1);
		}
	}

	private ZLTextElementArea findLast(int from, int to, ZLTextSelectionModel.BoundElement bound) {
		final int boundElementIndex = bound.TextElementIndex;
		final int boundCharIndex = bound.CharIndex;
		final ZLTextRectangularAreaVector textAreas = myTextElementMap;
		ZLTextElementArea elementArea = (ZLTextElementArea)textAreas.get(from);
		if ((elementArea.TextElementIndex < boundElementIndex) ||
				((elementArea.TextElementIndex == boundElementIndex) &&
				 (elementArea.StartCharIndex <= boundCharIndex))) {
			for (++from; from < to; ++from) {
				elementArea = (ZLTextElementArea)textAreas.get(from);
				if ((elementArea.TextElementIndex > boundElementIndex) ||
						((elementArea.TextElementIndex == boundElementIndex) &&
						 (elementArea.StartCharIndex > boundCharIndex))) {
					return (ZLTextElementArea)textAreas.get(from - 1);
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

	private void drawTextLine(final ZLPaintContext context, final ZLTextLineInfo info, final int from, final int to) {
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
		context.moveXTo(getLeftMargin());	
		if (info.NodeInfo != null) {
			drawTreeLines(context, info.NodeInfo, info.Height, info.Descent + info.VSpaceAfter);
		}

		int index = from;
		final int endWordIndex = info.EndWordIndex;
		int charIndex = info.RealStartCharIndex;
		for (int wordIndex = info.RealStartWordIndex; wordIndex != endWordIndex; ++wordIndex, charIndex = 0) {
			final ZLTextElement element = paragraph.getElement(wordIndex);
			if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				final ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.get(index++);
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - getElementDescent(context, element) - myTextStyle.getVerticalShift();
				context.moveXTo(x);
				if (element instanceof ZLTextWord) {
					drawWord(x, y, (ZLTextWord)element, charIndex, -1, false);
				} else {
					context.drawImage(x, y, ((ZLTextImageElement)element).ImageData);
				}
			}
		}
		if (index != to) {
			ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.get(index++);
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

	private void buildInfos(final ZLTextWordCursor start, final ZLTextWordCursor result) {
		result.setCursor(start);
		final ZLPaintContext context = Context;
		int textAreaHeight = getTextAreaHeight();
		myLineInfos.clear();
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
				myLineInfos.add(info);
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
		final ZLTextLineInfo cachedInfo = myLineInfoCache.getInfo(info);
		if (cachedInfo != null) {
			applyControls(paragraphCursor, startIndex, cachedInfo.EndWordIndex);
			return cachedInfo;
		}

		int currentWordIndex = startIndex;
		int currentCharIndex = startCharIndex;
		final boolean isFirstLine = (startIndex == 0) && (startCharIndex == 0);

		final ZLTextParagraph para = paragraphCursor.getParagraph();
		if (para.getKind() == ZLTextParagraph.Kind.TREE_PARAGRAPH) {
			final ZLTextTreeParagraph treeParagraph = (ZLTextTreeParagraph)para;
			final int stackLength = treeParagraph.getDepth() - 1;
			final boolean[] stack = new boolean[stackLength];
			if (stackLength > 0) {
				ZLTextTreeParagraph ctp = treeParagraph;
				for (int index = 0; index < stackLength; ++index) {
					stack[index] = !ctp.isLastChild();
					ctp = ctp.getParent();
				}
			}
			info.NodeInfo = new ZLTextLineInfo.TreeNodeInfo(
				!treeParagraph.hasChildren(),
				treeParagraph.isOpen(),
				isFirstLine,
				paragraphCursor.Index,
				stack
			);
		}

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
		if (info.NodeInfo != null) {
			info.LeftIndent +=
				(context.getStringHeight() + 2) / 3 *
				4 * (info.NodeInfo.VerticalLinesStack.length + 1);
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
			myLineInfoCache.put(info);
		}

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info) {
		final ZLPaintContext context = Context;
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;

		setTextStyle(info.StartStyle);
		final int y = Math.min(context.getY() + info.Height, getBottomLine());
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		context.moveXTo(getLeftMargin() + info.LeftIndent);
		final int maxWidth = context.getWidth() - getLeftMargin() - getRightMargin();
		switch (myTextStyle.getAlignment()) {
			case ZLTextAlignmentType.ALIGN_RIGHT: {
				context.moveX(maxWidth - myTextStyle.getRightIndent() - info.Width);
				break;
			} 
			case ZLTextAlignmentType.ALIGN_CENTER: {
				context.moveX((maxWidth - myTextStyle.getRightIndent() - info.Width) / 2);
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
			final int x = context.getX();
			final int width = getElementWidth(context, element, charIndex);
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					int correction = fullCorrection / spaceCounter;
					context.moveX(context.getSpaceWidth() + correction);
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
			context.moveX(width);
		}
		if (!endOfParagraph) {
			final int len = info.EndCharIndex;
			if (len > 0) {
				final int wordIndex = info.EndWordIndex;
				final ZLTextWord word = (ZLTextWord)paragraph.getElement(wordIndex);
				final boolean addHyphenationSign = word.Data[word.Offset + len - 1] != '-';
				final int x = context.getX();
				final int width = getWordWidth(context, word, 0, len, addHyphenationSign);
				final int height = getElementHeight(word);
				final int descent = context.getDescent();
				myTextElementMap.add(new ZLTextElementArea(paragraphIndex, wordIndex, 0, len, addHyphenationSign,
					changeStyle, myTextStyle, word, x, x + width - 1, y - height + 1, y + descent));
			}	
		}
		context.moveY(info.Height + info.Descent + info.VSpaceAfter);
	}
	
	public void scrollPage(boolean forward, int scrollingMode, int value) {
		preparePaintInfo();
		if (myPaintState == PaintState.READY) {
			myPaintState = forward ? PaintState.TO_SCROLL_FORWARD : PaintState.TO_SCROLL_BACKWARD;
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
		StartCursor.setCursor(ZLTextParagraphCursor.cursor(myModel, paragraphIndex));
		StartCursor.moveTo(wordIndex, charIndex);
		EndCursor.reset();
		myPaintState = PaintState.START_IS_KNOWN;
	}
	
	public final void gotoPosition(int modelIndex, int paragraphIndex, int wordIndex, int charIndex) {
		setModelIndex(modelIndex);
		gotoPosition(paragraphIndex, wordIndex, charIndex);
	}

	public void gotoParagraph(int num, boolean last) {
		if (myModel == null) {
			return;
		}

		if (myModel instanceof ZLTextTreeModel) {
			if ((num >= 0) && (num < (int)myModel.getParagraphsNumber())) {
				ZLTextTreeParagraph tp = (ZLTextTreeParagraph)(myModel).getParagraph(num);
				if (myTreeStateIsFrozen) {
					int corrected = num;
					ZLTextTreeParagraph parent = tp.getParent();
					while ((corrected > 0) && (parent != null) && !parent.isOpen()) {
						for (--corrected; ((corrected > 0) && parent != (myModel).getParagraph(corrected)); --corrected);
						parent = parent.getParent();
					}
					if (last && (corrected != num)) {
						++corrected;
					}
					num = corrected;
				} else {
					tp.openTree();
					rebuildPaintInfo(true);
				}
			}
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
		int newWidth = getViewWidth();
		int newHeight = getTextAreaHeight();
		if ((newWidth != myOldWidth) || (newHeight != myOldHeight)) {
			myOldWidth = newWidth;
			myOldHeight = newHeight;
			rebuildPaintInfo(false);
		}

		if ((myPaintState == PaintState.NOTHING_TO_PAINT) || (myPaintState == PaintState.READY)) {
			return;
		}

		final ZLTextLineInfoVector infos = myLineInfos;
		final int infosSize = infos.size();
		final ZLTextLineInfoCache cache = myLineInfoCache;
		for (int i = 0; i < infosSize; ++i) {
			cache.put(infos.getInfo(i));
		}

		switch (myPaintState) {
			default:
				break;
			case PaintState.TO_SCROLL_FORWARD:
				if (!EndCursor.getParagraphCursor().isLast() || !EndCursor.isEndOfParagraph()) {
					final ZLTextWordCursor startCursor = new ZLTextWordCursor();
					switch (myScrollingMode) {
						case ScrollingMode.NO_OVERLAPPING:
							break;
						case ScrollingMode.KEEP_LINES:
							findLineFromEnd(startCursor, myOverlappingValue);
							break;
						case ScrollingMode.SCROLL_LINES:
							findLineFromStart(startCursor, myOverlappingValue);
							if (startCursor.isEndOfParagraph()) {
								startCursor.nextParagraph();
							}
							break;
						case ScrollingMode.SCROLL_PERCENTAGE:
							findPercentFromStart(startCursor, myOverlappingValue);
							break;
					}
				
					if (!startCursor.isNull() && startCursor.equalsToCursor(StartCursor)) {
						findLineFromStart(startCursor, 1);
					}

					if (!startCursor.isNull()) {
						final ZLTextWordCursor endCursor = new ZLTextWordCursor();
						buildInfos(startCursor, endCursor);
						if (!pageIsEmpty() && ((myScrollingMode != ScrollingMode.KEEP_LINES) || (!endCursor.equalsToCursor(EndCursor)))) {
							StartCursor.setCursor(startCursor);
							EndCursor.setCursor(endCursor);
							break;
						}
					}

					StartCursor.setCursor(EndCursor);
					buildInfos(StartCursor, EndCursor);
				}
				break;
			case PaintState.TO_SCROLL_BACKWARD:
				if (!StartCursor.getParagraphCursor().isFirst() || !StartCursor.isStartOfParagraph()) {
					switch (myScrollingMode) {
						case ScrollingMode.NO_OVERLAPPING:
							StartCursor.setCursor(findStart(StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							break;
						case ScrollingMode.KEEP_LINES:
						{
							ZLTextWordCursor endCursor = new ZLTextWordCursor();
							findLineFromStart(endCursor, myOverlappingValue);
							if (!endCursor.isNull() && endCursor.equalsToCursor(EndCursor)) {
								findLineFromEnd(endCursor, 1);
							}
							if (!endCursor.isNull()) {
								ZLTextWordCursor startCursor = findStart(endCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight());
								if (startCursor.equalsToCursor(StartCursor)) {
									StartCursor.setCursor(findStart(StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
								} else {
									StartCursor.setCursor(startCursor);
								}
							} else {
								StartCursor.setCursor(findStart(StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							}
							break;
						}
						case ScrollingMode.SCROLL_LINES:
							StartCursor.setCursor(findStart(StartCursor, SizeUnit.LINE_UNIT, myOverlappingValue));
							break;
						case ScrollingMode.SCROLL_PERCENTAGE:
							StartCursor.setCursor(findStart(StartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight() * myOverlappingValue / 100));
							break;
					}
					buildInfos(StartCursor, EndCursor);
					if (pageIsEmpty()) {
						StartCursor.setCursor(findStart(StartCursor, SizeUnit.LINE_UNIT, 1));
						buildInfos(StartCursor, EndCursor);
					}
				}
				break;
			case PaintState.START_IS_KNOWN:
				buildInfos(StartCursor, EndCursor);
				break;
			case PaintState.END_IS_KNOWN:
				StartCursor.setCursor(findStart(EndCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
				buildInfos(StartCursor, EndCursor);
				break;
		}
		myPaintState = PaintState.READY;
		myLineInfoCache.clear();
	}

	boolean pageIsEmpty() {
		final ZLTextLineInfoVector infos = myLineInfos;
		final int infosSize = infos.size();
		for (int i = 0; i < infosSize; ++i) {
			if (infos.getInfo(i).IsVisible) {
				return false;
			}
		}
		return true;
	}

	private void findLineFromStart(ZLTextWordCursor cursor, int overlappingValue) {
		if (myLineInfos.isEmpty() || (overlappingValue == 0)) {
			cursor.reset();
		} else {
			final ZLTextLineInfoVector infos = myLineInfos;
			final int size = infos.size();
			ZLTextLineInfo info = null;
			for (int i = 0; i < size; ++i) {
				info = infos.getInfo(i);
				if (info.IsVisible) {
					--overlappingValue;
					if (overlappingValue == 0) {
						break;
					}
				}
			}
			cursor.setCursor(info.ParagraphCursor);
			cursor.moveTo(info.EndWordIndex, info.EndCharIndex);
		}
	}

	void findLineFromEnd(ZLTextWordCursor cursor, int overlappingValue) {
		if (myLineInfos.isEmpty() || (overlappingValue == 0)) {
			cursor.reset();
		} else {
			final ZLTextLineInfoVector infos = myLineInfos;
			final int size = infos.size();
			ZLTextLineInfo info = null;
			for (int i = size - 1; i >= 0; --i) {
				info = infos.getInfo(i);
				if (info.IsVisible) {
					--overlappingValue;
					if (overlappingValue == 0) {
						break;
					}
				}
			}
			cursor.setCursor(info.ParagraphCursor);
			cursor.moveTo(info.StartWordIndex, info.StartCharIndex);
		}
	}

	private void findPercentFromStart(ZLTextWordCursor cursor, int percent) {
		if (myLineInfos.isEmpty()) {
			cursor.reset();
		} else {
			int height = getTextAreaHeight() * percent / 100;
			boolean visibleLineOccured = false;
			final ZLTextLineInfoVector infos = myLineInfos;
			final int size = infos.size();
			ZLTextLineInfo info = null;
			for (int i = 0; i < size; ++i) {
				info = infos.getInfo(i);
				if (info.IsVisible) {
					visibleLineOccured = true;
				}
				height -= info.Height + info.Descent + info.VSpaceAfter;
				if (visibleLineOccured && (height <= 0)) {
					break;
				}
			}
			cursor.setCursor(info.ParagraphCursor);
			cursor.moveTo(info.EndWordIndex, info.EndCharIndex);
		}
	}

	public void clearCaches() {
		rebuildPaintInfo(true);
	}

	protected void rebuildPaintInfo(boolean strong) {
		if (strong) {
			ZLTextParagraphCursorCache.clear();
		}

		if (myPaintState != PaintState.NOTHING_TO_PAINT) {
			myLineInfos.clear();
			if (!StartCursor.isNull()) {
				if (strong) {
					StartCursor.rebuild();
				}
				EndCursor.reset();
				myPaintState = PaintState.START_IS_KNOWN;
			} else if (!EndCursor.isNull()) {
				if (strong) {
					EndCursor.rebuild();
				}
				StartCursor.reset();
				myPaintState = PaintState.END_IS_KNOWN;
			}
		}

		if (strong) {
			myLineInfoCache.clear();
		}
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
		return (ZLTextElementArea)myTextElementMap.binarySearch(x, y);
	}

	protected int getParagraphIndexByCoordinate(int y) {
		ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.binarySearch(y);
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

	public boolean onStylusPress(int x, int y) {
		if (myModel == null) {
			return false;
		}
		ZLTextIndicatorInfo indicatorInfo = getIndicatorInfo();
		if (indicatorInfo.isVisible() && indicatorInfo.isSensitive() && !StartCursor.isNull()) {
			final ZLPaintContext context = Context;
			final int yBottom = context.getHeight() - getBottomMargin() - 1;
			final int yTop = yBottom - indicatorInfo.getHeight() + 1;
			final int xLeft = getLeftMargin();
			final int xRight = context.getWidth() - getRightMargin() - 1;
			if ((x > xLeft) && (x < xRight) && (y > yTop) && (y < yBottom)) {
				Position position = new Position(myCurrentModelIndex, StartCursor);
				myTreeStateIsFrozen = true;
				final int[] textSizeVector = myTextSize;
				final int value = textSizeVector[textSizeVector.length - 1] * (x - xLeft) / (xRight - xLeft - 1);
				final int paragraphIndex = lowerBound(textSizeVector, value);
				gotoParagraph(paragraphIndex, true);
				preparePaintInfo();
				final int endCursorIndex = EndCursor.getParagraphCursor().Index;
				if (endCursorIndex == paragraphIndex) {
					final int paragraphLength = 
						EndCursor.getParagraphCursor().getParagraphLength();
					int wordCounter = paragraphLength
						* (value - textSizeVector[paragraphIndex])
						/ (textSizeVector[paragraphIndex + 1] - textSizeVector[paragraphIndex]);
					if (wordCounter > 0) {
						if (wordCounter == paragraphLength) {
							EndCursor.nextParagraph();
						} else {
							wordCounter -= EndCursor.getWordIndex();
							while (wordCounter-- > 0) {
								EndCursor.nextWord();
							}
						}
						StartCursor.reset();
						rebuildPaintInfo(false);
					}
				}
				if (StartCursor.isNull()) {
					preparePaintInfo();
				}
				savePosition(position, myCurrentModelIndex, StartCursor);
				Application.refreshWindow();
				myTreeStateIsFrozen = false;
				return true;
			}
		}

		if (myModel instanceof ZLTextTreeModel) {
			ZLTextTreeNodeArea nodeArea = (ZLTextTreeNodeArea)myTreeNodeMap.binarySearch(x, y);
			if (nodeArea != null) {
				final int index = nodeArea.ParagraphIndex;
				final ZLTextTreeParagraph paragraph = ((ZLTextTreeModel)myModel).getTreeParagraph(index);
				paragraph.open(!paragraph.isOpen());
				rebuildPaintInfo(true);
				preparePaintInfo();
				if (paragraph.isOpen()) {
					int nextParagraphIndex = index + paragraph.getFullSize();
					int lastParagraphIndex = EndCursor.getParagraphCursor().Index;
					if (EndCursor.isEndOfParagraph()) {
						++lastParagraphIndex;
					}
					/*
					if (lastParagraphIndex < nextParagraphIndex) {
						gotoParagraph(nextParagraphIndex, true);
						preparePaintInfo();
					}
					*/
				}
				int firstParagraphIndex = StartCursor.getParagraphCursor().Index;
				if (StartCursor.isStartOfParagraph()) {
					--firstParagraphIndex;
				}
				/*
				if (firstParagraphIndex >= paragraphIndex) {
					gotoParagraph(paragraphIndex);
					preparePaintInfo();
				}
				*/
				Application.refreshWindow();
				return true;
			}
		}
		return false;
	}

	public boolean onStylusMovePressed(int x, int y) {
		if (mySelectionModel.extendTo(x, y)) {
			//copySelectedTextToClipboard(ZLDialogManager::CLIPBOARD_SELECTION);
			Application.refreshWindow();
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
			Application.refreshWindow();
		}
	}

}
