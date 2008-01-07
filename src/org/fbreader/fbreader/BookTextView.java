package org.fbreader.fbreader;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.impl.*;
import org.fbreader.bookmodel.FBTextKind;

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

		ZLTextElementArea area = getElementByCoordinates(x, y);
		if (area != null) {
			ZLTextElement element = area.Element;
			if ((element instanceof ZLTextImageElement) ||
					(element instanceof ZLTextWord)) {
				final ZLTextWordCursor cursor = new ZLTextWordCursor(getStartCursor());
				cursor.moveToParagraph(area.ParagraphNumber);
				cursor.moveToParagraphStart();
				final int elementNumber = area.TextElementNumber;
				byte hyperlinkKind = FBTextKind.REGULAR;
				String id = null;
				for (int i = 0; i < elementNumber; ++i) {
					ZLTextElement e = cursor.getElement();
					if (e instanceof ZLTextControlElement) {
						if (e instanceof ZLTextHyperlinkControlElement) {
							final ZLTextHyperlinkControlElement control = (ZLTextHyperlinkControlElement)e;
							hyperlinkKind = control.Kind;
							id = control.Label;
						} else {
							final ZLTextControlElement control = (ZLTextControlElement)e;
							if (!control.IsStart && (control.Kind == hyperlinkKind)) {
								hyperlinkKind = FBTextKind.REGULAR;
								id = null;
							}
						}
					}
					cursor.nextWord();
				}
				if (id != null) {
					switch (hyperlinkKind) {
						case FBTextKind.EXTERNAL_HYPERLINK:
							ZLibrary.getInstance().openInBrowser(id);
							return true;
						case FBTextKind.FOOTNOTE:
						case FBTextKind.INTERNAL_HYPERLINK:
							getFBReader().tryOpenFootnote(id);
							return true;
					}
				}
			}
		}
		return false;
	}
}
