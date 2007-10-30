package org.zlibrary.text.view.impl;

import org.zlibrary.text.view.ZLTextView;

class ZLTextViewImpl implements ZLTextView {

	ZLTextLineInfo processTextLine(ZLTextWordCursor start, ZLTextWordCursor end) {
		ZLTextLineInfo info = new ZLTextLineInfo(start);
		return info;
	}
}
