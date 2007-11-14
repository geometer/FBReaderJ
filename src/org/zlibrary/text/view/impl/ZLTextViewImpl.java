package org.zlibrary.text.view.impl;

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

	public ZLTextViewImpl(ZLApplication application, ZLPaintContext context) {
		super(application, context);
		myStyle = new ViewStyle(context);
	}

	public void setModel(String fileName) {
		myModel = new FB2Reader(fileName).read();
	}

	public void paint() {
		ZLPaintContext context = getContext();

		ZLTextModel model = myModel.getBookModel();
		int paragraphs = model.getParagraphsNumber();
		int h = 0;
		int dh = context.stringHeight();
		for (int i = 0; i < paragraphs; i++) {
			ZLTextParagraphCursor cursor = ZLTextParagraphCursor.getCursor(model, i);
			for (int j = 0; j < cursor.getParagraphLength(); j++) {
				ZLTextElement element = cursor.getElement(j);
				if (element instanceof ZLTextWord) {
					String text = ((ZLTextWord) element).Data;
					final int w = context.stringWidth(text);
					h += dh;
					context.drawString((context.getWidth() - w) / 2, h, text);
				}	
			}
			h += dh;	
		}
	}

	public String caption() {
		return "SampleView";
	}
}
