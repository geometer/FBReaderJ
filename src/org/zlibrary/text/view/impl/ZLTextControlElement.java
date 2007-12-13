package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.impl.ZLTextControlEntry;

final class ZLTextControlElement extends ZLTextElement {
	private final ZLTextControlEntry myEntry;

	ZLTextControlElement(ZLTextControlEntry entry) {
//		System.out.println(entry.getKind() + " " + entry.isStart());
		myEntry = entry;
	}

	public ZLTextControlEntry getEntry() {
		return myEntry;
	}

	public byte getTextKind() {
		return myEntry.Kind;
	}

	public boolean isStart() {
		return myEntry.IsStart;
	}
}
