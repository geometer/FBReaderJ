package org.zlibrary.text.view.impl;

import org.zlibrary.text.model.entry.ZLTextControlEntry;

class ZLTextControlElement extends ZLTextElement {
	private final ZLTextControlEntry myEntry;

	/*package*/ ZLTextControlElement(ZLTextControlEntry entry) {
		myEntry = entry;
	}

/*	public Kind getKind() {
		return Kind.CONTROL_ELEMENT;
	}*/

	public ZLTextControlEntry getEntry() {
		return myEntry;
	}

/*	public byte getTextKind() {
		return getEntry().getKind();
	}*/

	public boolean isStart() {
		return myEntry.isStart();
	}
}
