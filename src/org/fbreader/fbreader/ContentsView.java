package org.fbreader.fbreader;

import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

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
	
	//TODO
	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);
	}

}
