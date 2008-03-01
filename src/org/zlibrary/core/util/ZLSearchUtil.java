package org.zlibrary.core.util;

class ZLSearchUtil {
	private ZLSearchUtil() {
	}

	public static int find(char[] text, int offset, int length, final ZLSearchPattern pattern, int pos) {
		if (pos < 0) {
			pos = 0;
		}
		final String lower = pattern.LowerCasePattern;
		final int last = offset + length - pattern.getLength();
		final int patternLast = lower.length() - 1;
		if (pattern.IgnoreCase) {
			final String upper = pattern.UpperCasePattern;
			for (int i = offset + pos; i <= last; i++) {
				int j = 0;
				int k = i;
				for (; j <= patternLast; j++, k++) {
					if (lower.charAt(j) != text[k] && upper.charAt(j) != text[k]) {
						break;
					}
				}
				if (j > patternLast) {
					return i - offset;
				}

			}
		} else {
			for (int i = offset + pos; i <= last; i++) {
				int j = 0;
				int k = i;
				for (; j <= patternLast; j++, k++) {
					if (lower.charAt(j) != text[k]) {
						break;
					}
				}
				if (j > patternLast) {
					return i - offset;
				}
			}
		}
		return -1;
	}	
}
