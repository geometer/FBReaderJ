package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;

public class ZLTextFixedHSpaceEntry implements ZLTextParagraph.Entry {
	public final short Length;

	ZLTextFixedHSpaceEntry(short length) {
		Length = length;
	}
}
