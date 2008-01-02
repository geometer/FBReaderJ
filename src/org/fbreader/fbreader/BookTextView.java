package org.fbreader.fbreader;

import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.impl.ZLTextWordCursor;
import org.zlibrary.text.view.impl.ZLTextParagraphCursor;

class BookTextView extends FBView {
	private ZLIntegerOption myParagraphNumberOption;
	private ZLIntegerOption myWordNumberOption;
	private ZLIntegerOption myCharNumberOption;
	BookTextView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);
		myParagraphNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Paragraph", 0);
		myWordNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Word", 0);
		myCharNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Char", 0);
		gotoPosition(myParagraphNumberOption.getValue(), myWordNumberOption.getValue(), myCharNumberOption.getValue());
	}

	protected void preparePaintInfo() {
		super.preparePaintInfo();
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull()) {
			myParagraphNumberOption.setValue(cursor.getParagraphCursor().getIndex());
			myWordNumberOption.setValue(cursor.getWordNumber());
			myCharNumberOption.setValue(cursor.getCharNumber());
		}
	}

	void scrollToHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().getIndex() == 0) {
			return;
		}
	  //gotoParagraph(0, false);
		gotoPosition(0, 0, 0);
	  getApplication().refreshWindow();
	}

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}
		return false;
	}
}
