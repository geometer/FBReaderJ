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

package org.geometerplus.zlibrary.core.filesystem.tar;

import java.io.InputStream;
import java.io.IOException;

class ZLTarHeader {
	String Name;
	int Size;
	boolean IsRegularFile;

	private static String getStringFromByteArray(byte[] buffer) {
		String s = new String(buffer);
		final int indexOfZero = s.indexOf((char)0);
		if (indexOfZero != -1) {
			return s.substring(0, indexOfZero);
		} else {
			return s;
		}
	}

	boolean read(InputStream stream) throws IOException {
		final byte[] fileName = new byte[100];
		if (stream.read(fileName) != 100) {
			return false;
		}
		if (fileName[0] == 0) {
			return false;
		}
		Name = getStringFromByteArray(fileName);

		if (stream.skip(24) != 24) {
			return false;
		}

		final byte[] fileSizeString = new byte[12];
		if (stream.read(fileSizeString) != 12) {
			return false;
		}
		Size = 0;
		for (int i = 0; i < 12; ++i) {
			final byte digit = fileSizeString[i];
			if ((digit < (byte)'0') || (digit > (byte)'7')) {
				break;
			}
			Size *= 8;
			Size += digit - (byte)'0';
		}

		if (stream.skip(20) != 20) {
			return false;
		}

		final byte linkFlag = (byte)stream.read();
		if (linkFlag == -1) {
			return false;
		}
		IsRegularFile = linkFlag == 0 || linkFlag == (byte)'0';

		stream.skip(355);

		if ((linkFlag == (byte)'L' || linkFlag == (byte)'K')
            	&& "././@LongLink".equals(Name) && Size < 10240) {
			final byte[] nameBuffer = new byte[Size - 1];
			stream.read(nameBuffer);
			Name = getStringFromByteArray(nameBuffer);
			final int skip = 512 - (Size & 0x1ff);
			stream.skip(skip + 1);
		}
		return true;
	}

	void erase() {
		Name = null;
	}
}
