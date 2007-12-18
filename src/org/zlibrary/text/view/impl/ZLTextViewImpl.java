package org.zlibrary.text.view.impl;

import java.util.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.options.*;

import org.zlibrary.text.model.*;

import org.zlibrary.text.view.*;
import org.zlibrary.text.view.style.*;

public class ZLTextViewImpl extends ZLTextView {
	private final static String OPTIONS = "Options";
	// TODO: move these options to the FBReader code
	public final ZLIntegerRangeOption LeftMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "LeftMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption RightMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "RightMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption TopMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "TopMargin", 0, 1000, 0);
	public final ZLIntegerRangeOption BottomMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "BottomMargin", 0, 1000, 4);

	private ZLTextModel myModel;

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;
		
	private final List<ZLTextLineInfo> myLineInfos = new ArrayList<ZLTextLineInfo>();
	private final ArrayList<ZLTextElementArea> myTextElementMap = new ArrayList<ZLTextElementArea>();
	private final ArrayList<ZLTextTreeNodeArea> myTreeNodeMap = new ArrayList<ZLTextTreeNodeArea>();
	private final ZLTextWordCursor myIteratorCursor = new ZLTextWordCursor();

	// TO BE DELETED
	private ZLIntegerOption StartParagraphNumberOption =
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, "DummyScrolling", "Paragraph", 0);

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		resetTextStyle();
	}

	public void setModel(ZLTextModel model) {
		myModel = model;
	}
	protected ZLTextModel getModel() {
		return myModel;
	}

	private void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		getContext().setFont(myTextStyle.getFontFamily(), myTextStyle.getFontSize(), myTextStyle.bold(), myTextStyle.italic());
	}

	private void resetTextStyle() {
		setTextStyle(ZLTextStyleCollection.getInstance().getBaseStyle());
	}

	private void applyControl(ZLTextControlElement control) {
		if (control.IsStart) {
//			System.out.println("Apply Start " + control.Kind);
			ZLTextStyleDecoration decoration = ZLTextStyleCollection.getInstance().getDecoration(control.Kind);
			setTextStyle(decoration.createDecoratedStyle(myTextStyle));
//			if (decoration instanceof ZLTextFullStyleDecoration) {
//				System.out.println("FontSize = " + myTextStyle.getFontSize());
//			}
		} else {
//			System.out.println("Apply End " + control.Kind);
			if (myTextStyle.isDecorated()) {
				setTextStyle(((ZLTextDecoratedStyle) myTextStyle).getBase());
			}
		}
	}

	private void applyControls(ZLTextWordCursor begin, ZLTextWordCursor end) {
		for (ZLTextWordCursor cursor = begin; !cursor.equalWordNumber(end); cursor.nextWord()) {
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement) element);
			}	
		}
	}

	private int getElementWidth(ZLTextElement element, int charNumber) {
		if (element instanceof ZLTextWord) {
			return getWordWidth((ZLTextWord) element, charNumber, -1, false);
		} else if (element instanceof ZLTextImageElement) {
			return getContext().imageWidth(((ZLTextImageElement) element).getImage());
		} else if (element == ZLTextElement.IndentElement) {
			return myTextStyle.firstLineIndentDelta();
		}
		return 0; 
	}

	private int getElementHeight(ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			if (myWordHeight == -1) {
				myWordHeight = (int) (getContext().getStringHeight() * myTextStyle.lineSpace()) + myTextStyle.verticalShift();
			}
			return myWordHeight;
		} else if (element instanceof ZLTextImageElement) {
			return getContext().imageHeight(((ZLTextImageElement) element).getImage()) + 
				Math.max((int)(getContext().getStringHeight() * (myTextStyle.lineSpace() - 1)), 3);
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
		return getContext().getHeight() - topMargin() - bottomMargin();
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
		final ZLPaintContext context = getContext();
		context.clear(ZLTextStyleCollection.getInstance().getBaseStyle().BackgroundColorOption.getValue());

		if (myModel == null) {
			return;
		}

		myTextElementMap.clear();
		myTreeNodeMap.clear();
		int paragraphs = myModel.getParagraphsNumber();
		if (paragraphs > 0) {
			int pn = StartParagraphNumberOption.getValue();
			pn = Math.max(0, Math.min(pn, paragraphs - 2));
			StartParagraphNumberOption.setValue(pn);
			ZLTextParagraphCursor firstParagraph = ZLTextParagraphCursor.cursor(myModel, pn);
			ZLTextWordCursor start = new ZLTextWordCursor();
			start.setCursor(firstParagraph);
			buildInfos(start);
		}

		List<Integer> labels = new ArrayList<Integer>(myLineInfos.size() + 1);
		labels.add(0);
		context.moveYTo(topMargin());
		for (ZLTextLineInfo info : myLineInfos) {
			prepareTextLine(info);
			labels.add(myTextElementMap.size());
		}

		context.moveYTo(topMargin());
		int index = 0;
		for (ZLTextLineInfo info : myLineInfos) {
			drawTextLine(info, labels.get(index), labels.get(index + 1));
			index++;
			//System.out.println("Line " + index + " Y = " + context.getY());
		}

//		for (ZLTextElementArea area : myTextElementMap) {
//			System.out.println(area.XStart + " " + area.XEnd + " " + area.YStart + " " + area.YEnd);
//		}
		
/*		int h = 0;
		for (ZLTextLineInfo info : myLineInfos) {
			int w = 0;
			int spaces = 0;
			boolean wordOccurred = false;
			ZLTextWordCursor cursor;
			for (cursor = info.getStart(); !cursor.equalWordNumber(info.End); cursor.nextWord()) {
				ZLTextElement element = cursor.getElement();
				if (element == ZLTextElement.HSpace) {
					if (wordOccurred) {
						w += context.getSpaceWidth();
						spaces++;
						wordOccurred = false;
					}
				} else if (element instanceof ZLTextWord) {
					System.out.println("Word");
					wordOccurred = true;
					ZLTextWord word = (ZLTextWord)element;
					context.drawString(w, h + info.Height, word.Data, word.Offset, word.Length);
					w += word.getWidth(context);
				} else if (element instanceof ZLTextControlElement) {
					applyControl((ZLTextControlElement) element);			
				}
			}
			if (cursor.isEndOfParagraph()) {
				resetTextStyle();
			}
			System.out.println("Line over");
			h += info.Height + info.Descent;
		}	*/
	}

	private void drawTreeLines(ZLTextLineInfo.TreeNodeInfo info, int height, int vSpaceAfter) {
		final ZLPaintContext context = getContext();
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

	private void drawTextLine(ZLTextLineInfo info, final int from, final int to) {
		final ZLPaintContext context = getContext();
		final ZLTextParagraphCursor paragraph = info.RealStart.getParagraphCursor();
	
		ListIterator<ZLTextElementArea> fromIt = myTextElementMap.listIterator(from);
		ListIterator<ZLTextElementArea> toIt = myTextElementMap.listIterator(to);
		
		context.moveY(info.Height);
		int maxY = topMargin() + getTextAreaHeight();
		if (context.getY() > maxY) {
			context.moveYTo(maxY);
		}
		context.moveXTo(leftMargin());	
		if (info.NodeInfo != null) {
			drawTreeLines(info.NodeInfo, info.Height, info.Descent + info.VSpaceAfter);
		}

		ListIterator<ZLTextElementArea> it = myTextElementMap.listIterator(from);
		final ZLTextWordCursor end = info.End;
		for (myIteratorCursor.setCursor(info.RealStart); !myIteratorCursor.equalWordNumber(end); myIteratorCursor.nextWord()) {
			final ZLTextElement element = myIteratorCursor.getElement();//paragraph.getElement(myIteratorCursor.getWordNumber());
			if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.println("Word = " + ((ZLTextWord) element).getWord());
				ZLTextElementArea area = it.next();
				if (area.ChangeStyle) {
					setTextStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - getElementDescent(element) - myTextStyle.verticalShift();
				context.moveXTo(x);
				if (element instanceof ZLTextWord) {
					//System.out.println("Draw " + x + " " + y + " " + area.YEnd);
					drawWord(x, y, (ZLTextWord) element, myIteratorCursor.getCharNumber(), -1, false);
				} else {
					context.drawImage(x, y, ((ZLTextImageElement) element).getImage());
				}
			}
		}
		if (!(it.nextIndex() == toIt.nextIndex())) {
			ZLTextElementArea area = it.next();
			if (area.ChangeStyle) {
				setTextStyle(area.Style);
			}
			int len = info.End.getCharNumber();
			final ZLTextWord word = (ZLTextWord)info.End.getElement();
			final int x = area.XStart;
			final int y = area.YEnd - getElementDescent(word) - myTextStyle.verticalShift();
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

	private ZLTextWordCursor buildInfos(ZLTextWordCursor start) {
		myLineInfos.clear();
		final ZLTextWordCursor cursor = new ZLTextWordCursor(start);
		int textAreaHeight = getTextAreaHeight();
		int counter = 0;
		final ZLTextWordCursor paragraphEnd = new ZLTextWordCursor();
		final ZLTextWordCursor paragraphStart = new ZLTextWordCursor();
		do {
			paragraphEnd.setCursor(cursor);
			paragraphEnd.moveToParagraphEnd();
			paragraphStart.setCursor(cursor);
			paragraphStart.moveToParagraphStart();
		
			resetTextStyle();
			applyControls(paragraphStart, cursor);	
			ZLTextLineInfo info = new ZLTextLineInfo(cursor, myTextStyle);
			while (!info.End.isEndOfParagraph()) {
				info = processTextLine(info.End, paragraphEnd);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				cursor.setCursor(info.End);
				myLineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (cursor.isEndOfParagraph() && cursor.nextParagraph() && !cursor.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		resetTextStyle();
//		System.out.println("----------------------INFOS BUILT--------------------------------");
		return cursor;
	}

	private ZLTextLineInfo processTextLine(ZLTextWordCursor start, ZLTextWordCursor end) {
		final ZLPaintContext context = getContext();
		final ZLTextLineInfo info = new ZLTextLineInfo(start, myTextStyle);

		ZLTextWordCursor current = new ZLTextWordCursor(start);
		ZLTextParagraphCursor paragraphCursor = current.getParagraphCursor();
		final boolean isFirstLine = current.isStartOfParagraph();

		if (paragraphCursor.getParagraph().getKind() == ZLTextParagraph.Kind.TREE_PARAGRAPH) {
			final ZLTextTreeParagraph treeParagraph =
				(ZLTextTreeParagraph)paragraphCursor.getParagraph();
			boolean[] stack = new boolean[treeParagraph.getDepth() - 1];
			if (stack.length > 0) {
				ZLTextTreeParagraph ctp = treeParagraph;
				for (int index = 0; index < stack.length; ++index) {
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
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber());
			while (element instanceof ZLTextControlElement) {
				if (element instanceof ZLTextControlElement) {
					applyControl((ZLTextControlElement) element);
				}
				current.nextWord();
				if (current.equalWordNumber(end)) {
					break;
				}
				element = paragraphCursor.getElement(current.getWordNumber());
			}
			info.StartStyle = myTextStyle;
			info.RealStart.setCursor(current);
		}	

		ZLTextStyle storedStyle = myTextStyle;		
		
		info.LeftIndent = myTextStyle.leftIndent();	
		if (isFirstLine) {
			info.LeftIndent += myTextStyle.firstLineIndentDelta();
		}	
		if (info.NodeInfo != null) {
			info.LeftIndent +=
				(context.getStringHeight() + 2) / 3 *
				4 * (info.NodeInfo.VerticalLinesStack.length + 1);
		}
		
		info.Width = info.LeftIndent;
		
		final ZLTextWordCursor realStart = info.RealStart;
		if (realStart.equalWordNumber(end)) {
			info.End.setCursor(realStart);
			return info;
		}

		int newWidth = info.Width;
		int newHeight = info.Height;
		int newDescent = info.Descent;
		int maxWidth = context.getWidth() - leftMargin() - rightMargin() - myTextStyle.rightIndent();
		boolean wordOccurred = false;
		boolean isVisible = false;
		int lastSpaceWidth = 0;
		int internalSpaceCounter = 0;
		boolean removeLastSpace = false;

		do {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber()); 
			newWidth += getElementWidth(element, current.getCharNumber());
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
			if ((newWidth > maxWidth) && !info.End.equalWordNumber(start)) {
				break;
			}
			ZLTextElement previousElement = element;
			current.nextWord();
			boolean allowBreak = current.equalWordNumber(end);
			if (!allowBreak) {
				element = current.getElement(); 
				allowBreak = (((!(element instanceof ZLTextWord)) || (previousElement instanceof ZLTextWord)) && 
						!(element instanceof ZLTextImageElement) && !(element instanceof ZLTextControlElement));
			}
			if (allowBreak) {
				info.IsVisible = isVisible;
				info.Width = newWidth;
				info.Height = Math.max(info.Height, newHeight);
				info.Descent = Math.max(info.Descent, newDescent);
				info.End.setCursor(current);
				storedStyle = myTextStyle;
				info.SpaceCounter = internalSpaceCounter;
				removeLastSpace = !wordOccurred && (info.SpaceCounter > 0);
			}	
		} while (!current.equalWordNumber(end));

/*		if (!current.equalWordNumber(end)) {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber());
			if (element instanceof ZLTextWord) { 
				newWidth -= getElementWidth(element, current.getCharNumber());
			}
			info.IsVisible = true;
			info.Width = newWidth;
			info.Height = Math.max(info.Height, newHeight);
			info.Descent = Math.max(info.Descent, newDescent);
			info.End.setCursor(current);
			info.SpaceCounter = internalSpaceCounter;
		}*/
		
		if (removeLastSpace) {
			info.Width -= lastSpaceWidth;
			info.SpaceCounter--;
		}

		setTextStyle(storedStyle);

		if (isFirstLine) {
			info.Height += info.StartStyle.spaceBefore();
		}
		if (info.End.isEndOfParagraph()) {
			info.VSpaceAfter = myTextStyle.spaceAfter();
		}		

		//System.out.println();
		//System.out.println("Info widht = " + info.Width);

	//	System.out.println(info.End.getElement());

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info) {
		final ZLPaintContext context = getContext();

		setTextStyle(info.StartStyle);
		final int y = Math.min(context.getY() + info.Height, topMargin() + getTextAreaHeight());
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.End.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		context.moveXTo(leftMargin() + info.LeftIndent);
		//System.out.println(context.getWidth() + " " + info.Width);
		final int maxWidth = context.getWidth() - leftMargin() - rightMargin();
		switch (myTextStyle.alignment()) {
			case ZLTextAlignmentType.ALIGN_RIGHT: {
				context.moveX(maxWidth - myTextStyle.rightIndent() - info.Width);
				break;
			} 
			case ZLTextAlignmentType.ALIGN_CENTER: {
				context.moveX((maxWidth - myTextStyle.rightIndent() - info.Width) / 2);
				break;
			} 
			case ZLTextAlignmentType.ALIGN_JUSTIFY: {
				if (!endOfParagraph && !(info.End.getElement() == ZLTextElement.AfterParagraph)) {
					fullCorrection = maxWidth - myTextStyle.rightIndent() - info.Width;
				}
				break;
			}
			case ZLTextAlignmentType.ALIGN_LEFT: 
			case ZLTextAlignmentType.ALIGN_UNDEFINED: {
				break;
			}
		}
	
		final ZLTextParagraphCursor paragraph = info.RealStart.getParagraphCursor();
		int paragraphNumber = paragraph.getIndex();
//		System.out.println();
		final ZLTextWordCursor end = info.End;
		for (myIteratorCursor.setCursor(info.RealStart); !myIteratorCursor.equalWordNumber(end); myIteratorCursor.nextWord()) {
			final ZLTextElement element = paragraph.getElement(myIteratorCursor.getWordNumber());
			final int x = context.getX();
			final int width = getElementWidth(element, myIteratorCursor.getCharNumber());
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					int correction = fullCorrection / spaceCounter;
					context.moveX(context.getSpaceWidth() + correction);
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}	
			} else 	if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.print(((ZLTextWord) element).Data + " " + x + " ");
				final int height = getElementHeight(element);
				final int descent = getElementDescent(element);
				final int length = (element instanceof ZLTextWord) ? ((ZLTextWord) element).Length : 0;
				myTextElementMap.add(new ZLTextElementArea(paragraphNumber, myIteratorCursor.getWordNumber(), myIteratorCursor.getCharNumber(), 
					length, false, changeStyle, myTextStyle, element, x, x + width - 1, y - height + 1, y + descent));
				changeStyle = false;
				wordOccurred = true;
			} else if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement) element);
				changeStyle = true;
			}
			context.moveX(width);
		}
		context.moveY(info.Height + info.Descent + info.VSpaceAfter);
	}
	
	public String caption() {
		return "SampleView";
	}

	// TO BE DELETED
	public void scroll(int numberOfParagraphs) {
		StartParagraphNumberOption.setValue(StartParagraphNumberOption.getValue() + numberOfParagraphs);
	}

	public void gotoParagraph(int index) {
		// TODO: implement
		StartParagraphNumberOption.setValue(index);
	}

	public int leftMargin() {
		return LeftMarginOption.getValue();
	}

	public int rightMargin() {
		return LeftMarginOption.getValue();
	}

	public int topMargin() {
		return LeftMarginOption.getValue();
	}

	public int bottomMargin() {
		return LeftMarginOption.getValue();
	}

	protected int paragraphIndexByCoordinate(int y) {
		ZLTextElementArea area = ZLTextRectangularArea.binarySearch(myTextElementMap, y);
		return (area != null) ? area.ParagraphNumber : -1;
	}

	public boolean onStylusPress(int x, int y) {
		if (myModel instanceof ZLTextTreeModel) {
			ZLTextTreeNodeArea nodeArea = ZLTextRectangularArea.binarySearch(myTreeNodeMap, x, y);
			if (nodeArea != null) {
				final int index = nodeArea.ParagraphNumber;
				final ZLTextTreeParagraph paragraph = ((ZLTextTreeModel)myModel).getParagraph(index);
				paragraph.open(!paragraph.isOpen());
				/*
				rebuildPaintInfo(true);
				preparePaintInfo();
				if (paragraph->isOpen()) {
					int nextParagraphNumber = paragraphNumber + paragraph->fullSize();
					int lastParagraphNumber = endCursor().paragraphCursor().index();
					if (endCursor().isEndOfParagraph()) {
						++lastParagraphNumber;
					}
					if (lastParagraphNumber < nextParagraphNumber) {
						gotoParagraph(nextParagraphNumber, true);
						preparePaintInfo();
					}
				}
				int firstParagraphNumber = startCursor().paragraphCursor().index();
				if (startCursor().isStartOfParagraph()) {
					--firstParagraphNumber;
				}
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
