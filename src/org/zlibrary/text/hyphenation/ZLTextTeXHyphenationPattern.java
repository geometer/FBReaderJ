package org.zlibrary.text.hyphenation;

/*package*/ class ZLTextTeXHyphenationPattern {
	private int myLength;
	private char[] mySymbols;
	private byte[] myValues;

	public ZLTextTeXHyphenationPattern(char[] ucs2String, int offset, int length) {
		myLength = length;
		System.arraycopy(ucs2String, offset, mySymbols, 0, length);
		myValues = null;
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
