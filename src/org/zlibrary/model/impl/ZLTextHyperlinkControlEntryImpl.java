package org.zlibrary.model.impl;

class ZLTextHyperlinkControlEntryImpl extends ZLTextControlEntryImpl {
	private String myLabel;

	ZLTextHyperlinkControlEntryImpl(byte kind, String label) {
		super(kind, true);
		myLabel = label;
	}
	
	public String label() {
		return myLabel;
	}
	 
	public	boolean isHyperlink() {
		return true;
	}
}
