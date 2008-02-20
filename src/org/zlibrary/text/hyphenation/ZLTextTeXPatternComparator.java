package org.zlibrary.text.hyphenation;

import java.util.Comparator;

/*package*/ class ZLTextTeXPatternComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof ZLTextTeXHyphenationPattern) || !(o2 instanceof ZLTextTeXHyphenationPattern)) {
			System.err.println("Objects passed to comparator are not instances of ZLTextTeXHyphenationPattern!");
			return 0;
		}
		ZLTextTeXHyphenationPattern p1 = ((ZLTextTeXHyphenationPattern) o1); 
		ZLTextTeXHyphenationPattern p2 = ((ZLTextTeXHyphenationPattern) o2); 
		int lengthDifference = p1.getLength() - p2.getLength();
		int minLength = (lengthDifference < 0) ? p1.getLength() : p2.getLength();
		char[] symbols1 = p1.getSymbols();
		char[] symbols2 = p2.getSymbols();

		for (int i = 0; i < minLength; i++) {
			if (symbols1[i] < symbols2[i]) {
				return -1;
			} else if (symbols1[i] > symbols2[i]) {
				return 1;
			}
		}
		
		return lengthDifference;
	}
}
