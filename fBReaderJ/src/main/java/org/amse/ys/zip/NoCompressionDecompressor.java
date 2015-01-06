package org.amse.ys.zip;

import java.io.*;

public final class NoCompressionDecompressor extends Decompressor {
	private final LocalFileHeader myHeader;
	private final MyBufferedInputStream myStream;
	private int myCurrentPosition;

	public NoCompressionDecompressor(MyBufferedInputStream is, LocalFileHeader header) {
		super();
		myHeader = header;
		myStream = is;
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		final int left = available();
		if (left <= 0) {
			return -1;
		}
		if (len > left) {
			len = left;
		}
		final int r = myStream.read(b, off, len);
		myCurrentPosition += r;
		return r;
	}

	@Override
	public int read() throws IOException {
		if (myCurrentPosition < myHeader.CompressedSize) {
			myCurrentPosition++;
			return myStream.read();
		} else {
			return -1;
		}
	}

	@Override
	public int available() throws IOException {
		return myHeader.UncompressedSize - myCurrentPosition;
	}
}
