package org.zlibrary.text.view.impl;

final class ZLTextControlElement extends ZLTextElement {
	private final static ZLTextControlElement[] myStartElements = new ZLTextControlElement[256];
	private final static ZLTextControlElement[] myEndElements = new ZLTextControlElement[256];

	static ZLTextControlElement get(byte kind, boolean isStart) {
		ZLTextControlElement[] elements = isStart ? myStartElements : myEndElements;
		ZLTextControlElement element = elements[kind & 0xFF];
		if (element == null) {
			element = new ZLTextControlElement(kind, isStart);
			elements[kind & 0xFF] = element;
		}
		return element;
	}

	public final byte Kind;
	public final boolean IsStart;

	private ZLTextControlElement(byte kind, boolean isStart) {
		Kind = kind;
		IsStart = isStart;
	}
}
