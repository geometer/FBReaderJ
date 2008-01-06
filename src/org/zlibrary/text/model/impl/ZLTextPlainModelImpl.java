package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLIntArray;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

final class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	private final ZLIntArray myParagraphInfos = new ZLIntArray(1024);

	ZLTextPlainModelImpl(int dataBlockSize) {
		super(dataBlockSize);
	}

	public final int getParagraphsNumber() {
		return myParagraphInfos.size();
	}

	public final ZLTextParagraph getParagraph(int index) {
		final byte kind = (byte)myParagraphInfos.get(index);
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	public final void createParagraph(byte kind) {
		createParagraph();
		myParagraphInfos.add(kind);
	}
}
