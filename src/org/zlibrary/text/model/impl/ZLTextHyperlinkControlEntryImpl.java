package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextHyperlinkControlEntry;

class ZLTextHyperlinkControlEntryImpl extends ZLTextControlEntryImpl implements ZLTextHyperlinkControlEntry {
	private final String myLabel;

	ZLTextHyperlinkControlEntryImpl(byte kind, String label) {
		super(kind, true);
		myLabel = label;
	}
	
	public final String getLabel() {
		return myLabel;
	}
	 
	public boolean isHyperlink() {
		return true;
	}
}
