package org.geometerplus.zlibrary.text.model.impl;

class ZLTextSpecialParagraphImpl extends ZLTextParagraphImpl {
	private final byte myKind;

	ZLTextSpecialParagraphImpl(byte kind, ZLTextModelImpl model) {
		super(model);
		myKind = kind;
	}

	ZLTextSpecialParagraphImpl(byte kind, ZLTextModelImpl model, int offset) {
		super(model, offset);
		myKind = kind;
	}

	public byte getKind() {
		return myKind;
	}
}
