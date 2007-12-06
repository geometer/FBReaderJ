package org.zlibrary.text.view.impl;

import org.zlibrary.core.view.ZLPaintContext;

final class ZLTextWord extends ZLTextElement { 
	public final String Data;
	public final int Offset;
	public final int Length;
	private int myWidth = -1;

	public ZLTextWord(String data, int offset, int length) {
		Data = data;
		Offset = offset;
		Length = length;
	}

	public int getWidth(ZLPaintContext context) {
		int width = myWidth;
		if (width == -1) {
			width = context.getStringWidth(Data, Offset, Length);	
			myWidth = width;
		}
		return width;
	}

	/*package*/ String getWord() {
		return Data.substring(Offset, Offset + Length);	
	}
}
