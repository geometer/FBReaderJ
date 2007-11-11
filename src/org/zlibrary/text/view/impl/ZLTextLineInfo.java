package org.zlibrary.text.view.impl;

class ZLTextLineInfo {

	/*This class has public fields like struct in C++. 
	 Should I remove prefix "my", or should I make "getters" and "setters" for all the fields, making them private?*/

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
