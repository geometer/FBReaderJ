package org.zlibrary.core.util;

public class ZLSearchUtil {
	private ZLSearchUtil() {
	}

	public static int find(char[] text, int offset, int length, final ZLSearchPattern pattern) {
		return find(text, offset, length, pattern, 0);
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
			final char firstCharLower = lower.charAt(0);
			final char firstCharUpper = upper.charAt(0);
			for (int i = offset + pos; i <= last; i++) {
				final char current = text[i];
				if ((current == firstCharLower) || (current == firstCharUpper)) {
					int j = 1;
					for (int k = i + 1; j <= patternLast; ++j, ++k) {
						final char symbol = text[k];
						if ((lower.charAt(j) != symbol) &&
								(upper.charAt(j) != symbol)) {
							break;
						}
					}
					if (j > patternLast) {
						return i - offset;
					}
				}
			}
		} else {
			final char firstChar = lower.charAt(0);
			for (int i = offset + pos; i <= last; i++) {
				if (text[i] == firstChar) {
					int j = 1;
					for (int k = i + 1; j <= patternLast; ++j, ++k) {
						if (lower.charAt(j) != text[k]) {
							break;
						}
					}
					if (j > patternLast) {
						return i - offset;
					}
				}
			}
		}
		return -1;
	}	
}
