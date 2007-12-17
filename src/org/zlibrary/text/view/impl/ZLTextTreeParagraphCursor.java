package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.ZLTextModel;

final class ZLTextTreeParagraphCursor extends ZLTextParagraphCursor {
	ZLTextTreeParagraphCursor(ZLTextModel model, int index) {
		super(model, index);	
	}	

	public boolean isLast() {
		// TODO: implement
		return (myIndex + 1 == myModel.getParagraphsNumber());
	}

	public ZLTextParagraphCursor previous() {
		// TODO: implement
		return isFirst() ? null : cursor(myModel, myIndex - 1);
	}

	public ZLTextParagraphCursor next() {
		// TODO: implement
		return isLast() ? null : cursor(myModel, myIndex + 1);
	}
}
