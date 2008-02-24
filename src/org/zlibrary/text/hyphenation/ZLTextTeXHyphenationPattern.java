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
		final char[] symbols = new char[patternLength];
		final byte[] values = new byte[patternLength + 1];

		values[0] = 0;
		for (int i = 0, k = 0; i < length; ++i) {
			final char sym = pattern[offset + i];
			if ((sym <= '9') && (sym >= '0')) {
				values[k] = (byte)(sym - '0');
			} else {
				symbols[k] = sym;
				++k;
				values[k] = 0;
			}
		}

		myLength = patternLength;
		mySymbols = symbols;
		myValues = values;
	}
	
	void apply(byte[] mask, int position) {
		final int patternLength = myLength;
		final byte[] values = myValues;
		for (int i = 0, j = position; i <= patternLength; ++i, ++j) {
			final byte val = values[i];
			if (mask[j] < val) {
				mask[j] = val;
			}
		}
	}

	public boolean equals(Object o) {
		ZLTextTeXHyphenationPattern pattern = (ZLTextTeXHyphenationPattern)o;
		final int len = myLength;
		if (len != pattern.myLength) {
			return false;
		}
		final char[] symbols0 = mySymbols;
		final char[] symbols1 = pattern.mySymbols;
		for (int i = 0; i < len; ++i) {
			if (symbols0[i] != symbols1[i]) {
				return false;
			}
		}
		return true;
	}

	private int myHashCode = -1;

	public int hashCode() {
		int hash = myHashCode;
		if (hash == -1) {
			final int len = myLength;
			final char[] symbols = mySymbols;
			hash = 0;
			for (int i = 0; i < len; ++i) {
				hash *= 31;
				hash += symbols[i];
			}
			myHashCode = hash;
		}
		return hash;
	}
}
