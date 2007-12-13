package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextParagraphEntry;

class EntryPool {
	private static final ZLTextParagraphEntry[] ourStartEntries = new ZLTextParagraphEntry[256];
	private static final ZLTextParagraphEntry[] ourEndEntries = new ZLTextParagraphEntry[256];

	public static ZLTextParagraphEntry getControlEntry(byte kind, boolean isStart) {
		ZLTextParagraphEntry[] entries = isStart ? ourStartEntries : ourEndEntries;
		ZLTextParagraphEntry entry = entries[kind & 0xFF];
		if (entry == null) {
			entry = new ZLTextControlEntryImpl(kind, isStart);
			entries[kind & 0xFF] = entry;
		}
		return entry;
	}
}
