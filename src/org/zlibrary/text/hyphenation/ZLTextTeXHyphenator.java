package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;
import org.zlibrary.core.library.ZLibrary;

final class ZLTextTeXHyphenator extends ZLTextHyphenator {
/*	
	private static void collectLanguages();
*/	
	private static ArrayList languageCodes;
	private static ArrayList languageNames;
/*
	public static final ArrayList getLanguageCodes();
	public static final ArrayList getLanguageNames();
*/	
	private final HashMap myPatternTable = new HashMap();
	private String myLanguage;
	
	public ZLTextTeXHyphenator() {
	}

	void addPattern(ZLTextTeXHyphenationPattern pattern) {
		myPatternTable.put(pattern, pattern);
	}

	public void load(final String language) {
		if (language.equals(myLanguage)) {
			return;
		}
		myLanguage = language;
		unload();
		new ZLTextHyphenationReader(this).read(ZLibrary.JAR_DATA_PREFIX + "data/hyphenationPatterns/" + language + ".pattern");
//		System.err.println("hyphenationPatterns were read.");
		System.err.println(myPatternTable.size());
	}

	public void unload() {
		myPatternTable.clear();
	}

	protected void hyphenate(char[] stringToHyphenate, boolean[] mask, int length) {
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
		for (int offset = 0; offset < length - 2; offset++) {
			for (int len = 1; len <= length - offset; len++) {
				pattern.update(stringToHyphenate, offset, len);
				ZLTextTeXHyphenationPattern toApply =
					(ZLTextTeXHyphenationPattern)table.get(pattern);
				if (toApply != null) {
					toApply.apply(values, offset);
				}
			}
		}
 	
//     		System.err.println("hyphenating...");
		for (int i = 0; i < length - 1; i++) {
//			System.err.print(values[i + 1] + " ");
			mask[i] = (values[i + 1] % 2) == 1;
		}
//		System.err.println();
	}
}
