package org.zlibrary.core.filesystem;

import java.io.InputStream;
import java.io.IOException;

class ZLTarHeader {
	String Name;
	int Size;
	boolean IsRegularFile;

	boolean read(InputStream stream) throws IOException {
		final byte[] fileName = new byte[100];
		stream.read(fileName);
		if (fileName[0] == 0) {
			return false;
		}
		Name = new String(fileName);

		stream.skip(24);
	
		final byte[] fileSizeString = new byte[12];
		stream.read(fileSizeString);
		Size = 0;
		for (int i = 0; i < 12; ++i) {
			final byte digit = fileSizeString[i];
			if ((digit < (byte)'0') || (digit > (byte)'9')) {
				break;
			}
			Size *= 8;
			Size += digit - (byte)'0';
		}
	
		stream.skip(20);

		final byte linkFlag = (byte)stream.read();
		IsRegularFile = (linkFlag == 0) || (linkFlag == (byte)'0');

		stream.skip(355);
	
		if (((linkFlag == (byte)'L') ||
				 (linkFlag == (byte)'K')) && (Name == "././@LongLink") && (Size < 10240)) {
			final byte[] nameBuffer = new byte[Size - 1];
			stream.read(nameBuffer);
			Name = new String(nameBuffer);
			final int skip = 512 - (Size & 0x1ff);
			stream.skip(skip + 1);
		}
		return true;
	}

	void erase() {
		Name = null;
	}
}
