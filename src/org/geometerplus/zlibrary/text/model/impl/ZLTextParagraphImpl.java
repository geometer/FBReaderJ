package org.geometerplus.zlibrary.text.model.impl;

import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ZLTextModelImpl myModel;
	private final int myIndex;
	
	ZLTextParagraphImpl(ZLTextModelImpl model, int index) {
		myModel = model;
		myIndex = index;
	}

	ZLTextParagraphImpl(ZLTextModelImpl model) {
		this(model, model.getParagraphsNumber());
	}

	public EntryIterator iterator() {
		return myModel.new EntryIteratorImpl(myIndex);
	}

	public byte getKind() {
		return Kind.TEXT_PARAGRAPH;
	}
}
