package org.amse.ys.zip;

import java.io.*;

public class NativeDeflatingDecompressor extends AbstractDeflatingDecompressor {
	public static final boolean INITIALIZED;
	static {
		boolean ini;
		try {
			System.loadLibrary("DeflatingDecompressor");
			ini = true;
		} catch (Throwable t) {
			ini = false;
		}
		INITIALIZED = ini;
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

    public NativeDeflatingDecompressor(MyBufferedInputStream inputStream, LocalFileHeader header) {
        super();
        reset(inputStream, header);
    }

    void reset(MyBufferedInputStream inputStream, LocalFileHeader header) {
		endInflating();

        myStream = inputStream;
        myCompressedAvailable = header.getCompressedSize();
		myAvailable = header.getUncompressedSize();

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
			System.arraycopy(myOutBuffer, myOutBufferOffset, b, off, ready);
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
			final int code = inflate(myInBuffer, myInBufferOffset, myInBufferLength, myOutBuffer);
			if (code == 0) {
				throw new IOException("cannot read from base stream");
			}
			myInBufferOffset += code >> 16;
			myInBufferLength -= code >> 16;
			myOutBufferOffset = 0;
			myOutBufferLength = code & 0x0FFFF;
		}
	}

    private native boolean startInflating();
    private native void endInflating();
	private native int inflate(byte[] in, int inOffset, int inLength, byte[] out);
}
