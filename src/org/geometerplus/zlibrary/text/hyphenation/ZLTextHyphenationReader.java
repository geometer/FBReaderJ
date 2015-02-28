/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.hyphenation;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.util.ZLArrayUtils;

final class ZLTextHyphenationReader extends ZLXMLReaderAdapter {
	private static final String PATTERN = "pattern";

	private final ZLTextTeXHyphenator myHyphenator;
	private boolean myReadPattern;
	private char[] myBuffer = new char[10];
	private int myBufferLength;

	ZLTextHyphenationReader(ZLTextTeXHyphenator hyphenator) {
		myHyphenator = hyphenator;
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if (PATTERN.equals(tag)) {
			myReadPattern = true;
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		if (PATTERN.equals(tag)) {
			myReadPattern = false;
			final int len = myBufferLength;
			if (len != 0) {
				myHyphenator.addPattern(new ZLTextTeXHyphenationPattern(myBuffer, 0, len, true));
			}
			myBufferLength = 0;
		}
		return false;
	}

	@Override
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
