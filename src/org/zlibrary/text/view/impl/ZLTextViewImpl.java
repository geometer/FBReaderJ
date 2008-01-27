package org.zlibrary.text.view.impl;

import java.util.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.options.*;

import org.zlibrary.text.model.*;

import org.zlibrary.text.view.*;
import org.zlibrary.text.view.style.*;

public abstract class ZLTextViewImpl extends ZLTextView {
	private ZLTextModel myModel;

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

	private final ZLTextWordCursor myStartCursor = new ZLTextWordCursor();
	private final ZLTextWordCursor myEndCursor = new ZLTextWordCursor();

	private final ZLTextLineInfoVector myLineInfos = new ZLTextLineInfoVector();
	private final ZLTextLineInfoCache myLineInfoCache = new ZLTextLineInfoCache();

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;
		
	private final ZLTextRectangularAreaVector myTextElementMap
		= new ZLTextRectangularAreaVector();
	private final ZLTextRectangularAreaVector myTreeNodeMap
		= new ZLTextRectangularAreaVector();

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		resetTextStyle();
	}

	public void setModel(ZLTextModel model) {
		myModel = model;
		if (myModel.getParagraphsNumber() > 0) {
			myStartCursor.setCursor(ZLTextParagraphCursor.cursor(myModel, 0));
			myEndCursor.reset();
			myPaintState = PaintState.START_IS_KNOWN;
		}
	}
	protected ZLTextModel getModel() {
		return myModel;
	}

	protected ZLTextWordCursor getStartCursor() {
		return myStartCursor;
	}

	private void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		getContext().setFont(style.getFontFamily(), style.getFontSize(), style.isBold(), style.isItalic());
	}

	private void resetTextStyle() {
		setTextStyle(ZLTextStyleCollection.getInstance().getBaseStyle());
	}

	private void applyControl(ZLTextControlElement control) {
		final ZLTextStyle textStyle = myTextStyle;
		if (control.IsStart) {
//			System.out.println("Apply Start " + control.Kind);
			ZLTextStyleDecoration decoration = ZLTextStyleCollection.getInstance().getDecoration(control.Kind);
			setTextStyle(decoration.createDecoratedStyle(textStyle));
//			if (decoration instanceof ZLTextFullStyleDecoration) {
//				System.out.println("FontSize = " + textStyle.getFontSize());
//			}
		} else {
//			System.out.println("Apply End " + control.Kind);
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

	private int getElementWidth(ZLTextElement element, int charNumber) {
		if (element instanceof ZLTextWord) {
			return getWordWidth((ZLTextWord)element, charNumber, -1, false);
		} else if (element instanceof ZLTextImageElement) {
			return getContext().imageWidth(((ZLTextImageElement)element).getImageData());
		} else if (element == ZLTextElement.IndentElement) {
			return myTextStyle.getFirstLineIndentDelta();
		}
		return 0; 
	}

	private int getElementHeight(ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			int wordHeight = myWordHeight;
			if (wordHeight == -1) {
				final ZLTextStyle textStyle = myTextStyle;
				wordHeight = (int)(getContext().getStringHeight() * textStyle.getLineSpacePercent() / 100) + textStyle.getVerticalShift();
				myWordHeight = wordHeight;
			}
			return wordHeight;
		} else if (element instanceof ZLTextImageElement) {
			final ZLPaintContext context = getContext();
			return context.imageHeight(((ZLTextImageElement)element).getImageData()) + 
				Math.max((int)(context.getStringHeight() * (myTextStyle.getLineSpacePercent() - 100) / 100), 3);
		}
		return 0;
	}
	
	private int getElementDescent(ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			return getContext().getDescent();
		}
		return 0;
	}

	private int getTextAreaHeight() {
		return getContext().getHeight() - getTopMargin() - getBottomMargin();
	}

	private int getWordWidth(ZLTextWord word) {
		return word.getWidth(getContext());
	}
	
	private int getWordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if (start == 0 && length == -1) {
			return word.getWidth(getContext());
		}	
		return 0;
	}
		
	public void paint() {
		preparePaintInfo();

		myTextElementMap.clear();
		myTreeNodeMap.clear();

		final ZLPaintContext context = getContext();
		context.clear(ZLTextStyleCollection.getInstance().getBaseStyle().BackgroundColorOption.getValue());

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

		context.moveYTo(getTopMargin());
		for (int i = 0; i < lineInfosSize; ++i) {
			drawTextLine(context, lineInfos.getInfo(i), labels[i], labels[i + 1]);
			//System.out.println("Line " + index + " Y = " + context.getY());
		}

//		for (ZLTextElementArea area : myTextElementMap) {
//			System.out.println(area.XStart + " " + area.XEnd + " " + area.YStart + " " + area.YEnd);
//		}
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
						info.ParagraphNumber,
						x, x + 4 * qstep,
						y - height + 1, y
					)
				);
			}
		} else if (!info.IsLeaf && info.IsOpen) {
			context.drawLine(x + 2 * qstep, y + vSpaceAfter, x + 2 * qstep, y - height + 1);
		}
	}

	private void drawTextLine(final ZLPaintContext context, final ZLTextLineInfo info, final int from, final int to) {
		final ZLTextParagraphCursor paragraph = info.ParagraphCursor;
	
		context.moveY(info.Height);
		int maxY = getTextAreaHeight() + getTopMargin();
		if (context.getY() > maxY) {
			context.moveYTo(maxY);
		}
		context.moveXTo(getLeftMargin());	
		if (info.NodeInfo != null) {
			drawTreeLines(context, info.NodeInfo, info.Height, info.Descent + info.VSpaceAfter);
		}

		int index = from;
		final int endWordNumber = info.EndWordNumber;
		int charNumber = info.RealStartCharNumber;
		for (int wordNumber = info.RealStartWordNumber; wordNumber != endWordNumber; ++wordNumber, charNumber = 0) {
			final ZLTextElement element = paragraph.getElement(wordNumber);
			if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.println("Word = " + ((ZLTextWord) element).getWord());
				ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.getArea(index++);
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - getElementDescent(element) - myTextStyle.getVerticalShift();
				context.moveXTo(x);
				if (element instanceof ZLTextWord) {
					//System.out.println("Draw " + x + " " + y + " " + area.YEnd);
					drawWord(x, y, (ZLTextWord) element, charNumber, -1, false);
				} else {
					context.drawImage(x, y, ((ZLTextImageElement) element).getImageData());
				}
			}
		}
		if (index != to) {
			ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.getArea(index++);
			if (area.ChangeStyle) {
				setTextStyle(area.Style);
			}
			int len = info.EndCharNumber;
			final ZLTextWord word = (ZLTextWord)paragraph.getElement(info.EndWordNumber);
			final int x = area.XStart;
			final int y = area.YEnd - getElementDescent(word) - myTextStyle.getVerticalShift();
			drawWord(x, y, word, 0, len, area.AddHyphenationSign);
		}
		context.moveY(info.Descent + info.VSpaceAfter);
	}

	private void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		final ZLPaintContext context = getContext();
		if ((start == 0) && (length == -1)) {
			context.setColor(myTextStyle.getColor());	
			context.drawString(x, y, word.Data, word.Offset, word.Length);
		} else {
			System.out.println("Shouldn't be here - no hyphenations supported yet.");
		}
	}

	private void buildInfos(final ZLTextWordCursor start, final ZLTextWordCursor result) {
		result.setCursor(start);
		final ZLPaintContext context = getContext();
		int textAreaHeight = getTextAreaHeight();
		myLineInfos.clear();
		int counter = 0;
		do {
			resetTextStyle();
			final ZLTextParagraphCursor paragraphCursor = result.getParagraphCursor();
			final int wordNumber = result.getWordNumber();
			applyControls(paragraphCursor, 0, wordNumber);	
			ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, wordNumber, result.getCharNumber(), myTextStyle);
			final int endIndex = info.ParagraphCursorLength;
			while (info.EndWordNumber != endIndex) {
				info = processTextLine(context, paragraphCursor, info.EndWordNumber, info.EndCharNumber, endIndex);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				result.moveTo(info.EndWordNumber, info.EndCharNumber);
				myLineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (result.isEndOfParagraph() && result.nextParagraph() && !result.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		resetTextStyle();
//		System.out.println("----------------------INFOS BUILT--------------------------------");
	}

	private ZLTextLineInfo processTextLine(final ZLPaintContext context, final ZLTextParagraphCursor paragraphCursor, final int startIndex, final int startCharNumber, final int endIndex) {
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharNumber, myTextStyle);
		final ZLTextLineInfo cachedInfo = myLineInfoCache.getInfo(info);
		if (cachedInfo != null) {
			return cachedInfo;
		}

		int currentWordIndex = startIndex;
		int currentCharNumber = startCharNumber;
		final boolean isFirstLine = (startIndex == 0) && (startCharNumber == 0);

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
				paragraphCursor.getIndex(),
				stack
			);
		}

		if (isFirstLine) {
			ZLTextElement element = paragraphCursor.getElement(currentWordIndex);
			while (element instanceof ZLTextControlElement) {
				if (element instanceof ZLTextControlElement) {
					applyControl((ZLTextControlElement) element);
				}
				++currentWordIndex;
				currentCharNumber = 0;
				if (currentWordIndex == endIndex) {
					break;
				}
				element = paragraphCursor.getElement(currentWordIndex);
			}
			info.StartStyle = myTextStyle;
			info.RealStartWordNumber = currentWordIndex;
			info.RealStartCharNumber = currentCharNumber;
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
		
		if (info.RealStartWordNumber == endIndex) {
			info.EndWordNumber = info.RealStartWordNumber;
			info.EndCharNumber = info.RealStartCharNumber;
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
			newWidth += getElementWidth(element, currentCharNumber);
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
				//System.out.println("Word = " + ((ZLTextWord) element).Data + " FontSize = " + myTextStyle.getFontSize());
			} else if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement) element);
			} else if (element instanceof ZLTextImageElement) {
				wordOccurred = true;
				isVisible = true;
			}			
			if ((newWidth > maxWidth) && (info.EndWordNumber != startIndex)) {
				break;
			}
			ZLTextElement previousElement = element;
			++currentWordIndex;
			currentCharNumber = 0;
			boolean allowBreak = currentWordIndex == endIndex;
			if (!allowBreak) {
				element = paragraphCursor.getElement(currentWordIndex); 
				allowBreak = (((!(element instanceof ZLTextWord)) || (previousElement instanceof ZLTextWord)) && 
						!(element instanceof ZLTextImageElement) && !(element instanceof ZLTextControlElement));
			}
			if (allowBreak) {
				info.IsVisible = isVisible;
				info.Width = newWidth;
				info.Height = Math.max(info.Height, newHeight);
				info.Descent = Math.max(info.Descent, newDescent);
				info.EndWordNumber = currentWordIndex;
				info.EndCharNumber = currentCharNumber;
				info.SpaceCounter = internalSpaceCounter;
				storedStyle = myTextStyle;
				removeLastSpace = !wordOccurred && (internalSpaceCounter > 0);
			}	
		} while (currentWordIndex != endIndex);

