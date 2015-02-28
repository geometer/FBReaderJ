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

package org.geometerplus.zlibrary.core.util;

import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream extends InputStream {
	private final InputStream myBaseStream;

	private int myDecoded0 = -1;
	private int myDecoded1 = -1;
	private int myDecoded2 = -1;
	private final byte[] myBuffer = new byte[32768];
	private int myBufferOffset;
	private int myBufferLength;

	public Base64InputStream(InputStream stream) {
		myBaseStream = stream;
	}

	@Override
	public int available() throws IOException {
		// TODO: real value might be less than returned one
		return (myBufferLength + myBaseStream.available()) * 3 / 4;
	}

	@Override
	public long skip(long n) throws IOException {
		// TODO: optimize
		for (long skipped = 0; skipped < n; ++skipped) {
			if (read() == -1) {
				return skipped;
			}
		}
		return n;
	}

	@Override
	public int read() throws IOException {
		int result = myDecoded0;
		if (result != -1) {
			myDecoded0 = -1;
			return result;
		}
		result = myDecoded1;
		if (result != -1) {
			myDecoded1 = -1;
			return result;
		}
		result = myDecoded2;
		if (result != -1) {
			myDecoded2 = -1;
			return result;
		}

		fillDecodedBuffer();
		result = myDecoded0;
		myDecoded0 = -1;
		return result;
	}

	@Override
	public void close() throws IOException {
		myBaseStream.close();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		int ready = 0;
		if (myDecoded0 != -1) {
			b[off] = (byte)myDecoded0;
			myDecoded0 = -1;
			if (len == 1) { return 1; }
			b[off + 1] = (byte)myDecoded1;
			myDecoded1 = -1;
			if (len == 2) { return 2; }
			b[off + 2] = (byte)myDecoded2;
			myDecoded2 = -1;
			ready = 3;
		} else if (myDecoded1 != -1) {
			b[off] = (byte)myDecoded1;
			myDecoded1 = -1;
			if (len == 1) { return 1; }
			b[off + 1] = (byte)myDecoded2;
			myDecoded2 = -1;
			ready = 2;
		} else if (myDecoded2 != -1) {
			b[off] = (byte)myDecoded2;
			myDecoded2 = -1;
			ready = 1;
		}
		for (; ready < len - 2; ready += 3) {
			int first = -1;
			int second = -1;
			int third = -1;
			int fourth = -1;
main:
			while (myBufferLength >= 0) {
				while (myBufferLength-- > 0) {
					final int digit = decode(myBuffer[myBufferOffset++]);
					if (digit != -1) {
						if (first == -1) {
							first = digit;
						} else if (second == -1) {
							second = digit;
						} else if (third == -1) {
							third = digit;
						} else {
							fourth = digit;
							break main;
						}
					}
				}
				fillBuffer();
			}
			if (first == -1) {
				return ready > 0 ? ready : -1;
			}
			b[off + ready]     = (byte)((first << 2) | (second >> 4));
			b[off + ready + 1] = (byte)((second << 4) | (third >> 2));
			b[off + ready + 2] = (byte)((third << 6) | fourth);
		}
		fillDecodedBuffer();
		for (; ready < len; ++ready) {
			final int num = read();
			if (num == -1) {
				return ready > 0 ? ready : -1;
			}
			b[off + ready] = (byte)num;
		}
		return len;
	}

	private void fillDecodedBuffer() throws IOException {
		int first = -1;
		int second = -1;
		int third = -1;
		int fourth = -1;
main:
		while (myBufferLength >= 0) {
			while (myBufferLength-- > 0) {
				final int digit = decode(myBuffer[myBufferOffset++]);
				if (digit != -1) {
					if (first == -1) {
						first = digit;
					} else if (second == -1) {
						second = digit;
					} else if (third == -1) {
						third = digit;
					} else {
						fourth = digit;
						break main;
					}
				}
			}
			fillBuffer();
		}
		if (first != -1) {
			myDecoded0 = (first << 2) | (second >> 4);
			myDecoded1 = 0xFF & ((second << 4) | (third >> 2));
			myDecoded2 = 0xFF & ((third << 6) | fourth);
		}
	}

	private void fillBuffer() throws IOException {
		myBufferLength = myBaseStream.read(myBuffer);
		myBufferOffset = 0;
	}

	private static int decode(byte b) {
		switch (b) {
			default:
				return -1;
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
			case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
			case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
			case 'Y': case 'Z':
				return b - 'A';
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
			case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
			case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
			case 's': case 't': case 'u': case 'v': case 'w': case 'x':
			case 'y': case 'z':
				return b - 'a' + 26;
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return b - '0' + 52;
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 64;
		}
	}
}
