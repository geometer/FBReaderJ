package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;

public class ZLTextControlEntry implements ZLTextParagraph.Entry {
	public final byte Kind;
	public final boolean IsStart;

	ZLTextControlEntry(byte kind, boolean isStart) {
		Kind = kind;
		IsStart = isStart;
	}
	 
	public boolean isHyperlink() {
		return false;
	}
}
