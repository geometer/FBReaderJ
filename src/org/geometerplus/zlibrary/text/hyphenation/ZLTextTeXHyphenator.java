/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

final class ZLTextTeXHyphenator extends ZLTextHyphenator {
	private final HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern> myPatternTable = 
		new HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern>();
	private String myLanguage;

	void addPattern(ZLTextTeXHyphenationPattern pattern) {
		myPatternTable.put(pattern, pattern);
	}

	private List<String> myLanguageCodes;
	public List<String> languageCodes() {
		if (myLanguageCodes == null) {
			final TreeSet<String> codes = new TreeSet<String>();
			final ZLFile patternsFile = ZLResourceFile.createResourceFile("hyphenationPatterns");
			for (ZLFile file : patternsFile.children()) {
				final String name = file.getShortName();
				if (name.endsWith(".pattern")) {
				    codes.add(name.substring(0, name.length() - ".pattern".length()));
				}
			}

			codes.add("zh");
			myLanguageCodes = new ArrayList<String>(codes);
		}

		return Collections.unmodifiableList(myLanguageCodes);
	}

	public void load(final String language) {
		if (ZLMiscUtil.equals(language, myLanguage)) {
			return;
		}
		myLanguage = language;
		unload();

		if (language != null) {
			new ZLTextHyphenationReader(this).read(ZLResourceFile.createResourceFile(
		  		"hyphenationPatterns/" + language + ".pattern"
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

		final HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern> table = myPatternTable;
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
