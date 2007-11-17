package org.zlibrary.text.view.impl;

import org.zlibrary.core.view.ZLPaintContext;

/*Should we use this special comment for package local things?*/

public class ZLTextWord extends ZLTextElement { 
	public String Data;
	//public short mySize;
	public short Length;
	//public int myParagraphOffset;

	public ZLTextWord(String data, short size, int paragraphOffset) {
		Data = data;
		//mySize = size;
		Length = (short) data.length();
		//myParagraphOffset = paragraphOffset;
	}

	public int getWidth(ZLPaintContext context) {
		return context.getStringWidth(Data);	
	}
	
	/*	public Kind getKind() {
		return Kind.WORD_ELEMENT;
	}*/
}
