package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

public final class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	private byte[] myParagraphKinds = new byte[1024];

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

	public final void createParagraph(byte kind) {
		final int index = myParagraphsNumber;
		createParagraph();
		byte[] paragraphKinds = myParagraphKinds;
		if (index == paragraphKinds.length) {
			byte[] tmp = new byte[2 * index];
			System.arraycopy(paragraphKinds, 0, tmp, 0, index);
			paragraphKinds = tmp;
			myParagraphKinds = paragraphKinds;
		}
		paragraphKinds[index] = kind;
	}
}
