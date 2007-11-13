package org.zlibrary.sampleview;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;

import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.entry.*;

import org.zlibrary.text.view.impl.*;

import org.fbreader.bookmodel.*;
import org.fbreader.formats.fb2.*;

class SampleView extends ZLView {
	private BookModel myModel;

	void setModel(String fileName) {
		myModel = new FB2Reader(fileName).read();
	}

	SampleView(SampleApplication application, ZLPaintContext context) {
		super(application, context);

		/*
		ZLModelFactory modelFactory = new ZLModelFactory();
		myModel = modelFactory.createPlainModel();
		ZLTextParagraph paragraph = modelFactory.createParagraph();
		ZLTextEntry entry;
		entry = modelFactory.createTextEntry("First paragraph:  Some text here ");
		paragraph.addEntry(entry);
		myModel.addParagraphInternal(paragraph);

		paragraph = modelFactory.createParagraph();
		entry = modelFactory.createTextEntry("  Second paragraph: ");
		paragraph.addEntry(entry);
		myModel.addParagraphInternal(paragraph);
		*/
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
					String text = ((ZLTextWord) element).myData;
					final int w = context.stringWidth(text);
					h += dh;
					context.drawString((context.getWidth() - w) / 2, h, text);
				}	
			}
			h += dh;	
		}

/*		String text = "42";
		final int w = context.stringWidth(text);
		context.drawString((context.getWidth() - w) / 2, context.stringHeight(), text);
*/	
	}

	public String caption() {
		return "SampleView";
	}
}
