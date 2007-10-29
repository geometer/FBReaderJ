package org.zlibrary.view.impl;

class ZLTextLineInfo {
	public ZLTextWordCursor myStart;
	public ZLTextWordCursor myRealStart;
	public ZLTextWordCursor myEnd;
	public boolean myIsVisible;
	public int myLeftIndent;
	public int myWidth;
	public int myHeight;

	public ZLTextLineInfo(ZLTextWordCursor word) {
		myStart = word;
		myRealStart= word;
		myEnd = word;
		myIsVisible = false;
		myLeftIndent = 0;
		myWidth = 0;
		myHeight = 0;
	}
}
