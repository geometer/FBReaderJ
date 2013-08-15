package org.geometerplus.fbreader.book;

public class RationalNumber {
	private long myNumerator;
	private long myDenominator;
	
	public RationalNumber(long numerator, long denominator) {
		myNumerator = numerator;
		myDenominator = denominator;
	}
	
	public long getNumerator() {
		return myNumerator;
	}
	
	public long getDenominator() {
		return myDenominator;
	}
}
