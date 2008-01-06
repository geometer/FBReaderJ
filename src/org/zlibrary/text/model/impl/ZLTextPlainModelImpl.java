package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLArrayUtils;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

public final class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	private byte[] myParagraphKinds = new byte[INITIAL_CAPACITY];

	public ZLTextPlainModelImpl(int dataBlockSize) {
		super(dataBlockSize);
	}

	public final int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public final ZLTextParagraph getParagraph(int index) {
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	void extend() {
		super.extend();
		final int size = myParagraphKinds.length;
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size << 1);
	}

	public final void createParagraph(byte kind) {
		final int index = myParagraphsNumber;
		createParagraph();
		myParagraphKinds[index] = kind;
	}
}
