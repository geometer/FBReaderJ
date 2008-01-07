package org.zlibrary.text.view.impl;

public final class ZLTextHyperlinkControlElement extends ZLTextControlElement {
	public final String Label;

	ZLTextHyperlinkControlElement(byte kind, String label) {
		super(kind, true);
		Label = label;
	}
}
