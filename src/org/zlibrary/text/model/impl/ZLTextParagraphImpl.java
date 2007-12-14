package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.ZLTextEntryImpl;

import java.util.*;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ArrayList<Entry> myEntries;
	private final int myOffset;
	private int myLength;

	ZLTextParagraphImpl(ArrayList<Entry> entries) {
		myEntries = entries;
		myOffset = entries.size();
		myLength = 0;
	}

	private class EntryIterator implements Iterator<Entry> {
		private int myPosition;

		EntryIterator() {
			myPosition = myOffset;
		}

		public boolean hasNext() {
			return myPosition < myOffset + myLength;
		}

		public Entry next() {
			if (myPosition == myOffset + myLength) {
				throw new NoSuchElementException();
			}
			return myEntries.get(myPosition++);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<Entry> iterator() {
		return new EntryIterator();
	}

	public Kind getKind() {
		return Kind.TEXT_PARAGRAPH;
	}

	public final int getEntryNumber() {
		return myLength;
	}

	public final int getTextLength() {
		int size = 0;
		final ArrayList<Entry> entries = myEntries;
		final int from = myOffset;
		final int to = from + myLength;
		for (int i = from; i < to; ++i) {
			Entry entry = entries.get(i);
			if (entry instanceof ZLTextEntryImpl) {
				size += ((ZLTextEntryImpl)entry).getDataLength();
			}
		}
		return size;
	}

	final void addEntry(Entry entry) {
		myEntries.add(entry);
		++myLength;
	}
}
