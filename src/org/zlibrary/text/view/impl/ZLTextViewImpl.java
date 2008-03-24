package org.zlibrary.text.view.impl;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLTextMark;
import org.zlibrary.text.hyphenation.*;
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

	private int[] myTextSize;

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;
		
	private boolean myTreeStateIsFrozen = false;
	
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
		if (model != null) {
			final int paragraphsNumber = model.getParagraphsNumber();
			if (paragraphsNumber > 0) {
				myTextSize = new int[paragraphsNumber + 1];
				myTextSize[0] = 0;
				for (int i = 0; i < paragraphsNumber; ++i) {
					myTextSize[i + 1] = myTextSize[i] + model.getParagraphTextLength(i);
				}
				myStartCursor.setCursor(ZLTextParagraphCursor.cursor(model, 0));
				myEndCursor.reset();
				myPaintState = PaintState.START_IS_KNOWN;
			}
		}
	}

	protected ZLTextModel getModel() {
		return myModel;
	}

	protected ZLTextWordCursor getStartCursor() {
		return myStartCursor;
	}
	
	public void highlightParagraph(int paragraphNumber) {
		myModel.selectParagraph(paragraphNumber);
		rebuildPaintInfo(true);
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

	private int getElementWidth(ZLPaintContext context, ZLTextElement element, int charNumber) {
		if (element instanceof ZLTextWord) {
			return getWordWidth(context, (ZLTextWord)element, charNumber);
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
				wordHeight = (int)(getContext().getStringHeight() * textStyle.getLineSpacePercent() / 100) + textStyle.getVerticalShift();
				myWordHeight = wordHeight;
			}
			return wordHeight;
		} else if (element instanceof ZLTextImageElement) {
			final ZLPaintContext context = getContext();
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

	abstract protected ZLTextIndicatorInfo getIndicatorInfo();

	private int getTextAreaHeight() {
		return getContext().getHeight() - getTopMargin() - getBottomMargin() - getIndicatorInfo().getFullHeight();
	}

	private int getBottomLine() {
		return getContext().getHeight() - getBottomMargin() - getIndicatorInfo().getFullHeight();
	}

	private static int getWordWidth(ZLPaintContext context, ZLTextWord word, int start) {
		return
			(start == 0) ?
				word.getWidth(context) :
				context.getStringWidth(word.Data, word.Offset + start, word.Length - start);
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
	
	private void moveStartCursor(int paragraphNumber) {
		moveStartCursor(paragraphNumber, 0, 0);
	}
		
	private void moveStartCursor(int paragraphNumber, int wordNumber, int charNumber) {
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;
		}
		if (myStartCursor.isNull()) {
			myStartCursor.setCursor(myEndCursor);
		}
		myStartCursor.moveToParagraph(paragraphNumber);
		myStartCursor.moveTo(wordNumber, charNumber);
		myEndCursor.reset();
		myLineInfos.clear();
		myPaintState = PaintState.START_IS_KNOWN;
	}

	private void moveEndCursor(int paragraphNumber) {
		moveEndCursor(paragraphNumber, 0, 0);
	}

	private void moveEndCursor(int paragraphNumber, int wordNumber, int charNumber) {
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;	
		}
		if (myEndCursor.isNull()) {
			myEndCursor.setCursor(myStartCursor);
		}
		myEndCursor.moveToParagraph(paragraphNumber);
		if ((paragraphNumber > 0) && (wordNumber == 0) && (charNumber == 0)) {
			myEndCursor.previousParagraph();
			myEndCursor.moveToParagraphEnd();
		} else {
			myEndCursor.moveTo(wordNumber, charNumber);
		}
		myStartCursor.reset();
		myLineInfos.clear();
		myPaintState = PaintState.END_IS_KNOWN;
	}

	public void gotoMark(ZLTextMark mark) {
		if (mark.ParagraphNumber < 0) {
			return;
		}
		boolean doRepaint = false;
		if (myStartCursor.isNull()) {
			doRepaint = true;
			preparePaintInfo();
		}
		if (myStartCursor.isNull()) {
			return;
		}
		if ((myStartCursor.getParagraphCursor().getIndex() != mark.ParagraphNumber) || (myStartCursor.getPosition().compareTo(mark) > 0)) {
			doRepaint = true;
			gotoParagraph(mark.ParagraphNumber, false);
			preparePaintInfo();
		}
		if (myEndCursor.isNull()) {
			preparePaintInfo();
		}
		while (mark.compareTo(myEndCursor.getPosition()) > 0) { 
			doRepaint = true;
			scrollPage(true, ScrollingMode.NO_OVERLAPPING, 0);
			preparePaintInfo();
		}
		if (doRepaint) {
			getApplication().refreshWindow();
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
		if (!myStartCursor.isNull()) {
			rebuildPaintInfo(true);
			ZLTextMark position = myStartCursor.getPosition();
			gotoMark(wholeText ? 
				(backward ? myModel.getLastMark() : myModel.getFirstMark()) :
				(backward ? myModel.getPreviousMark(position) : myModel.getNextMark(position)));
			getApplication().refreshWindow();
		}
	}

	public void paint() {
		//android.os.Debug.startMethodTracing("/tmp/paint");
		preparePaintInfo();

		myTextElementMap.clear();
		myTreeNodeMap.clear();

		final ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().getBaseStyle();
		final ZLPaintContext context = getContext();
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

			final ZLTextWordCursor wordCursor = myEndCursor;
			final ZLTextParagraphCursor paragraphCursor = wordCursor.getParagraphCursor();
			final int paragraphIndex = paragraphCursor.getIndex();
			final int[] textSizeVector = myTextSize;
			int fullTextSize = textSizeVector[textSizeVector.length - 1];
			if (fullTextSize > 0) {
				int textSizeBeforeCursor = textSizeVector[paragraphIndex];
				final int paragraphLength = paragraphCursor.getParagraphLength();
				if (paragraphLength > 0) {
					textSizeBeforeCursor +=
						(textSizeVector[paragraphIndex + 1] - textSizeBeforeCursor)
						* wordCursor.getWordNumber()
						/ paragraphLength;
				}
				long width = fullWidth * textSizeBeforeCursor / fullTextSize;
				if (width < 0) {
					width = 0;
				}
				if (width > fullWidth) {
					width = fullWidth;
				}
				context.setFillColor(indicatorInfo.getColor());
				context.fillRectangle(xLeft + 1, yTop + 1, xLeft + 1 + (int)width, yBottom - 1);
			}
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
		int maxY = getBottomLine();
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
				ZLTextElementArea area = (ZLTextElementArea)myTextElementMap.getArea(index++);
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - getElementDescent(context, element) - myTextStyle.getVerticalShift();
				context.moveXTo(x);
				if (element instanceof ZLTextWord) {
					drawWord(x, y, (ZLTextWord) element, charNumber, -1, false);
				} else {
					context.drawImage(x, y, ((ZLTextImageElement) element).ImageData);
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
			final int y = area.YEnd - context.getDescent() - myTextStyle.getVerticalShift();
			drawWord(x, y, word, 0, len, area.AddHyphenationSign);
		}
		context.moveY(info.Descent + info.VSpaceAfter);
	}

	private void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		final ZLPaintContext context = getContext();
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
		final ZLPaintContext context = getContext();
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
					context.setColor(ZLTextStyleCollection.getInstance().getBaseStyle().SelectedTextColorOption.getValue());
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
	}

	private ZLTextLineInfo processTextLine(final ZLPaintContext context, final ZLTextParagraphCursor paragraphCursor, 
		final int startIndex, final int startCharNumber, final int endIndex) {
		final ZLTextLineInfo info = new ZLTextLineInfo(paragraphCursor, startIndex, startCharNumber, myTextStyle);
		final ZLTextLineInfo cachedInfo = myLineInfoCache.getInfo(info);
		if (cachedInfo != null) {
			applyControls(paragraphCursor, startIndex, cachedInfo.EndWordNumber);
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
			newWidth += getElementWidth(context, element, currentCharNumber);
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
				info.EndWordNumber = currentWordIndex;
				info.EndCharNumber = currentCharNumber;
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
				newWidth -= getWordWidth(context, word, currentCharNumber);
				int spaceLeft = maxWidth - newWidth;
				if ((word.Length > 3) && (spaceLeft > 2 * getContext().getSpaceWidth())) {
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
						info.EndWordNumber = currentWordIndex;
						info.EndCharNumber = hyphenationPosition;
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

		if ((info.EndWordNumber != endIndex) || (endIndex == info.ParagraphCursorLength)) {
			myLineInfoCache.put(info);
		}

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info) {
		final ZLPaintContext context = getContext();
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
		final int endWordNumber = info.EndWordNumber;
		int charNumber = info.RealStartCharNumber;
		for (int wordNumber = info.RealStartWordNumber; wordNumber != endWordNumber; ++wordNumber, charNumber = 0) {
			final ZLTextElement element = paragraph.getElement(wordNumber);
			final int x = context.getX();
			final int width = getElementWidth(context, element, charNumber);
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
		if (!endOfParagraph) {
			int len = info.EndCharNumber;
			if (len > 0) {
				final int wordNumber = info.EndWordNumber;
				final ZLTextWord word = (ZLTextWord)paragraph.getElement(wordNumber);
				final boolean addHyphenationSign = word.Data[word.Offset + len - 1] != '-';
				final int x = context.getX();
				final int width = getWordWidth(context, word, 0, len, addHyphenationSign);
				final int height = getElementHeight(word);
				final int descent = context.getDescent();
				myTextElementMap.add(new ZLTextElementArea(paragraphNumber, wordNumber, 0, len, addHyphenationSign,
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

	public void gotoPosition(int paragraphNumber, int wordNumber, int charNumber) {
		// TODO: implement
		int paragraphs = myModel.getParagraphsNumber();
		int pn = Math.max(0, Math.min(paragraphNumber, paragraphs - 2));
		myStartCursor.setCursor(ZLTextParagraphCursor.cursor(myModel, pn));
		myStartCursor.moveTo(wordNumber, charNumber);
		myEndCursor.reset();
		myPaintState = PaintState.START_IS_KNOWN;
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
	}

	protected void rebuildPaintInfo(boolean strong) {
		if (myPaintState == PaintState.NOTHING_TO_PAINT) {
			return;
		}

		myLineInfos.clear();
		if (!myStartCursor.isNull()) {
			if (strong) {
				ZLTextParagraphCursorCache.clear();
				myStartCursor.rebuild();
				myLineInfoCache.clear();
			}
			myEndCursor.reset();
			myPaintState = PaintState.START_IS_KNOWN;
		} else if (!myEndCursor.isNull()) {
			if (strong) {
				ZLTextParagraphCursorCache.clear();
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
		ZLTextIndicatorInfo indicatorInfo = getIndicatorInfo();
		if (indicatorInfo.isVisible() && indicatorInfo.isSensitive()) {
			final ZLPaintContext context = getContext();
			final int yBottom = context.getHeight() - getBottomMargin() - 1;
			final int yTop = yBottom - indicatorInfo.getHeight() + 1;
			final int xLeft = getLeftMargin();
			final int xRight = context.getWidth() - getRightMargin() - 1;
			if ((x > xLeft) && (x < xRight) && (y > yTop) && (y < yBottom)) {
				System.err.println("indicator touched at " + (x - xLeft) + " of " + (xRight - xLeft - 1));
				return true;
			}
		}

		//search("FBReader", true, true, false, false);
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
