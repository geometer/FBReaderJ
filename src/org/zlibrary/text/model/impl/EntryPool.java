package org.zlibrary.text.model.impl;

class EntryPool {
	private static final ZLTextControlEntry[] ourStartEntries = new ZLTextControlEntry[256];
	private static final ZLTextControlEntry[] ourEndEntries = new ZLTextControlEntry[256];

	public static ZLTextControlEntry getControlEntry(byte kind, boolean isStart) {
		ZLTextControlEntry[] entries = isStart ? ourStartEntries : ourEndEntries;
		ZLTextControlEntry entry = entries[kind & 0xFF];
		if (entry == null) {
			entry = new ZLTextControlEntry(kind, isStart);
			entries[kind & 0xFF] = entry;
		}
		return entry;
	}
}
