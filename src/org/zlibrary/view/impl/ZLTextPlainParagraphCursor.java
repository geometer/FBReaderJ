package org.zlibrary.view.impl;

import org.zlibrary.text.model.ZLTextModel;

class ZLTextPlainParagraphCursor extends ZLTextParagraphCursor {
	/*package*/ ZLTextPlainParagraphCursor(ZLTextModel model, int index) {
		super(model, index);	
	}	

	public boolean isLast() {
		return (myIndex + 1 == myModel.getParagraphsNumber());
	}

	public ZLTextParagraphCursor previous() {
		return isFirst() ? null : getCursor(myModel, myIndex - 1);
	}

	public ZLTextParagraphCursor next() {
		return isLast() ? null : getCursor(myModel, myIndex + 1);
	}
}
