package org.zlibrary.text.view.impl;

final class ZLTextControlElement extends ZLTextElement {
	public final byte Kind;
	public final boolean IsStart;

	ZLTextControlElement(byte kind, boolean isStart) {
//		System.out.println(entry.getKind() + " " + entry.isStart());
		Kind = kind;
		IsStart = isStart;
	}

	/*
	public ZLTextControlEntry getEntry() {
		return myEntry;
	}
	*/
}
