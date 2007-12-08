package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextControlEntry;

class ZLTextControlEntryImpl implements ZLTextControlEntry {
	private final byte myKind;
	private final boolean myIsStart;

	ZLTextControlEntryImpl(byte kind, boolean isStart) {
		myKind = kind;
		myIsStart = isStart;
	}

	public final byte getKind() {
		return myKind;
	}

	public final boolean isStart() {
		return myIsStart;
	}
	
	public boolean isHyperlink() {
		return false;
	}
}
