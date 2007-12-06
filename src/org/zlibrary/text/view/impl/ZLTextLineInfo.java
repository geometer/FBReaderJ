package org.zlibrary.text.view.impl;

import org.zlibrary.text.view.*;

class ZLTextLineInfo {
	public ZLTextWordCursor Start;
	public ZLTextWordCursor RealStart;
	public ZLTextWordCursor End;
	public boolean IsVisible;
	public int LeftIndent;
	public int Width;
	public int Height;
	public int Descent;
	public int VSpaceAfter;
	public ZLTextStyle StartStyle;
	public int SpaceCounter;

	public ZLTextLineInfo(ZLTextWordCursor word, ZLTextStyle style) {
		Start = word;
		RealStart = word;
		End = word;
		IsVisible = false;
		LeftIndent = 0;
		Width = 0;
		Height = 0;
		Descent = 0;
		VSpaceAfter = 0;
		StartStyle = style;
		SpaceCounter = 0;
	}
}
