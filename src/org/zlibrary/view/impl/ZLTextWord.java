package org.zlibrary.view.impl;

class ZLTextWord extends ZLTextElement { 
	public String myData;
	public short mySize;
	public short myLength;
	public int myParagraphOffset;
	
	private ZLTextWord(String data, short size, int paragraphOffset) {
		myData = data;
		mySize = size;
		myLength = (short) data.length();
		myParagraphOffset = paragraphOffset;
	}

	public Kind getKind() {
		return Kind.WORD_ELEMENT;
	}
}
