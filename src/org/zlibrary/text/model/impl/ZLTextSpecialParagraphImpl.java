package org.zlibrary.text.model.impl;

import java.util.ArrayList;

class ZLTextSpecialParagraphImpl extends ZLTextParagraphImpl {
	private byte myKind;

	ZLTextSpecialParagraphImpl(byte kind, ZLTextModelImpl model) {
		super(model);
		myKind = kind;
	}

	ZLTextSpecialParagraphImpl(byte kind, ZLTextModelImpl model, int offset, int length) {
		super(model, offset, length);
		myKind = kind;
	}

	public byte getKind() {
		return myKind;
	}
}
