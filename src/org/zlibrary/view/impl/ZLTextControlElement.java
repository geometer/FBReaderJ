package org.zlibrary.view.impl;

import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextControlEntry;

class ZLTextControlElement extends ZLTextElement {
	private ZLTextParagraphEntry myEntry;

	private ZLTextControlElement(ZLTextParagraphEntry entry) {
		myEntry = entry;
	}

	public Kind getKind() {
		return Kind.CONTROL_ELEMENT;
	}

	public ZLTextControlEntry getEntry() {
		return (ZLTextControlEntry)myEntry;
	}

	public byte getTextKind() {
		return getEntry().getKind();
	}

	public boolean isStart() {
		return getEntry().isStart();
	}
}
