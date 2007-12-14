package org.zlibrary.text.model.impl;

import java.util.ArrayList;

class ZLTextSpecialParagraphImpl extends ZLTextParagraphImpl {
	private Kind myKind;

	ZLTextSpecialParagraphImpl(Kind kind, ArrayList<Entry> entries) {
		super(entries);
		myKind = kind;
	}

	public Kind getKind() {
		return myKind;
	}
}
