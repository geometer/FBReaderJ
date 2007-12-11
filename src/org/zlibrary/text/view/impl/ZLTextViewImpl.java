package org.zlibrary.text.view.impl;

import java.util.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.options.*;

import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.entry.*;

import org.fbreader.formats.fb2.*;

import org.zlibrary.text.view.*;
import org.zlibrary.text.view.style.*;

public class ZLTextViewImpl extends ZLTextView {
	private final static String OPTIONS = "Options";
	public final ZLIntegerRangeOption LeftMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "LeftMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption RightMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "RightMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption TopMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "TopMargin", 0, 1000, 0);
	public final ZLIntegerRangeOption BottomMarginOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTIONS, "BottomMargin", 0, 1000, 4);

	private class ViewStyle {
		private ZLTextStyle myStyle;
		private ZLPaintContext myContext;
		private int myWordHeight;
		
		public ViewStyle(ZLPaintContext context) {
			myContext = context;
			setStyle(ZLTextStyleCollection.getInstance().getBaseStyle());
			myWordHeight = -1;
		}

		public void reset() {
			setStyle(ZLTextStyleCollection.getInstance().getBaseStyle());
		}
		
		public void setStyle(ZLTextStyle style) {
			if (myStyle != style) {
				myStyle = style;
				myWordHeight = -1;
			}
			myContext.setFont(myStyle.getFontFamily(), myStyle.getFontSize(), myStyle.bold(), myStyle.italic());
		}

		public void applyControl(ZLTextControlElement control) {
			if (control.isStart()) {
//				System.out.println("Apply Start " + control.getTextKind());
				ZLTextStyleDecoration decoration = ZLTextStyleCollection.getInstance().getDecoration(control.getTextKind());
				setStyle(decoration.createDecoratedStyle(myStyle));
//				if (decoration instanceof ZLTextFullStyleDecoration) {
//					System.out.println("FontSize = " + myStyle.getFontSize());
//				}
			} else {
//				System.out.println("Apply End " + control.getTextKind());
				if (myStyle.isDecorated()) {
					setStyle(((ZLTextDecoratedStyle) myStyle).getBase());
				}
			}
		}

		public void applyControls(ZLTextWordCursor begin, ZLTextWordCursor end) {
			for (ZLTextWordCursor cursor = begin; !cursor.equalWordNumber(end); cursor.nextWord()) {
				final ZLTextElement element = cursor.getElement();
				if (element instanceof ZLTextControlElement) {
					applyControl((ZLTextControlElement) element);
				}	
			}
		}

		public ZLPaintContext getPaintContext() {
			return myContext;
		}
	
		public ZLTextStyle getTextStyle() {
			return myStyle;
		}

		public int getElementWidth(ZLTextElement element, int charNumber) {
			if (element instanceof ZLTextWord) {
				return getWordWidth((ZLTextWord) element, charNumber, -1, false);
			} else if (element instanceof ZLTextImageElement) {
				return myContext.imageWidth(((ZLTextImageElement) element).getImage());
			} else if (element == ZLTextElement.IndentElement) {
				return myStyle.firstLineIndentDelta();
			}
			return 0; 
		}

		public int getElementHeight(ZLTextElement element) {
			if (element instanceof ZLTextWord) {
				if (myWordHeight == -1) {
					myWordHeight = (int) (myContext.getStringHeight() * myStyle.lineSpace()) + myStyle.verticalShift();
				}
				return myWordHeight;
			} else if (element instanceof ZLTextImageElement) {
				return myContext.imageHeight(((ZLTextImageElement) element).getImage()) + 
					Math.max(myContext.getStringHeight() * (myStyle.lineSpace() - 1), 3);
			}
			return 0;
		}
		
		public int getElementDescent(ZLTextElement element) {
			if (element instanceof ZLTextWord) {
				return myContext.getDescent();
			}
			return 0;
		}

		public int getTextAreaHeight() {
			return myContext.getHeight() - topMargin() - bottomMargin();
		}

		public int getWordWidth(ZLTextWord word) {
			return word.getWidth(myContext);
		}
		
		public int getWordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
			if (start == 0 && length == -1) {
				return word.getWidth(myContext);
			}	
			return 0;
		}
	}

	private ZLTextModel myModel;
	private ViewStyle myStyle;
	private List<ZLTextLineInfo> myLineInfos;
	private List<ZLTextElementArea> myTextElementMap = new ArrayList<ZLTextElementArea> ();

	// TO BE DELETED
	private ZLIntegerOption StartParagraphNumberOption =
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, "DummyScrolling", "Paragraph", 0);

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		myStyle = new ViewStyle(context);
		myLineInfos = new ArrayList<ZLTextLineInfo> ();
	}

	public void setModel(ZLTextModel model) {
		/*
		myModel = new BookModel();
		ZLModelFactory factory = new ZLModelFactory();
		ZLTextPlainModel model = myModel.getBookModel();
		ZLTextParagraph paragraph = factory.createParagraph();
		paragraph.addEntry(factory.createTextEntry("default style"));
		paragraph.addEntry(factory.createControlEntry((byte) 28, true));
		paragraph.addEntry(factory.createTextEntry(" bold "));
		paragraph.addEntry(factory.createControlEntry((byte) 28, false));
		paragraph.addEntry(factory.createTextEntry("default style again "));
		paragraph.addEntry(factory.createControlEntry((byte) 31, true));
		paragraph.addEntry(factory.createTextEntry("large font now"));
		paragraph.addEntry(factory.createControlEntry((byte) 31, false));
		paragraph.addEntry(factory.createTextEntry(" default style once more"));
		model.addParagraphInternal(paragraph);
		*/
/*		model.addText("default style");
		model.addControl((byte) 42, true);
		model.addText("bold");
		model.addControl((byte) 42, false);
		model.addText("default again");*/
		myModel = model;
	}

	public void setModel(String fileName) {
		setModel(new FB2Reader().readBook(fileName).getBookModel());
	}

	public void paint() {
		ZLPaintContext context = getContext();
		myTextElementMap.clear();
		int paragraphs = myModel.getParagraphsNumber();
		if (paragraphs > 0) {
			int pn = StartParagraphNumberOption.getValue();
			pn = Math.max(0, Math.min(pn, paragraphs - 2));
			StartParagraphNumberOption.setValue(pn);
			ZLTextParagraphCursor firstParagraph = ZLTextParagraphCursor.getCursor(myModel, pn);
			ZLTextWordCursor start = new ZLTextWordCursor();
			start.setCursor(firstParagraph);
			buildInfos(start);
		}

		List<Integer> labels = new ArrayList<Integer>(myLineInfos.size() + 1);
		labels.add(0);
		getContext().moveYTo(topMargin());
		for (ZLTextLineInfo info : myLineInfos) {
			prepareTextLine(info);
			labels.add(myTextElementMap.size());
		}

		getContext().moveYTo(topMargin());
		int index = 0;
		for (ZLTextLineInfo info : myLineInfos) {
			drawTextLine(info, labels.get(index), labels.get(index + 1));
			index++;
			//System.out.println("Line " + index + " Y = " + getContext().getY());
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
			for (cursor = info.Start; !cursor.equalWordNumber(info.End); cursor.nextWord()) {
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
					myStyle.applyControl((ZLTextControlElement) element);			
				}
			}
			if (cursor.isEndOfParagraph()) {
				myStyle.reset();
			}
			System.out.println("Line over");
			h += info.Height + info.Descent;
		}	*/
	}

	private void drawTextLine(ZLTextLineInfo info, final int from, final int to) {
		final ZLTextParagraphCursor paragraph = info.RealStart.getParagraphCursor();
	
		ListIterator<ZLTextElementArea> fromIt = myTextElementMap.listIterator(from);
		ListIterator<ZLTextElementArea> toIt = myTextElementMap.listIterator(to);
		
		getContext().moveY(info.Height);
		int maxY = topMargin() + myStyle.getTextAreaHeight();
		if (getContext().getY() > maxY) {
			getContext().moveYTo(maxY);
		}
		getContext().moveXTo(leftMargin());	
		
		ListIterator<ZLTextElementArea> it = myTextElementMap.listIterator(from);
		for (ZLTextWordCursor pos = new ZLTextWordCursor(info.RealStart); !pos.equalWordNumber(info.End); pos.nextWord()) {
			final ZLTextElement element = pos.getElement();//paragraph.getElement(pos.getWordNumber());
			if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.println("Word = " + ((ZLTextWord) element).getWord());
				ZLTextElementArea area = it.next();
				if (area.ChangeStyle) {
					myStyle.setStyle(area.Style);
				}
				final int x = area.XStart;
				final int y = area.YEnd - myStyle.getElementDescent(element) - myStyle.getTextStyle().verticalShift();
				getContext().moveXTo(x);
				if (element instanceof ZLTextWord) {
					//System.out.println("Draw " + x + " " + y + " " + area.YEnd);
					drawWord(x, y, (ZLTextWord) element, pos.getCharNumber(), -1, false);
				} else {
					getContext().drawImage(x, y, ((ZLTextImageElement) element).getImage());
				}
			}
		}
		if (!(it.nextIndex() == toIt.nextIndex())) {
			ZLTextElementArea area = it.next();
			if (area.ChangeStyle) {
				myStyle.setStyle(area.Style);
			}
			int len = info.End.getCharNumber();
			final ZLTextWord word = (ZLTextWord) info.End.getElement();
			final int x = area.XStart;
			final int y = area.YEnd - myStyle.getElementDescent(word) - myStyle.getTextStyle().verticalShift();
			drawWord(x, y, word, 0, len, area.AddHyphenationSign);
		}
		getContext().moveY(info.Descent + info.VSpaceAfter);
	}

	private void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if ((start == 0) && (length == -1)) {
			getContext().drawString(x, y, word.Data, word.Offset, word.Length);
		} else {
			System.out.println("Shouldn't be here - no hyphenations supported yet.");
		}
	}

	private ZLTextWordCursor buildInfos(ZLTextWordCursor start) {
		myLineInfos.clear();
		ZLTextWordCursor cursor = start;
		int textAreaHeight = myStyle.getTextAreaHeight();
		int counter = 0;
		do {
			ZLTextWordCursor paragraphEnd = new ZLTextWordCursor(cursor);
		       	paragraphEnd.moveToParagraphEnd();
			ZLTextWordCursor paragraphStart = new ZLTextWordCursor(cursor);
		       	paragraphStart.moveToParagraphStart();
		
			myStyle.reset();
			myStyle.applyControls(paragraphStart, cursor);	
			ZLTextLineInfo info = new ZLTextLineInfo(cursor, myStyle.getTextStyle());
			while (!info.End.isEndOfParagraph()) {
				info = processTextLine(info.End, paragraphEnd);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				cursor = new ZLTextWordCursor(info.End);
				myLineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (cursor.isEndOfParagraph() && cursor.nextParagraph() && !cursor.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		myStyle.reset();
//		System.out.println("----------------------INFOS BUILT--------------------------------");
		return cursor;
	}

	private ZLTextLineInfo processTextLine(ZLTextWordCursor start, ZLTextWordCursor end) {
		ZLTextLineInfo info = new ZLTextLineInfo(start, myStyle.getTextStyle());

		ZLTextWordCursor current = new ZLTextWordCursor(start);
		ZLTextParagraphCursor paragraphCursor = current.getParagraphCursor();
		boolean isFirstLine = current.isStartOfParagraph();
	
		if (isFirstLine) {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber());
			while (element instanceof ZLTextControlElement) {
				if (element instanceof ZLTextControlElement) {
					myStyle.applyControl((ZLTextControlElement) element);
				}
				current.nextWord();
				if (current.equalWordNumber(end)) {
					break;
				}
				element = paragraphCursor.getElement(current.getWordNumber());
			}
			info.StartStyle = myStyle.getTextStyle();
			info.RealStart = new ZLTextWordCursor(current);
		}	

		ZLTextStyle storedStyle = myStyle.getTextStyle();		
		
		info.LeftIndent = myStyle.getTextStyle().leftIndent();	
		if (isFirstLine) {
			info.LeftIndent += myStyle.getTextStyle().firstLineIndentDelta();
		}		
		
		info.Width = info.LeftIndent;
		
		if (info.RealStart.equalWordNumber(end)) {
			info.End = new ZLTextWordCursor(info.RealStart);
			return info;
		}

		int newWidth = info.Width;
		int newHeight = info.Height;
		int newDescent = info.Descent;
		int maxWidth = myStyle.getPaintContext().getWidth() - leftMargin() - rightMargin() - myStyle.getTextStyle().rightIndent();
		boolean wordOccurred = false;
		boolean isVisible = false;
		int lastSpaceWidth = 0;
		int internalSpaceCounter = 0;
		boolean removeLastSpace = false;

		do {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber()); 
			newWidth += myStyle.getElementWidth(element, current.getCharNumber());
			newHeight = Math.max(newHeight, myStyle.getElementHeight(element));
			newDescent = Math.max(newDescent, myStyle.getElementDescent(element));
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred) {
					wordOccurred = false;
					internalSpaceCounter++;
					lastSpaceWidth = myStyle.getPaintContext().getSpaceWidth();
					newWidth += lastSpaceWidth;
				}
			} else if (element instanceof ZLTextWord) {
				wordOccurred = true;
				isVisible = true;
				//System.out.println("Word = " + ((ZLTextWord) element).Data + " FontSize = " + myStyle.getTextStyle().getFontSize());
			} else if (element instanceof ZLTextControlElement) {
				myStyle.applyControl((ZLTextControlElement) element);
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
				info.End = new ZLTextWordCursor(current);
				storedStyle = myStyle.getTextStyle();
				info.SpaceCounter = internalSpaceCounter;
				removeLastSpace = !wordOccurred && (info.SpaceCounter > 0);
			}	
		} while (!current.equalWordNumber(end));

