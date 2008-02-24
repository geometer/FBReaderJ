package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;
import org.zlibrary.core.library.ZLibrary;

/*package*/ class ZLTextTeXHyphenator extends ZLTextHyphenator {

	private static final String PREFIX = ZLibrary.JAR_DATA_PREFIX + "data/hyphenationPatterns/";
	private static final String POSTFIX = ".pattern";	
/*	
	private static final String getPatternZip();
	private static void collectLanguages();
*/	
	private static ArrayList languageCodes;
	private static ArrayList languageNames;
/*
	public static final ArrayList getLanguageCodes();
	public static final ArrayList getLanguageNames();
*/	
	//private final ArrayList myPatternTable = new ArrayList();
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
		new ZLTextHyphenationReader(this).read(PREFIX + language + POSTFIX);
//		System.err.println("hyphenationPatterns were read.");
		System.err.println(myPatternTable.size());
//		Collections.sort(myPatternTable, new ZLTextTeXPatternComparator());
	}

	public void unload() {
		myPatternTable.clear();
	}

	private static int comparePatterns(ZLTextTeXHyphenationPattern pattern1, ZLTextTeXHyphenationPattern pattern2) {
		final int len1 = pattern1.myLength;
		final int len2 = pattern2.myLength;
		final int minLength = (len1 < len2) ? len1 : len2;

		final char[] symbols1 = pattern1.mySymbols;
		final char[] symbols2 = pattern2.mySymbols;
		for (int i = 0; i < minLength; i++) {
			final int diff = symbols1[i] - symbols2[i];
			if (diff != 0) {
				return diff;
			}
		}
		
		return len1 - len2;
	}

	/*
	private static ZLTextTeXHyphenationPattern findPattern(ArrayList patternTable, ZLTextTeXHyphenationPattern pattern) {
		int left = 0;
		ZLTextTeXHyphenationPattern candidate = (ZLTextTeXHyphenationPattern)patternTable.get(left);
		int test = comparePatterns(candidate, pattern);
		if (test == 0) {
			return candidate;
		}
		if (test > 0) {
			return null;
		}

		int right = patternTable.size() - 1;
		candidate = (ZLTextTeXHyphenationPattern)patternTable.get(right);
		test = comparePatterns(candidate, pattern);
		if (test == 0) {
			return candidate;
		}
		if (test < 0) {
			return null;
		}
		while (right - left > 1) {
			final int middle = (left + right) / 2;
			candidate = (ZLTextTeXHyphenationPattern)patternTable.get(middle);
			test = comparePatterns(candidate, pattern);
			if (test == 0) {
				return candidate;
			}
			if (test < 0) {
				left = middle;
			} else {
				right = middle;
			}
		}
		return null;
	}
	*/

	protected void hyphenate(char[] stringToHyphenate, boolean[] mask, int length) {
		if (myPatternTable.isEmpty()) {
			for (int i = 0; i < length - 1; i++) {
				mask[i] = false;
			}
			return;
		}

		byte[] values = new byte [length + 1];
		for (int i = 0; i < length + 1; i++) {
			values[i] = 0;
		}
		
		for (int j = 0; j < length - 2; j++) {
			for (int k = 1; k <= length - j; k++) {
				ZLTextTeXHyphenationPattern pattern = new ZLTextTeXHyphenationPattern(stringToHyphenate, j, k);
				ZLTextTeXHyphenationPattern toApply = (ZLTextTeXHyphenationPattern)myPatternTable.get(pattern);
				if (toApply != null) {
					toApply.apply(values, j);
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
