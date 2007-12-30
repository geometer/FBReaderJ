package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLIntArray;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

final class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	private ZLIntArray myParagraphInfos = new ZLIntArray(1024);

	public final int getParagraphsNumber() {
		return myParagraphInfos.size();
	}

	public final ZLTextParagraph getParagraph(int index) {
		final int info = myParagraphInfos.get(index);
		final byte kind = (byte)(info >> 24);
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index, info & 0x00FFFFFF) :
			new ZLTextSpecialParagraphImpl(kind, this, index, info & 0x00FFFFFF);
	}

	void increaseLastParagraphSize() {
		myParagraphInfos.increment(myParagraphInfos.size() - 1);
	}

	public void createParagraph(byte kind) {
		onParagraphCreation();
		myParagraphInfos.add(kind << 24);
	}
}
