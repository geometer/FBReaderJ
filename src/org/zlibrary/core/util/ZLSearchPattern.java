package org.zlibrary.core.util;

public class ZLSearchPattern {
	/*package*/ boolean IgnoreCase;
	/*package*/ String LowerCasePattern;
	/*package*/ String UpperCasePattern;

	public ZLSearchPattern(final String pattern, boolean ignoreCase) {
		IgnoreCase = ignoreCase;
		if (IgnoreCase) {
			LowerCasePattern = pattern.toLowerCase();
			UpperCasePattern = pattern.toUpperCase();
		} else {
			LowerCasePattern = pattern;
		}
	}
	
	public int getLength() {
		return LowerCasePattern.length();
	}
}
