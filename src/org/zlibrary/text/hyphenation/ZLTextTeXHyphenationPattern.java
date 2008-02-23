package org.zlibrary.text.hyphenation;



/*package*/ class ZLTextTeXHyphenationPattern {
	private int myLength;
	private char[] mySymbols;
	private byte[] myValues;

	public ZLTextTeXHyphenationPattern(char[] pattern, int offset, int length) {
		myLength = 0;
		for (int i = 0; i < length; i++) {
			if (pattern[offset + i] < '0' || pattern[offset + i] > '9') {
				myLength++;
			}
		}
		mySymbols = new char[myLength];
		myValues = new byte[myLength + 1];

		myValues[0] = 0;
		for (int j = 0, k = 0; j < length; j++) {
			if (pattern[offset + j] >= '0' && pattern[offset + j] <= '9') {
				myValues[k] = (byte) (pattern[offset + j] - '0');
			} else {
				mySymbols[k] = pattern[offset + j];
				k++;
				myValues[k] = 0;
			}
		}
	}

	int getLength() {
		return myLength;
	}

	char[] getSymbols() {
		return mySymbols;
	}
	
	void apply(byte[] values, int position) {
		for (int i = 0; i <= myLength; i++) {
			if (values[position + i] < myValues[i]) {
				values[position + i] = myValues[i];
			}
		}
	}
}
