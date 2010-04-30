package org.amse.ys.zip;

import java.io.*;

public class DeflatingDecompressor extends Decompressor {
	static {
		System.loadLibrary("DeflatingDecompressor");
	}

    // common variables
    private MyBufferedInputStream myStream;
    private int myCompressedAvailable;
    private int myAvailable;

	private static final int IN_BUFFER_SIZE = 2048;
	private static final int OUT_BUFFER_SIZE = 32768;

	private final byte[] myInBuffer = new byte[IN_BUFFER_SIZE];
	private int myInBufferOffset;
	private int myInBufferLength;
	private final byte[] myOutBuffer = new byte[OUT_BUFFER_SIZE];
	private int myOutBufferOffset;
	private int myOutBufferLength;

    public DeflatingDecompressor(MyBufferedInputStream inputStream, LocalFileHeader header) throws IOException {
        super();
        reset(inputStream, header);
    }

    void reset(MyBufferedInputStream inputStream, LocalFileHeader header) throws IOException {
		endInflating();

        myStream = inputStream;
        myCompressedAvailable = header.CompressedSize;
		myAvailable = header.UncompressedSize;

		myInBufferOffset = IN_BUFFER_SIZE;
		myInBufferLength = 0;
		myOutBufferOffset = OUT_BUFFER_SIZE;
		myOutBufferLength = 0;

		startInflating();
    }

	@Override
    public int available() {
        return myAvailable;
    }

	@Override
    public int read(byte[] b, int off, int len) throws IOException {
		if (myAvailable <= 0) {
			return -1;
		}
		if (len > myAvailable) {
			len = myAvailable;
		}
		for (int toFill = len; toFill > 0; ) {
			if (myOutBufferLength == 0) {
				fillOutBuffer();
			}
			if (myOutBufferLength == 0) {
				throw new IOException("cannot read from zip");
			}
			final int ready = (toFill < myOutBufferLength) ? toFill : myOutBufferLength;
			if (b != null) {
				System.arraycopy(myOutBuffer, myOutBufferOffset, b, off, ready);
			}
			off += ready;
			myOutBufferOffset += ready;
			toFill -= ready;
			myOutBufferLength -= ready;
		}
		myAvailable -= len;
		return len;
    }

	@Override
    public int read() throws IOException {
		if (myAvailable <= 0) {
			return -1;
		}
		if (myOutBufferLength == 0) {
			fillOutBuffer();
		}
		if (myOutBufferLength == 0) {
			throw new IOException("cannot read from zip");
		}
		--myAvailable;
		--myOutBufferLength;
		return myOutBuffer[myOutBufferOffset++];
    }

	private void fillOutBuffer() throws IOException {
		while (myOutBufferLength == 0) {
			if (myInBufferLength == 0) {
				myInBufferOffset = 0;
				final int toRead = (myCompressedAvailable < IN_BUFFER_SIZE) ? myCompressedAvailable : IN_BUFFER_SIZE;
				if (myStream.read(myInBuffer, 0, toRead) != toRead) {
					throw new IOException("cannot read from base stream");
				}
				myInBufferLength = toRead;
				myCompressedAvailable -= toRead;
			}
			final long result = inflate(myInBuffer, myInBufferOffset, myInBufferLength, myOutBuffer);
			if (result == 0) {
				throw new IOException("cannot read from base stream");
			}
			final int in = (int)(result >> 16) & 0xFFFF;
			final int out = (int)result & 0xFFFF;
			myInBufferOffset += in;
			myInBufferLength -= in;
			myOutBufferOffset = 0;
			myOutBufferLength = out;
			if ((result & (1L << 32)) != 0) {
				endInflating();
				myStream.backSkip(myInBufferLength - myInBufferOffset);
				break;
			}
		}
	}

    private native boolean startInflating();
    private native void endInflating();
	private native long inflate(byte[] in, int inOffset, int inLength, byte[] out);
}
