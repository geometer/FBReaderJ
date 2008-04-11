package org.geometerplus.zlibrary.core.image;

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public class ZLFileImage implements ZLImage {
	public ZLFileImage(String mimeType, String path, int offset) {
		myPath = path;
		myOffset = offset;
	}

	public byte [] byteData() {
		final InputStream stream = ZLibrary.getInstance().getInputStream(myPath);
		if (stream == null) {
			return new byte[0];
		}
		final ArrayList data = new ArrayList();
		byte[] buffer;
		int sizeOfBufferData;
		try {
			do {
				buffer = new byte[4096];
				sizeOfBufferData = stream.read(buffer);
				data.add(buffer);
			} while (sizeOfBufferData == 4096);
			final int dataSizeMinusOne = data.size() - 1;
			buffer = new byte[dataSizeMinusOne * 4096 + sizeOfBufferData];
			for (int i = 0; i < dataSizeMinusOne; ++i) {
				System.arraycopy(data.get(i), 0, buffer, i * 4096, 4096);
			}
			System.arraycopy(data.get(dataSizeMinusOne), 0, buffer, dataSizeMinusOne * 4096, sizeOfBufferData);
			return buffer;
		} catch (IOException e) {
		}
		return new byte[0];
	}

	private final String myPath;
	private final int myOffset;
}
