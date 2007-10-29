package org.zlibrary.view.impl;

import org.zlibrary.view.ZLTextView;

class ZLTextViewImpl implements ZLTextView {

	ZLTextLineInfo processTextLine(ZLTextWordCursor start, ZLTextWordCursor end) {
		ZLTextLineInfo info = new ZLTextLineInfo(start);
		return info;
	}
}
