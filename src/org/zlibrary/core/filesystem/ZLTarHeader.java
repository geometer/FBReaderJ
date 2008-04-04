package org.zlibrary.core.filesystem;

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
		IsRegularFile = (linkFlag == 0) || (linkFlag == (byte)'0');

		stream.skip(355);
	
		if (((linkFlag == (byte)'L') ||
				 (linkFlag == (byte)'K')) && (Name == "././@LongLink") && (Size < 10240)) {
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
