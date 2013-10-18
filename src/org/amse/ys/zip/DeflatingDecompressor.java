package org.amse.ys.zip;

import java.io.*;

class DeflatingDecompressor extends Decompressor {
	static {
		System.loadLibrary("DeflatingDecompressor-v3");
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

	private volatile int myInflatorId = -1;

	public DeflatingDecompressor(MyBufferedInputStream inputStream, LocalFileHeader header) throws IOException {
		super();
		reset(inputStream, header);
	}

	void reset(MyBufferedInputStream inputStream, LocalFileHeader header) throws IOException {
		if (myInflatorId != -1) {
			endInflating(myInflatorId);
			myInflatorId = -1;
		}

		myStream = inputStream;
		myCompressedAvailable = header.CompressedSize;
		if (myCompressedAvailable <= 0) {
			myCompressedAvailable = Integer.MAX_VALUE;
		}
		myAvailable = header.UncompressedSize;
		if (myAvailable <= 0) {
			myAvailable = Integer.MAX_VALUE;
		}

		myInBufferOffset = IN_BUFFER_SIZE;
		myInBufferLength = 0;
		myOutBufferOffset = OUT_BUFFER_SIZE;
		myOutBufferLength = 0;

		myInflatorId = startInflating();
		if (myInflatorId == -1) {
			throw new ZipException("cannot start inflating");
		}
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
				len -= toFill;
				break;
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
		if (len > 0) {
			myAvailable -= len;
		} else {
			myAvailable = 0;
		}
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
			myAvailable = 0;
			return -1;
		}
		--myAvailable;
		--myOutBufferLength;
		return myOutBuffer[myOutBufferOffset++];
	}

	private void fillOutBuffer() throws IOException {
		if (myInflatorId == -1) {
			return;
		}

		while (myOutBufferLength == 0) {
			if (myInBufferLength == 0) {
				myInBufferOffset = 0;
				final int toRead = (myCompressedAvailable < IN_BUFFER_SIZE) ? myCompressedAvailable : IN_BUFFER_SIZE;
				myInBufferLength = myStream.read(myInBuffer, 0, toRead);
				if (myInBufferLength < toRead) {
					myCompressedAvailable = 0;
				} else {
					myCompressedAvailable -= toRead;
				}
			}
			if (myInBufferLength <= 0) {
				break;
			}
			final long result = inflate(myInflatorId, myInBuffer, myInBufferOffset, myInBufferLength, myOutBuffer);
			if (result <= 0) {
				final StringBuilder extraInfo = new StringBuilder()
					.append(myStream.offset()).append(":")
					.append(myInBufferOffset).append(":")
					.append(myInBufferLength).append(":")
					.append(myOutBuffer.length).append(":");
				for (int i = 0; i < Math.min(10, myInBufferLength); ++i) {
					extraInfo.append(myInBuffer[myInBufferOffset + i]).append(",");
				}
				throw new ZipException("Cannot inflate zip-compressed block, code = " + result + ";extra info = " + extraInfo);
			}
			final int in = (int)(result >> 16) & 0xFFFF;
			if (in > myInBufferLength) {
				throw new ZipException("Invalid inflating result, code = " + result + "; buffer length = " + myInBufferLength);
			}
			final int out = (int)result & 0xFFFF;
			myInBufferOffset += in;
			myInBufferLength -= in;
			myOutBufferOffset = 0;
			myOutBufferLength = out;
			if ((result & (1L << 32)) != 0) {
				endInflating(myInflatorId);
				myInflatorId = -1;
				myStream.backSkip(myInBufferLength);
				break;
			}
		}
	}

	private native int startInflating();
	private native void endInflating(int inflatorId);
	private native long inflate(int inflatorId, byte[] in, int inOffset, int inLength, byte[] out);
}
