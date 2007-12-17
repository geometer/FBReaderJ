package org.zlibrary.text.view.impl;

import org.zlibrary.text.view.*;

final class ZLTextLineInfo {
	static final class TreeNodeInfo {
		public final boolean IsLeaf;
		public final boolean IsOpen;
		public final boolean IsFirstLine;
		public final int ParagraphNumber;
		public final boolean[] VerticalLinesStack;

		TreeNodeInfo(boolean isLeaf, boolean isOpen, boolean isFirstLine, int paragraphNumber, boolean[] stack) {
			IsLeaf = isLeaf;
			IsOpen = isOpen;
			IsFirstLine = isFirstLine;
			ParagraphNumber = paragraphNumber;
			VerticalLinesStack = stack;
		}
	};
	TreeNodeInfo NodeInfo;

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
		Start = new ZLTextWordCursor(word);
		RealStart = new ZLTextWordCursor(word);
		End = new ZLTextWordCursor(word);
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
