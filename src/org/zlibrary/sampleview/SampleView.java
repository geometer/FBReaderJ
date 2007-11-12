package org.zlibrary.sampleview;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;

import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.entry.*;

import org.zlibrary.text.view.impl.*;

class SampleView extends ZLView {
	SampleView(SampleApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public void paint() {
		ZLPaintContext context = getContext();
		ZLModelFactory modelFactory = new ZLModelFactory();
		ZLTextModel model = modelFactory.createPlainModel();
		ZLTextParagraph paragraph = modelFactory.createParagraph();
		ZLTextEntry entry;
		entry = modelFactory.createTextEntry("griffon");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("griffon");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("griffon");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("griffon");
		paragraph.addEntry(entry);
		model.addParagraphInternal(paragraph);

		paragraph = modelFactory.createParagraph();
		entry = modelFactory.createTextEntry("42");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("22");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("42");
		paragraph.addEntry(entry);
		entry = modelFactory.createTextEntry("22");
		paragraph.addEntry(entry);
		model.addParagraphInternal(paragraph);

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
