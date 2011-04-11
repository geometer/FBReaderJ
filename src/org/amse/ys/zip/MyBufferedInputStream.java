package org.amse.ys.zip;

import java.io.*;

final class MyBufferedInputStream extends InputStream {
	private final ZipFile.InputStreamHolder myStreamHolder;
	private InputStream myFileInputStream;
	private final byte[] myBuffer;
	int myBytesReady;
	int myPositionInBuffer;
	private int myCurrentPosition;

	public MyBufferedInputStream(ZipFile.InputStreamHolder streamHolder, int bufferSize) throws IOException {
		myStreamHolder = streamHolder;
		myFileInputStream = streamHolder.getInputStream();
		myBuffer = new byte[bufferSize];
		myBytesReady = 0;
		myPositionInBuffer = 0;
	}

	public MyBufferedInputStream(ZipFile.InputStreamHolder streamHolder) throws IOException {
		this(streamHolder, 1 << 10);
	}

	public int available() throws IOException {
		return (myFileInputStream.available() + myBytesReady);
	}

	int offset() {
		return myCurrentPosition;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int ready = (len < myBytesReady) ? len : myBytesReady;
		if (ready > 0) {
			System.arraycopy(myBuffer, myPositionInBuffer, b, off, ready);
			len -= ready;
			myBytesReady -= ready;
			myPositionInBuffer += ready;
			off += ready;
		}
		if (len > 0) {
			final int ready2 = myFileInputStream.read(b, off, len);
			if (ready2 >= 0) {
				ready += ready2;
			}
		}
		myCurrentPosition += ready;
		return (ready > 0) ? ready : -1;
	}

	public int read() throws IOException {
		myCurrentPosition++;
		if (myBytesReady <= 0) {
			myPositionInBuffer = 0;
			myBytesReady = myFileInputStream.read(myBuffer);
			if (myBytesReady <= 0) {
				return -1;
			}
		}
		myBytesReady--;
		return myBuffer[myPositionInBuffer++] & 255;
	}

	int read2Bytes() throws IOException {
		int low = read();
		int high = read();
		if (high < 0) {
			throw new IOException("unexpected end of file at position " + offset());
		}
		return (high << 8) + low;
	}

	int read4Bytes() throws IOException {
		int firstByte = read();
		int secondByte = read();
		int thirdByte = read();
		int fourthByte = read();
		if (fourthByte < 0) {
			throw new IOException("unexpected end of file at position " + offset());
		}
		return (fourthByte << 24) + (thirdByte << 16) + (secondByte << 8) + firstByte;
	}

	String readString(int stringLength) throws IOException {
		char[] array = new char[stringLength];
		for (int i = 0; i < stringLength; i++) {
			array[i] = (char)read();
		}
		return new String(array);
	}

	public void skip(int n) throws IOException {
		myCurrentPosition += n;
		if (myBytesReady >= n) {
			myBytesReady -= n;
			myPositionInBuffer += n;
		} else {
			n -= myBytesReady;
			myBytesReady = 0;

			if (n > myFileInputStream.available()) {
				throw new IOException("Not enough bytes to read");
			}
			n -= myFileInputStream.skip(n);
			while (n > 0) {
				int skipped = myFileInputStream.read(myBuffer, 0, Math.min(n, myBuffer.length));
				if (skipped <= 0) {
					throw new IOException("Not enough bytes to read");
				}
				n -= skipped;
			}
		}
	}

	public void backSkip(int n) throws IOException {
		if (n <= 0) {
			return;
		}
		myFileInputStream.close();
		myFileInputStream = myStreamHolder.getInputStream();
		myBytesReady = 0;
		myPositionInBuffer = 0;
		int position = myCurrentPosition - n;
		myCurrentPosition = 0;
		skip(position);
	}

	public void setPosition(int position) throws IOException {
		if (myCurrentPosition < position) {
			skip(position - myCurrentPosition);
		} else {
			backSkip(myCurrentPosition - position);
		}
	}

	/*
	public void setPosition(int position) throws IOException {
		if (myCurrentPosition < position) {
			skip(position - myCurrentPosition);
		} else {
			myFileInputStream.close();
			myFileInputStream = myStreamHolder.getInputStream();
			myBytesReady = 0;
			skip(position);
			myCurrentPosition = position;
		}
	}
	*/

	public void close() throws IOException {
		myFileInputStream.close();
		myBytesReady = 0;
	}
}
