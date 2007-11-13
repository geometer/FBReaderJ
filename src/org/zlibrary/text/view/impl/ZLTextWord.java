package org.zlibrary.text.view.impl;

/*Should we use this special comment for package local things?*/

public class ZLTextWord extends ZLTextElement { 
	public String myData;
	//public short mySize;
	public short myLength;
	//public int myParagraphOffset;

	/*Temporarily made public to avoid implementing ZLTextElementPool with all its messy allocators, wtf is it?*/

	public ZLTextWord(String data, short size, int paragraphOffset) {
		myData = data;
		//mySize = size;
		myLength = (short) data.length();
		//myParagraphOffset = paragraphOffset;
	}

/*	public Kind getKind() {
		return Kind.WORD_ELEMENT;
	}*/
}
