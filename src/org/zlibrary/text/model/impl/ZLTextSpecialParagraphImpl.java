package org.zlibrary.text.model.impl;

class ZLTextSpecialParagraphImpl extends ZLTextParagraphImpl {
	private final byte myKind;

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
