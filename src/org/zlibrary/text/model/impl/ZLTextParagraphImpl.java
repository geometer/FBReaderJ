package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ZLTextModelImpl myModel;
	private final int myIndex;
	private int myLength;
	
	public int INDEX;

	ZLTextParagraphImpl(ZLTextModelImpl model, int index, int length) {
		myModel = model;
		myIndex = index;
		myLength = length;
	}

	ZLTextParagraphImpl(ZLTextModelImpl model) {
		this(model, model.getParagraphsNumber(), 0);
	}

	public EntryIterator iterator() {
		return myModel.new EntryIteratorImpl(myIndex, myLength);
	}

	public byte getKind() {
		return Kind.TEXT_PARAGRAPH;
	}

	public final int getEntryNumber() {
		return myLength;
	}

	public final int getTextLength() {
		int size = 0;
		for (EntryIterator it = iterator(); it.hasNext(); ) {
			if (it.getType() == Entry.TEXT) {
				size += it.getTextLength();
			}
		}
		return size;
	}

	final void addEntry() {
		++myLength;
	}
}
