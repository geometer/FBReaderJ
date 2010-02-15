/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.pdb;

import java.io.*;

public abstract class DocDecompressor {
	public static int decompress(InputStream stream, byte[] targetBuffer, int compressedSize) throws IOException {
		final byte[] sourceBuffer = new byte[compressedSize];

		if (stream.read(sourceBuffer) != compressedSize) {
			return 0;
		}

		int sourceIndex = 0;
		int targetIndex = 0;

		int count0 = 0;
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		try {
			while (true) {
				final byte token = sourceBuffer[sourceIndex++];
				switch (token) {
					default:
						++count0;
						targetBuffer[targetIndex++] = token;
						break;
					case 1: case 2: case 3: case 4:
					case 5: case 6: case 7: case 8:
						++count1;
						System.arraycopy(sourceBuffer, sourceIndex, targetBuffer, targetIndex, token);
						sourceIndex += token;
						targetIndex += token;
						break;
					case -64: case -63: case -62: case -61:
					case -60: case -59: case -58: case -57:
					case -56: case -55: case -54: case -53:
					case -52: case -51: case -50: case -49:
					case -48: case -47: case -46: case -45:
					case -44: case -43: case -42: case -41:
					case -40: case -39: case -38: case -37:
					case -36: case -35: case -34: case -33:
					case -32: case -31: case -30: case -29:
					case -28: case -27: case -26: case -25:
					case -24: case -23: case -22: case -21:
					case -20: case -19: case -18: case -17:
					case -16: case -15: case -14: case -13:
					case -12: case -11: case -10: case -9:
					case -8: case -7: case -6: case -5:
					case -4: case -3: case -2: case -1:
						++count2;
						targetBuffer[targetIndex++] = ' ';
						targetBuffer[targetIndex++] = (byte)(token ^ 0x80);
						break;
					case -128: case -127: case -126: case -125:
					case -124: case -123: case -122: case -121:
					case -120: case -119: case -118: case -117:
					case -116: case -115: case -114: case -113:
					case -112: case -111: case -110: case -109:
					case -108: case -107: case -106: case -105:
					case -104: case -103: case -102: case -101:
					case -100: case -99: case -98: case -97:
					case -96: case -95: case -94: case -93:
					case -92: case -91: case -90: case -89:
					case -88: case -87: case -86: case -85:
					case -84: case -83: case -82: case -81:
					case -80: case -79: case -78: case -77:
					case -76: case -75: case -74: case -73:
					case -72: case -71: case -70: case -69:
					case -68: case -67: case -66: case -65:
						++count3;
						final int N = ((token & 0x3F) << 8) + (sourceBuffer[sourceIndex++] & 0xFF);
						int copyLength = (N & 7) + 3;
						int srcIndex = targetIndex - (N >> 3);
						if (targetIndex >= srcIndex + copyLength) {
							System.arraycopy(targetBuffer, srcIndex, targetBuffer, targetIndex, copyLength);
							targetIndex += copyLength;
						} else {
							while (copyLength-- > 0) {
								targetBuffer[targetIndex++] = targetBuffer[srcIndex++];
							}
						}
						break;
				}
			}
		} catch (Exception e) {
			if (targetIndex > targetBuffer.length) {
				targetIndex = targetBuffer.length;
			}
		}

		return targetIndex;
	}
}
