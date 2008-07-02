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
		System.out.println("byteData: ");
		System.out.println("path: " + myPath);
		System.out.println("Offset: " + myOffset);
		final InputStream stream = ZLibrary.getInstance().getInputStream(myPath);
		
		if (stream == null) {
			System.out.println("Stream == null");
			return new byte[0];
		}
		byte[] buffer;
		try {
			stream.skip(myOffset);
			buffer = new byte[mySize];
			final int size = stream.read(buffer, 0, mySize);
			System.out.println("DataSize: " + size + " " + mySize);
			return buffer;
		} catch (IOException e) {
			System.out.println("ioexception");
		}
		
		return new byte[0];
	}

}
