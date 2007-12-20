package org.fbreader.fbreader;

import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.impl.ZLTextViewImpl;

class BookTextView extends ZLTextViewImpl {
	private final ZLIntegerOption ParagraphPositionOption =
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, "DummyScrolling", "Paragraph", 0);
	BookTextView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		gotoParagraph(ParagraphPositionOption.getValue());
	}

	public void gotoParagraph(int index) {
		super.gotoParagraph(index);
		ParagraphPositionOption.setValue(index);
	}
}
