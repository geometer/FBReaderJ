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
		final int info = myParagraphInfos.get(index);
		final byte kind = (byte)(info >> 24);
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index, info & 0x00FFFFFF) :
			new ZLTextSpecialParagraphImpl(kind, this, index, info & 0x00FFFFFF);
	}

	void increaseLastParagraphSize() {
		final ZLIntArray infos = myParagraphInfos;
		infos.increment(infos.size() - 1);
	}

	public void createParagraph(byte kind) {
		createParagraph();
		myParagraphInfos.add(kind << 24);
	}
}