/*		if (!current.equalWordNumber(end)) {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber());
			if (element instanceof ZLTextWord) { 
				newWidth -= getElementWidth(element, current.getCharNumber());
			}
			info.IsVisible = true;
			info.Width = newWidth;
			info.Height = Math.max(info.Height, newHeight);
			info.Descent = Math.max(info.Descent, newDescent);
			info.setEnd(current);
			info.SpaceCounter = internalSpaceCounter;
		}*/
		
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

		//System.out.println();
		//System.out.println("Info widht = " + info.Width);

	//	System.out.println(info.End.getElement());
		if ((info.EndWordNumber != endIndex) || (endIndex == info.ParagraphCursorLength)) {
			myLineInfoCache.put(info);
		}

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info) {
		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraphCursor = info.ParagraphCursor;

		setTextStyle(info.StartStyle);
		final int y = Math.min(context.getY() + info.Height, getTextAreaHeight() + getTopMargin());
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		context.moveXTo(getLeftMargin() + info.LeftIndent);
		//System.out.println(context.getWidth() + " " + info.Width);
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
				if (!endOfParagraph && (paragraphCursor.getElement(info.EndWordNumber) != ZLTextElement.AfterParagraph)) {
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
		int paragraphNumber = paragraph.getIndex();
//		System.out.println();
		final int endWordNumber = info.EndWordNumber;
		int charNumber = info.RealStartCharNumber;
		for (int wordNumber = info.RealStartWordNumber; wordNumber != endWordNumber; ++wordNumber, charNumber = 0) {
			final ZLTextElement element = paragraph.getElement(wordNumber);
			final int x = context.getX();
			final int width = getElementWidth(element, charNumber);
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					int correction = fullCorrection / spaceCounter;
					context.moveX(context.getSpaceWidth() + correction);
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}	
			} else if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.print(((ZLTextWord) element).Data + " " + x + " ");
				final int height = getElementHeight(element);
				final int descent = getElementDescent(element);
				final int length = (element instanceof ZLTextWord) ? ((ZLTextWord) element).Length : 0;
				myTextElementMap.add(new ZLTextElementArea(paragraphNumber, wordNumber, charNumber, 
					length, false, changeStyle, myTextStyle, element, x, x + width - 1, y - height + 1, y + descent));
				changeStyle = false;
				wordOccurred = true;
			} else if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
				changeStyle = true;
			}
			context.moveX(width);
		}
		context.moveY(info.Height + info.Descent + info.VSpaceAfter);
	}
	
	public String caption() {
		return "SampleView";
	}

	public void scrollPage(boolean forward, int scrollingMode, int value) {
		preparePaintInfo();
		if (myPaintState == PaintState.READY) {
			myPaintState = forward ? PaintState.TO_SCROLL_FORWARD : PaintState.TO_SCROLL_BACKWARD;
			myScrollingMode = scrollingMode;
			myOverlappingValue = value;
		}
	}

	public void gotoPosition(int paragraphNumber, int wordNumber, int charNumber) {
		// TODO: implement
		int paragraphs = myModel.getParagraphsNumber();
		int pn = Math.max(0, Math.min(paragraphNumber, paragraphs - 2));
		myStartCursor.setCursor(ZLTextParagraphCursor.cursor(myModel, pn));
		myStartCursor.moveTo(wordNumber, charNumber);
		myEndCursor.reset();
		myPaintState = PaintState.START_IS_KNOWN;
	}

	/*
	public void gotoParagraph(int index) {
		// TODO: implement
		myStartParagraphNumber = index;
	}
	*/

	private int getViewWidth() {
		return Math.max(getContext().getWidth() - getLeftMargin() - getRightMargin(), 1);
	}

	protected void preparePaintInfo() {
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
				if (!myEndCursor.getParagraphCursor().isLast() || !myEndCursor.isEndOfParagraph()) {
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
				
					if (!startCursor.isNull() && startCursor.equalsToCursor(myStartCursor)) {
						findLineFromStart(startCursor, 1);
					}

					if (!startCursor.isNull()) {
						final ZLTextWordCursor endCursor = new ZLTextWordCursor();
						buildInfos(startCursor, endCursor);
						if (!pageIsEmpty() && ((myScrollingMode != ScrollingMode.KEEP_LINES) || (!endCursor.equalsToCursor(myEndCursor)))) {
							myStartCursor.setCursor(startCursor);
							myEndCursor.setCursor(endCursor);
							break;
						}
					}

					myStartCursor.setCursor(myEndCursor);
					buildInfos(myStartCursor, myEndCursor);
				}
				break;
			case PaintState.TO_SCROLL_BACKWARD:
				if (!myStartCursor.getParagraphCursor().isFirst() || !myStartCursor.isStartOfParagraph()) {
					switch (myScrollingMode) {
						case ScrollingMode.NO_OVERLAPPING:
							myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							break;
						case ScrollingMode.KEEP_LINES:
						{
							ZLTextWordCursor endCursor = new ZLTextWordCursor();
							findLineFromStart(endCursor, myOverlappingValue);
							if (!endCursor.isNull() && endCursor.equalsToCursor(myEndCursor)) {
								findLineFromEnd(endCursor, 1);
							}
							if (!endCursor.isNull()) {
								ZLTextWordCursor startCursor = findStart(endCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight());
								if (startCursor.equalsToCursor(myStartCursor)) {
									myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
								} else {
									myStartCursor.setCursor(startCursor);
								}
							} else {
								myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
							}
							break;
						}
						case ScrollingMode.SCROLL_LINES:
							myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.LINE_UNIT, myOverlappingValue));
							break;
						case ScrollingMode.SCROLL_PERCENTAGE:
							myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight() * myOverlappingValue / 100));
							break;
					}
					buildInfos(myStartCursor, myEndCursor);
					if (pageIsEmpty()) {
						myStartCursor.setCursor(findStart(myStartCursor, SizeUnit.LINE_UNIT, 1));
						buildInfos(myStartCursor, myEndCursor);
					}
				}
				break;
			case PaintState.START_IS_KNOWN:
				buildInfos(myStartCursor, myEndCursor);
				break;
			case PaintState.END_IS_KNOWN:
				myStartCursor.setCursor(findStart(myEndCursor, SizeUnit.PIXEL_UNIT, getTextAreaHeight()));
				buildInfos(myStartCursor, myEndCursor);
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
			cursor.moveTo(info.EndWordNumber, info.EndCharNumber);
		}
	}

	void findLineFromEnd(ZLTextWordCursor cursor, int overlappingValue) {
		if (myLineInfos.isEmpty() || (overlappingValue == 0)) {
			cursor.reset();
		} else {
			final ZLTextLineInfoVector infos = myLineInfos;
			final int size = infos.size();
			ZLTextLineInfo info = null;
			for (int i = size; i >= 0; --i) {
				info = infos.getInfo(i);
				if (info.IsVisible) {
					--overlappingValue;
					if (overlappingValue == 0) {
						break;
					}
				}
			}
			cursor.setCursor(info.ParagraphCursor);
			cursor.moveTo(info.StartWordNumber, info.StartCharNumber);
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
			cursor.moveTo(info.EndWordNumber, info.EndCharNumber);
		}
	}

	public void clearCaches() {
		rebuildPaintInfo(true);
		ZLTextParagraphCursorCache.clear();
	}

	private void rebuildPaintInfo(boolean strong) {
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;
		}

		myLineInfos.clear();
		if (!myStartCursor.isNull()) {
			if (strong) {
				myStartCursor.rebuild();
				myLineInfoCache.clear();
			}
			myEndCursor.reset();
			myPaintState = PaintState.START_IS_KNOWN;
		} else if (!myEndCursor.isNull()) {
			if (strong) {
				myEndCursor.rebuild();
				myLineInfoCache.clear();
			}
			myStartCursor.reset();
			myPaintState = PaintState.END_IS_KNOWN;
		}
	}

	private int infoSize(ZLTextLineInfo info, int unit) {
		return (unit == SizeUnit.PIXEL_UNIT) ? (info.Height + info.Descent + info.VSpaceAfter) : (info.IsVisible ? 1 : 0);
	}

	private int paragraphSize(ZLTextWordCursor cursor, boolean beforeCurrentPosition, int unit) {
		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		final int endWordNumber =
			beforeCurrentPosition ? cursor.getWordNumber() : paragraphCursor.getParagraphLength();
		
		resetTextStyle();

		int size = 0;

		int wordNumber = 0;
		int charNumber = 0;
		while (wordNumber != endWordNumber) {
			ZLTextLineInfo info = processTextLine(context, paragraphCursor, wordNumber, charNumber, endWordNumber);
			wordNumber = info.EndWordNumber;
			charNumber = info.EndCharNumber;
			size += infoSize(info, unit);
		}

		return size;
	}

	private void skip(ZLTextWordCursor cursor, int unit, int size) {
		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraphCursor = cursor.getParagraphCursor();
		final int endWordNumber = paragraphCursor.getParagraphLength();

		resetTextStyle();
		applyControls(paragraphCursor, 0, cursor.getWordNumber());

		while (!cursor.isEndOfParagraph() && (size > 0)) {
			ZLTextLineInfo info = processTextLine(context, paragraphCursor, cursor.getWordNumber(), cursor.getCharNumber(), endWordNumber);
			cursor.moveTo(info.EndWordNumber, info.EndCharNumber);
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
		return (area != null) ? area.ParagraphNumber : -1;
	}

	public boolean onStylusPress(int x, int y) {
		if (myModel instanceof ZLTextTreeModel) {
			ZLTextTreeNodeArea nodeArea = (ZLTextTreeNodeArea)myTreeNodeMap.binarySearch(x, y);
			if (nodeArea != null) {
				final int index = nodeArea.ParagraphNumber;
				final ZLTextTreeParagraph paragraph = ((ZLTextTreeModel)myModel).getTreeParagraph(index);
				paragraph.open(!paragraph.isOpen());
				rebuildPaintInfo(true);
				preparePaintInfo();
				if (paragraph.isOpen()) {
					int nextParagraphNumber = index + paragraph.getFullSize();
					int lastParagraphNumber = myEndCursor.getParagraphCursor().getIndex();
					if (myEndCursor.isEndOfParagraph()) {
						++lastParagraphNumber;
					}
					/*
					if (lastParagraphNumber < nextParagraphNumber) {
						gotoParagraph(nextParagraphNumber, true);
						preparePaintInfo();
					}
					*/
				}
				int firstParagraphNumber = myStartCursor.getParagraphCursor().getIndex();
				if (myStartCursor.isStartOfParagraph()) {
					--firstParagraphNumber;
				}
				/*
				if (firstParagraphNumber >= paragraphNumber) {
					gotoParagraph(paragraphNumber);
					preparePaintInfo();
				}
				*/
				getApplication().refreshWindow();
				return true;
			}
		}
		return false;
	}
}
