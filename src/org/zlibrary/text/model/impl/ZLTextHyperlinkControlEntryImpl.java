package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextHyperlinkControlEntry;

class ZLTextHyperlinkControlEntryImpl extends ZLTextControlEntryImpl implements ZLTextHyperlinkControlEntry {
	private String myLabel;

	ZLTextHyperlinkControlEntryImpl(byte kind, String label) {
		super(kind, true);
		myLabel = label;
	}
	
	public String getLabel() {
		return myLabel;
	}
	 
	public	boolean isHyperlink() {
		return true;
	}
}
