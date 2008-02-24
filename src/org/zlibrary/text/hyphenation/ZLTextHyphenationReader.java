package org.zlibrary.text.hyphenation;

import org.zlibrary.core.xml.ZLStringMap;
import org.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.zlibrary.core.util.ZLArrayUtils;

final  class ZLTextHyphenationReader extends ZLXMLReaderAdapter {
	private static final String PATTERN = "pattern";

	private final ZLTextTeXHyphenator myHyphenator;
	private boolean myReadPattern;
	private char[] myBuffer = new char[10];
	private int myBufferLength;

	ZLTextHyphenationReader(ZLTextTeXHyphenator hyphenator) {
		myHyphenator = hyphenator;
	}

	public void startElementHandler(String tag, ZLStringMap attributes) {
		if (PATTERN.equals(tag)) {
			myReadPattern = true;
		}
	}

	public void endElementHandler(String tag) {
		if (PATTERN.equals(tag)) {
			myReadPattern = false;
			final int len = myBufferLength;
			if (len != 0) {
				myHyphenator.addPattern(new ZLTextTeXHyphenationPattern(myBuffer, 0, len, true));
			}
			myBufferLength = 0;
		}
	}

	public void characterDataHandler(char[] ch, int start, int length) {
		if (myReadPattern) {
			char[] buffer = myBuffer;
			final int oldLen = myBufferLength;
			final int newLen = oldLen + length;
			if (newLen > buffer.length) {
				buffer = ZLArrayUtils.createCopy(buffer, oldLen, newLen + 10);
				myBuffer = buffer;
			}
			System.arraycopy(ch, start, buffer, oldLen, length);
			myBufferLength = newLen;
		}
	}
}
