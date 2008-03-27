package org.fbreader.fbreader;

import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.*;
import org.zlibrary.text.view.impl.ZLTextWordCursor;

import org.fbreader.bookmodel.ContentsModel;

class ContentsView extends FBView {
	ContentsView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		final int index = getParagraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		final ContentsModel contentsModel = (ContentsModel)getModel();
		final ZLTextTreeParagraph paragraph = contentsModel.getTreeParagraph(index);
		final int reference = contentsModel.getReference(paragraph);

		FBReader fbreader = (FBReader)getApplication();
		fbreader.getBookTextView().gotoPosition(reference, 0, 0);
		fbreader.setMode(FBReader.ViewMode.BOOK_TEXT);

		return true;
	}

	boolean isEmpty() {
		final ContentsModel contentsModel = (ContentsModel)getModel();
		return (contentsModel == null) || (contentsModel.getParagraphsNumber() == 0);
	}

	int currentTextViewParagraph(boolean includeStart) {
		final ZLTextWordCursor cursor = getFBReader().getBookTextView().getStartCursor();
		if (!cursor.isNull()) {
			int reference = cursor.getParagraphCursor().getIndex();
			boolean startOfParagraph = cursor.getWordNumber() == 0;
			if (cursor.isEndOfParagraph()) {
				++reference;
				startOfParagraph = true;
			}
			final int length = getModel().getParagraphsNumber();
			final ContentsModel contentsModel = (ContentsModel)getModel();
			for (int i = 1; i < length; ++i) {
				final int contentsReference =
					contentsModel.getReference(contentsModel.getTreeParagraph(i));
				if ((contentsReference > reference) ||
						(!includeStart && startOfParagraph
							 && (contentsReference == reference))) {
					return i - 1;
				}
			}
			return length - 1;
		}
		return -1;
	}

	void gotoReference() {
		getModel().removeAllMarks();
		final int selected = currentTextViewParagraph(true);
		highlightParagraph(selected);
		gotoParagraph(selected, false);
		scrollPage(false, ScrollingMode.SCROLL_PERCENTAGE, 40);
	}
}
