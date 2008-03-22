package org.zlibrary.text.view.impl;

public class ZLTextFixedHSpaceElement extends ZLTextElement {
	private final static ZLTextElement[] ourCollection = new ZLTextElement[20];

	public static ZLTextElement getElement(short length) {
		if (length < 20) {
			ZLTextElement cached = ourCollection[length];
			if (cached == null) {
				cached = new ZLTextFixedHSpaceElement(length);
				ourCollection[length] = cached;
			}
			return cached;
		}	else {
			return new ZLTextFixedHSpaceElement(length);
		}
	}

	public final short Length;

	private ZLTextFixedHSpaceElement(short length) {
		Length = length;
	}
}
