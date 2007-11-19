package org.zlibrary.text.view.impl;

import java.util.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;

import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.entry.*;

import org.fbreader.bookmodel.*;
import org.fbreader.formats.fb2.*;

import org.zlibrary.text.view.*;
import org.zlibrary.text.view.style.ZLTextBaseStyle;

public class ZLTextViewImpl extends ZLTextView {
	private static class ViewStyle {
		private ZLTextStyle myStyle;
		private ZLPaintContext myContext;
		private int myWordHeight;
		
		public ViewStyle(ZLPaintContext context) {
			myContext = context;
			setStyle(new ZLTextBaseStyle());
			myWordHeight = -1;
		}

		public void setStyle(ZLTextStyle style) {
			if (myStyle != style) {
				myStyle = style;
				myWordHeight = -1;
			}
			myContext.setFont(myStyle.fontFamily(), myStyle.fontSize(), myStyle.bold(), myStyle.italic());
		}

/*		public void applyControl(ZLTextControlElement control) {
		}

*/
		public ZLPaintContext getPaintContext() {
			return myContext;
		}
	
		public ZLTextStyle getTextStyle() {
			return myStyle;
		}

/*		public int elementWidth(ZLTextElement element, int charNumber) {
		}

		public int elementHeight(ZLTextElement element) {
		}
*/		
		public int textAreaHeight() {
			return myContext.getHeight();
		}

		public int wordWidth(ZLTextWord word) {
			return word.getWidth(myContext);
		}
		
/*		public int wordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		}*/
	}

	private BookModel myModel;
	private ViewStyle myStyle;
	private List<ZLTextLineInfo> myLineInfos;

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		myStyle = new ViewStyle(context);
		myLineInfos = new ArrayList<ZLTextLineInfo> ();
	}

	public void setModel(String fileName) {
		myModel = new FB2Reader(fileName).read();
	}

	public void paint() {
		ZLPaintContext context = getContext();

		ZLTextModel model = myModel.getBookModel();
		int paragraphs = model.getParagraphsNumber();
		int h = 0;
		int dh = context.getStringHeight();
		for (int i = 0; i < paragraphs; i++) {
			ZLTextParagraphCursor cursor = ZLTextParagraphCursor.getCursor(model, i);
			for (int j = 0; j < cursor.getParagraphLength(); j++) {
				ZLTextElement element = cursor.getElement(j);
				if (element instanceof ZLTextWord) {
					String text = ((ZLTextWord) element).Data;
					final int w = context.getStringWidth(text);
					h += dh;
					context.drawString((context.getWidth() - w) / 2, h, text);
				}	
			}
			h += dh;	
		}
	}

	private ZLTextWordCursor buildInfos(ZLTextWordCursor start) {
		myLineInfos.clear();
		ZLTextWordCursor cursor = start;
		int textAreaHeight = myStyle.textAreaHeight();
		int counter = 0;
		do {
			ZLTextWordCursor paragraphEnd = cursor;
		       	paragraphEnd.moveToParagraphEnd();
			ZLTextWordCursor paragraphStart = cursor;
		       	paragraphStart.moveToParagraphStart();
		
		//	myStyle.reset();
		//	myStyle.applyControl(paragraphStart, cursor);	
			ZLTextLineInfo info = new ZLTextLineInfo(cursor, myStyle.getTextStyle());
			while (!info.End.isEndOfParagraph()) {
				info = processTextLine(info.End, paragraphEnd);
				textAreaHeight -= info.Height + info.Descent;
				if ((textAreaHeight < 0) && (counter > 0)) {
					break;
				}
				textAreaHeight -= info.VSpaceAfter;
				cursor = info.End;
				myLineInfos.add(info);
				if (textAreaHeight < 0) {
					break;
				}
				counter++;
			}
		} while (cursor.isEndOfParagraph() && cursor.nextParagraph() && !cursor.getParagraphCursor().isEndOfSection() && (textAreaHeight >= 0));
		return cursor;
	}

	private ZLTextLineInfo processTextLine(ZLTextWordCursor start, ZLTextWordCursor end) {
		return null;
	}

	public String caption() {
		return "SampleView";
	}
}
