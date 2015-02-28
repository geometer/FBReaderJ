/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.hyphenation;

public final class ZLTextTeXHyphenationPattern {
	private int myLength;
	private final char[] mySymbols;
	private final byte[] myValues;
	private int myHashCode;

	void update(char[] pattern, int offset, int length) {
		// We assert
		// 		1. this pattern doesn't use values
		// 		length <= original pattern length
		System.arraycopy(pattern, offset, mySymbols, 0, length);
		myLength = length;
		myHashCode = 0;
	}

	public ZLTextTeXHyphenationPattern(char[] pattern, int offset, int length, boolean useValues) {
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

	void reset(int length) {
		myLength = length;
		myHashCode = 0;
	}

	int length() {
		return myLength;
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

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < myLength; ++i) {
			if (myValues != null) {
				buffer.append((int)myValues[i]);
			}
			buffer.append(mySymbols[i]);
		}
		if (myValues != null) {
			buffer.append((int)myValues[myLength]);
		}
		return buffer.toString();
	}
}
