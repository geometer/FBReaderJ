/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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
package org.geometerplus.zlibrary.core.language;

import java.util.ArrayList;
import java.util.Iterator;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.language.ZLLanguageMatcher.ZLLanguagePatternBasedMatcher;
import org.geometerplus.zlibrary.core.language.ZLLanguageMatcher.ZLWordBasedMatcher;
import org.geometerplus.zlibrary.core.util.ZLUnicodeUtil;

public class ZLLanguageDetector {
	private ArrayList<ZLWordBasedMatcher> myUtf8Matchers = new ArrayList<ZLWordBasedMatcher>();
	private	ArrayList<ZLWordBasedMatcher> myNonUtf8Matchers = new ArrayList<ZLWordBasedMatcher>();
	//private	ArrayList<ZLChineseMatcher> myChineseMatchers;

	// 0: no break
	// 1: break and skip
	// 2: skip word
	// 3: skip word, break after
	private static char[] SYMBOL_TYPE = {
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 2, 1, 1, 1, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 3, 1,
		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 1, 1,
		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	
	public class LanguageInfo {
			LanguageInfo(String language, String encoding) {
				this.Language = language;
				this.Encoding = encoding;
			}
			public String Language;
			public String Encoding;
		};

	public ZLLanguageDetector() {
		for (ZLFile file : ZLLanguageList.patternsFile().children()) {
			final String name = file.getName(true);
			final int index = name.indexOf('_');
			if (index != -1) {
				final String language = name.substring(0, index);
				final String encoding = name.substring(index + 1);
				ZLWordBasedMatcher matcher = new ZLLanguagePatternBasedMatcher(file, new LanguageInfo(language, encoding));
				if (encoding == ZLLanguageMatcher.UTF8_ENCODING_NAME) {
					myUtf8Matchers.add(matcher);
				} else if (encoding == "US-ASCII") {
					myUtf8Matchers.add(matcher);
					myNonUtf8Matchers.add(matcher);
				} else {
					myNonUtf8Matchers.add(matcher);
				}
			}
		}
		//myUtf8Matchers.add(new ZLChineseUtf8Matcher());
		//myChineseMatchers.add(new ZLChineseBig5Matcher());
		//myChineseMatchers.add(new ZLChineseGBKMatcher());
	}

	enum EncodingType { ASCII, UTF8, OTHER 
	};
	
	public LanguageInfo findInfo(byte[] buffer, int length, int matchingCriterion) {
		final int start = 0;
		final int end = length;

		EncodingType encodingType = EncodingType.ASCII;
		
		int nonLeadingCharsCounter = 0;
		for (int ptr = start; ptr != end; ++ptr) {
			if (nonLeadingCharsCounter == 0) {
				if ((buffer[ptr] & 0x80) != 0) {
					encodingType = EncodingType.UTF8;
					if ((buffer[ptr] & 0xE0) == 0xC0) {
						nonLeadingCharsCounter = 1;
					} else if ((buffer[ptr] & 0xF0) == 0xE0) {
						nonLeadingCharsCounter = 2;
					} else if ((buffer[ptr] & 0xF8) == 0xF0) {
						nonLeadingCharsCounter = 3;
					} else {
						encodingType = EncodingType.OTHER;
						break;
					}
				}
			} else {
				if ((buffer[ptr] & 0xC0) != 0x80) {
					encodingType = EncodingType.OTHER;
					break;
				}
				--nonLeadingCharsCounter;
			}
		}
		
		ArrayList<ZLWordBasedMatcher> wbMatchers = (encodingType == EncodingType.UTF8) ? myUtf8Matchers : myNonUtf8Matchers;

		int wordStart = start;
		boolean containsSpecialSymbols = false;
		String word = "";
		for (int ptr = start; ptr != end; ++ptr) {
			switch (SYMBOL_TYPE[buffer[ptr]]) {
				case 0:
					break;
				case 1:
					if (!containsSpecialSymbols && (ptr > wordStart)) {
						int length2 = ptr - wordStart;
						if (encodingType == EncodingType.UTF8) {
							length = ZLUnicodeUtil.utf8Length(buffer, wordStart, length2);
						}
						String str = new String(buffer); 
						word += str.substring(wordStart,wordStart + ptr - wordStart);	
						for (Iterator<ZLWordBasedMatcher> it = wbMatchers.iterator(); it.hasNext(); ) {
							it.next().processWord(word, length2);
						}
						word = "";
					}
					wordStart = ptr + 1;
					containsSpecialSymbols = false;
					break;
				case 2:
					containsSpecialSymbols = true;
					break;
				case 3:
					wordStart = ptr + 1;
					containsSpecialSymbols = false;
					break;
			}
		}

		LanguageInfo info = null;
		
		for (Iterator<ZLWordBasedMatcher> it = wbMatchers.iterator(); it.hasNext(); ) {
			ZLWordBasedMatcher itzl = it.next();
			int criterion = itzl.criterion();
			if (criterion > matchingCriterion) {
				info = itzl.info();
				matchingCriterion = criterion;
			}
			itzl.reset();
		}
		
		if (encodingType == EncodingType.OTHER) {
			/*for (Iterator it = myChineseMatchers.begin(); it != myChineseMatchers.end(); ++it) {
				(*it)->processBuffer((const unsigned char*)start, (const unsigned char*)end);
				int criterion = (*it)->criterion();
				if (criterion > matchingCriterion) {
					info = (*it)->info();
					matchingCriterion = criterion;
				}
				(*it)->reset();
			}*/
		}

		if (info != null &&
				(encodingType == EncodingType.UTF8) &&
				(info.Encoding != ZLLanguageMatcher.UTF8_ENCODING_NAME)) {
			return new LanguageInfo(info.Language, ZLLanguageMatcher.UTF8_ENCODING_NAME);
		}
		return info;
	}
}
