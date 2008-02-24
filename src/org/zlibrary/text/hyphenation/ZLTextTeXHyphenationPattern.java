package org.zlibrary.text.hyphenation;

final class ZLTextTeXHyphenationPattern {
	final int myLength;
	final char[] mySymbols;
	private final byte[] myValues;

	ZLTextTeXHyphenationPattern(char[] pattern, int offset, int length) {
		int patternLength = 0;
		for (int i = 0; i < length; ++i) {
			final char symbol = pattern[offset + i];
			if ((symbol > '9') || (symbol < '0')) {
				++patternLength;
			}
		}
		myLength = patternLength;
		mySymbols = new char[patternLength];
		myValues = new byte[patternLength + 1];

		myValues[0] = 0;
		for (int i = 0, k = 0; i < length; ++i) {
			final char symbol = pattern[offset + i];
			if ((symbol <= '9') && (symbol >= '0')) {
				myValues[k] = (byte)(symbol - '0');
			} else {
				mySymbols[k] = symbol;
				++k;
				myValues[k] = 0;
			}
		}
	}
	
	void apply(byte[] values, int position) {
		final int patternLength = myLength;
		for (int i = 0; i <= patternLength; i++) {
			final byte val = myValues[i];
			if (values[position + i] < val) {
				values[position + i] = val;
			}
		}
	}
}
