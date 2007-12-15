package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

final class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	private int[] myParagraphInfos = new int[1024];
	private int myParagraphsNumber;

	public final int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public final ZLTextParagraph getParagraph(int index) {
		final int info = myParagraphInfos[index];
		final byte kind = (byte)(info >> 24);
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index, info & 0x00FFFFFF) :
			new ZLTextSpecialParagraphImpl(kind, this, index, info & 0x00FFFFFF);
	}

	void increaseLastParagraphSize() {
		++myParagraphInfos[myParagraphsNumber - 1];
	}

	public void createParagraph(byte kind) {
		onParagraphCreation();
		if (myParagraphInfos.length == myParagraphsNumber) {
			int newLength = myParagraphInfos.length * 2;
			int[] infos = new int[newLength];
			System.arraycopy(myParagraphInfos, 0, infos, 0, myParagraphInfos.length);
			myParagraphInfos = infos;
		}
		myParagraphInfos[myParagraphsNumber++] = kind << 24;
	}
}
