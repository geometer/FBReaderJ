package org.zlibrary.text.hyphenation;

final class ZLTextTeXHyphenationPattern {
	int myLength;
	final char[] mySymbols;
	private final byte[] myValues;
	int myHashCode;

	void update(char[] pattern, int offset, int length) {
		// We assert
		// 		1. this pattern doesn't use values
		// 		length <= original pattern length
		System.arraycopy(pattern, offset, mySymbols, 0, length);
		myLength = length;
		myHashCode = 0;
	}

	ZLTextTeXHyphenationPattern(char[] pattern, int offset, int length, boolean useValues) {
		if (useValues) {
			int patternLength = 0;
			for (int i = 0; i < length; ++i) {
				final char symbol = pattern[offset + i];
				if ((symbol > '9') || (symbol < '0')) {
					++patternLength;
				}
			}
			final char[] symbols = new char[patternLength];
			final byte[] values = new byte[patternLength + 1];
    
			for (int i = 0, k = 0; i < length; ++i) {
				final char sym = pattern[offset + i];
				if ((sym <= '9') && (sym >= '0')) {
					values[k] = (byte)(sym - '0');
				} else {
					symbols[k] = sym;
					++k;
				}
			}
    
			myLength = patternLength;
			mySymbols = symbols;
			myValues = values;
		} else {
			final char[] symbols = new char[length];
			System.arraycopy(pattern, offset, symbols, 0, length);
			myLength = length;
			mySymbols = symbols;
			myValues = null;
		}
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
		int len = myLength;
		if (len != pattern.myLength) {
			return false;
		}
		final char[] symbols0 = mySymbols;
		final char[] symbols1 = pattern.mySymbols;
		while (len-- != 0) {
			if (symbols0[len] != symbols1[len]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int hash = myHashCode;
		if (hash == 0) {
			final char[] symbols = mySymbols;
			hash = 0;
			int index = myLength;
			while (index-- != 0) {
				hash *= 31;
				hash += symbols[index];
			}
			myHashCode = hash;
		}
		return hash;
	}
}
