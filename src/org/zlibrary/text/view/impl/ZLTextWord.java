package org.zlibrary.text.view.impl;

import org.zlibrary.core.view.ZLPaintContext;

public final class ZLTextWord extends ZLTextElement { 
	public final char[] Data;
	public final int Offset;
	public final int Length;
	private int myWidth = -1;
	private Mark myMark;
	private int myParagraphOffset;

	class Mark {
		public final int Start;
		public final int Length;
		private Mark myNext;

		private Mark(int start, int length) {
			Start = start;
			Length = length;
			myNext = null;
		}

		public Mark getNext() {
			return myNext;
		}

		private void setNext(Mark mark) {
			myNext = mark;
		}
	}
	
	public ZLTextWord(char[] data, int offset, int length, int paragraphOffset) {
		Data = data;
		Offset = offset;
		Length = length;
		myParagraphOffset = paragraphOffset;
	}

	public Mark getMark() {
		return myMark;
	}

	public int getParagraphOffset() {
		return myParagraphOffset;
	}
	
	public void addMark(int start, int length) {
		System.err.println("addMark");
		Mark existingMark = myMark;
		Mark mark = new Mark(start, length);
		if ((existingMark == null) || (existingMark.Start > start)) {
			mark.setNext(existingMark);
			myMark = mark;
		} else {
			while ((existingMark.getNext() != null) && (existingMark.getNext().Start < start)) {
				existingMark = existingMark.getNext();
			}
			mark.setNext(existingMark.getNext());
			existingMark.setNext(mark);
		}		
	}
	
	public int getWidth(ZLPaintContext context) {
		int width = myWidth;
		if (width == -1) {
			width = context.getStringWidth(Data, Offset, Length);	
			myWidth = width;
		}
		return width;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = Offset; i < Offset + Length; i++) {
			sb.append(Data[i]);
		}	
		return sb.toString();
	}
}
