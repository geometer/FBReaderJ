package org.geometerplus.fbreader.formats.plucker;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public class PluckerFileImage extends ZLSingleImage {
	private final String myPath;
	private final int myOffset;
	private final int mySize;

	public PluckerFileImage(String mimeType, final String path, final int offset, final int size) {
		super(mimeType);
		myPath = path;
		myOffset = offset;
		mySize = size;
	}

	public byte[] byteData() {
		final InputStream stream = ZLibrary.Instance().getInputStream(myPath);
		
		if (stream == null) {
			return new byte[0];
		}
		try {
			stream.skip(myOffset);
			byte [] buffer = new byte[mySize];
			stream.read(buffer, 0, mySize);
			return buffer;
		} catch (IOException e) {}
		
		return new byte[0];
	}

}
