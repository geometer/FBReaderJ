package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;
import org.zlibrary.core.library.ZLibrary;

/*package*/ class ZLTextTeXHyphenator extends ZLTextHyphenator {

	public static final String POSTFIX = ".pattern";	
/*	
	private static final String getPatternZip();
	private static void collectLanguages();
*/	
	private static ZLTextTeXPatternComparator comparator = new ZLTextTeXPatternComparator();
	private static ArrayList languageCodes;
	private static ArrayList languageNames;
/*
	public static final ArrayList getLanguageCodes();
	public static final ArrayList getLanguageNames();
*/	
	private final ArrayList myPatternTable = new ArrayList();
	private String myLanguage;
	private String myBreakingAlgorithm;
	
	public ZLTextTeXHyphenator() {
		myBreakingAlgorithm = "";
	}

	void setBreakingAlgorithm(String algorithm) {
		myBreakingAlgorithm = algorithm;
	}

	void addPattern(ZLTextTeXHyphenationPattern pattern) {
		myPatternTable.add(pattern);
	}

	private String PatternZip() {
		return ZLibrary.JAR_DATA_PREFIX + "data/";
	}

	public void load(final String language) {
		if (language.equals(myLanguage)) {
			return;
		}
		myLanguage = language;
		unload();
		System.err.println(new ZLTextHyphenationReader(this).read(PatternZip() + language + POSTFIX));
//		System.err.println("hyphenationPatterns were read.");
		System.err.println(myPatternTable.size());
//		Collections.sort(myPatternTable, new ZLTextTeXPatternComparator());
	}

	public void unload() {
		myPatternTable.clear();
		myBreakingAlgorithm = "";	
	}

	public String getBreakingAlgorithm() {
		return myBreakingAlgorithm;	
	}

	protected void hyphenate(StringBuffer ucs2String, boolean[] mask, int length) {
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
		
		ZLTextTeXPatternComparator comparator = new ZLTextTeXPatternComparator();
		
		for (int j = 0; j < length - 2; j++) {
			for (int k = 1; k <= length - j; k++) {
				ZLTextTeXHyphenationPattern pattern = new ZLTextTeXHyphenationPattern(ucs2String.toString().toCharArray(), j, k);
				int index = -1;
				for (int i = 0; i < myPatternTable.size(); i++) {
					if (comparator.compare(myPatternTable.get(i), pattern) == 0) {
						index = i;
						break;
					}
				}
				if (index == -1) {
					continue;
				}
				((ZLTextTeXHyphenationPattern) myPatternTable.get(index)).apply(values, j);
			}
		}
 	
//     		System.err.println("hyphenating...");
		for (int i = 0; i < length - 1; i++) {
//			System.err.print(values[i + 1] + " ");
			mask[i] =  (values[i + 1] % 2) == 1;
		}
//		System.err.println();
	}
}
