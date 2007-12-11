package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;
import org.zlibrary.text.model.impl.ZLTextEntryImpl;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ArrayList<ZLTextParagraphEntry> myEntries = new ArrayList<ZLTextParagraphEntry>();

	ZLTextParagraphImpl() {
	}

	public List<ZLTextParagraphEntry> getEntries() {
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
		for (ZLTextParagraphEntry entry: myEntries) {
			if (entry instanceof ZLTextEntryImpl) {
				size += ((ZLTextEntryImpl)entry).getDataLength();
			}
		}
		return size;
	}

	public void addEntry(ZLTextParagraphEntry entry) {
		myEntries.add(entry);
	}
}
