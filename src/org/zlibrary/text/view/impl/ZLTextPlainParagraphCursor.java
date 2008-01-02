package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.ZLTextModel;

class ZLTextPlainParagraphCursor extends ZLTextParagraphCursor {
	ZLTextPlainParagraphCursor(ZLTextModel model, int index) {
		super(model, index);	
		//System.err.println("ZLTextPlainParagraphCursor " + index);
	}	

	public boolean isLast() {
		return (myIndex + 1 == myModel.getParagraphsNumber());
	}

	public ZLTextParagraphCursor previous() {
		return isFirst() ? null : cursor(myModel, myIndex - 1);
	}

	public ZLTextParagraphCursor next() {
		return isLast() ? null : cursor(myModel, myIndex + 1);
	}
}
