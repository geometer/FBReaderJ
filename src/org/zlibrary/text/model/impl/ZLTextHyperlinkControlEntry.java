package org.zlibrary.text.model.impl;

public final class ZLTextHyperlinkControlEntry extends ZLTextControlEntry {
	private final String Label;

	ZLTextHyperlinkControlEntry(byte kind, String label) {
		super(kind, true);
		Label = label;
	}
	 
	public boolean isHyperlink() {
		return true;
	}
}
