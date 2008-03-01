package org.zlibrary.text.model.impl;

class ZLTextMark {
	public int ParagraphNumber;
	public int Offset;
	public int Length;

	public ZLTextMark() {
		ParagraphNumber = -1;
		Offset = -1;
		Length = -1;
	}

	public ZLTextMark(int paragraphNumber, int offset, int length) {
		ParagraphNumber = paragraphNumber;
		Offset = offset;
		Length = length;
	}

	public ZLTextMark(final ZLTextMark mark) {
		ParagraphNumber = mark.ParagraphNumber;
		Offset = mark.Offset;
		Length = mark.Length;
	}

	public int compareTo(ZLTextMark mark) {
		if (ParagraphNumber == mark.ParagraphNumber) {
			if (Offset == mark.Offset) {
				return 0;
			} else if (Offset < mark.Offset) {
				return -1;
			} else {
				return 1;
			}
		} else if (ParagraphNumber < mark.ParagraphNumber) {
			return -1;
		} else {
			return 1;
		}
	}
}
