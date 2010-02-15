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

package org.geometerplus.zlibrary.text.hyphenation;

import java.util.*;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

public final class ZLTextTeXHyphenator extends ZLTextHyphenator {
	private final HashMap myPatternTable = new HashMap();
	private String myLanguage;
	
	public ZLTextTeXHyphenator() {
	}

	void addPattern(ZLTextTeXHyphenationPattern pattern) {
		myPatternTable.put(pattern, pattern);
	}

	public void load(final String language) {
		if (ZLMiscUtil.equals(language, myLanguage)) {
			return;
		}
		myLanguage = language;
		unload();

		if (language != null) {
			new ZLTextHyphenationReader(this).read(ZLResourceFile.createResourceFile(
		  		"data/hyphenationPatterns/" + language + ".pattern"
			)); 
		}
	}	

	public void unload() {
		myPatternTable.clear();
	}

	public void hyphenate(char[] stringToHyphenate, boolean[] mask, int length) {
		if (myPatternTable.isEmpty()) {
			for (int i = 0; i < length - 1; i++) {
				mask[i] = false;
			}
			return;
		}

		byte[] values = new byte[length + 1];
		
		final HashMap table = myPatternTable;
		ZLTextTeXHyphenationPattern pattern =
			new ZLTextTeXHyphenationPattern(stringToHyphenate, 0, length, false);
		for (int offset = 0; offset < length - 1; offset++) {
			int len = length - offset + 1;
			pattern.update(stringToHyphenate, offset, len - 1);
			while (--len > 0) {
				pattern.myLength = len;
				pattern.myHashCode = 0;
				ZLTextTeXHyphenationPattern toApply =
					(ZLTextTeXHyphenationPattern)table.get(pattern);
				if (toApply != null) {
					toApply.apply(values, offset);
				}
			}
		}
 	
		for (int i = 0; i < length - 1; i++) {
			mask[i] = (values[i + 1] % 2) == 1;
		}
	}
}
