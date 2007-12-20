package org.fbreader.fbreader;

import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.impl.ZLTextViewImpl;

class BookTextView extends ZLTextViewImpl {
	private ZLIntegerOption myParagraphPositionOption;
	BookTextView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);
		myParagraphPositionOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Paragraph", 0);
		gotoParagraph(myParagraphPositionOption.getValue());
	}

	public void gotoParagraph(int index) {
		super.gotoParagraph(index);
		myParagraphPositionOption.setValue(index);
	}
}