/*		if (!current.equalWordNumber(end)) {
			ZLTextElement element = paragraphCursor.getElement(current.getWordNumber());
			if (element instanceof ZLTextWord) { 
				newWidth -= myStyle.getElementWidth(element, current.getCharNumber());
			}
			info.IsVisible = true;
			info.Width = newWidth;
			info.Height = Math.max(info.Height, newHeight);
			info.Descent = Math.max(info.Descent, newDescent);
			info.End = new ZLTextWordCursor(current);
			info.SpaceCounter = internalSpaceCounter;
		}*/
		
		if (removeLastSpace) {
			info.Width -= lastSpaceWidth;
			info.SpaceCounter--;
		}

		myStyle.setStyle(storedStyle);

		if (isFirstLine) {
			info.Height += info.StartStyle.spaceBefore();
		}
		if (info.End.isEndOfParagraph()) {
			info.VSpaceAfter = myStyle.getTextStyle().spaceAfter();
		}		

		//System.out.println();
		//System.out.println("Info widht = " + info.Width);

	//	System.out.println(info.End.getElement());

		return info;	
	}

	private void prepareTextLine(ZLTextLineInfo info) {
		myStyle.setStyle(info.StartStyle);
		final int y = Math.min(getContext().getY() + info.Height, topMargin() + myStyle.getTextAreaHeight());
		int spaceCounter = info.SpaceCounter;
		int fullCorrection = 0;
		final boolean endOfParagraph = info.End.isEndOfParagraph();
		boolean wordOccurred = false;
		boolean changeStyle = true;

		getContext().moveXTo(leftMargin() + info.LeftIndent);
		//System.out.println(getContext().getWidth() + " " + info.Width);
		final int maxWidth = getContext().getWidth() - leftMargin() - rightMargin();
		switch (myStyle.getTextStyle().alignment()) {
			case ZLTextAlignmentType.ALIGN_RIGHT: {
				getContext().moveX(maxWidth - myStyle.getTextStyle().rightIndent() - info.Width);
				break;
			} 
			case ZLTextAlignmentType.ALIGN_CENTER: {
				getContext().moveX((maxWidth - myStyle.getTextStyle().rightIndent() - info.Width) / 2);
				break;
			} 
			case ZLTextAlignmentType.ALIGN_JUSTIFY: {
				if (!endOfParagraph && !(info.End.getElement() == ZLTextElement.AfterParagraph)) {
					fullCorrection = maxWidth - myStyle.getTextStyle().rightIndent() - info.Width;
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
		for (ZLTextWordCursor pos = new ZLTextWordCursor(info.RealStart); !pos.equalWordNumber(info.End); pos.nextWord()) {
			final ZLTextElement element = paragraph.getElement(pos.getWordNumber());
			final int x = getContext().getX();
			final int width = myStyle.getElementWidth(element, pos.getCharNumber());
			if (element == ZLTextElement.HSpace) {
				if (wordOccurred && (spaceCounter > 0)) {
					int correction = fullCorrection / spaceCounter;
					getContext().moveX(getContext().getSpaceWidth() + correction);
					fullCorrection -= correction;
					wordOccurred = false;
					--spaceCounter;
				}	
			} else 	if ((element instanceof ZLTextWord) || (element instanceof ZLTextImageElement)) {
				//System.out.print(((ZLTextWord) element).Data + " " + x + " ");
				final int height = myStyle.getElementHeight(element);
				final int descent = myStyle.getElementDescent(element);
				final int length = (element instanceof ZLTextWord) ? ((ZLTextWord) element).Length : 0;
				myTextElementMap.add(new ZLTextElementArea(paragraphNumber, pos.getWordNumber(), pos.getCharNumber(), 
					length, false, changeStyle, myStyle.getTextStyle(), element, x, x + width - 1, y - height + 1, y + descent));
				changeStyle = false;
				wordOccurred = true;
			} else if (element instanceof ZLTextControlElement) {
				myStyle.applyControl((ZLTextControlElement) element);
				changeStyle = true;
			}
			getContext().moveX(width);
		}
		getContext().moveY(info.Height + info.Descent + info.VSpaceAfter);
	}
	
	public String caption() {
		return "SampleView";
	}

	// TO BE DELETED
	public void scroll(int numberOfParagraphs) {
		StartParagraphNumberOption.setValue(StartParagraphNumberOption.getValue() + numberOfParagraphs);
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
}
