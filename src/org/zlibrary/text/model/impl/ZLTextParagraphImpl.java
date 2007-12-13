package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.ZLTextEntryImpl;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ArrayList<ZLTextParagraph.Entry> myEntries = new ArrayList<ZLTextParagraph.Entry>();

	ZLTextParagraphImpl() {
	}

	public List<ZLTextParagraph.Entry> getEntries() {
		return Collections.unmodifiableList(myEntries);
	}

	public Kind getKind() {
		return Kind.TEXT_PARAGRAPH;
	}

	public int getEntryNumber() {
		return myEntries.size();
	}

	public int getTextLength() {
		int size = 0;
		for (ZLTextParagraph.Entry entry: myEntries) {
			if (entry instanceof ZLTextEntryImpl) {
				size += ((ZLTextEntryImpl)entry).getDataLength();
			}
		}
		return size;
	}

	public void addEntry(ZLTextParagraph.Entry entry) {
		myEntries.add(entry);
	}
}
