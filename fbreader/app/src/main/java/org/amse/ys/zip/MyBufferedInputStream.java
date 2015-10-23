package org.amse.ys.zip;

import java.io.*;

import org.geometerplus.zlibrary.core.util.InputStreamHolder;

final class MyBufferedInputStream extends InputStream {
	private final InputStreamHolder myStreamHolder;
	private InputStream myFileInputStream;
	private final byte[] myBuffer;
	int myBytesReady;
	int myPositionInBuffer;
	private int myCurrentPosition;

	public MyBufferedInputStream(InputStreamHolder streamHolder, int bufferSize) throws IOException {
		myStreamHolder = streamHolder;
		myFileInputStream = streamHolder.getInputStream();
		myBuffer = new byte[bufferSize];
		myBytesReady = 0;
		myPositionInBuffer = 0;
	}

	public MyBufferedInputStream(InputStreamHolder streamHolder) throws IOException {
		this(streamHolder, 1 << 10);
	}

	@Override
	public int available() throws IOException {
		return (myFileInputStream.available() + myBytesReady);
	}

	int offset() {
		return myCurrentPosition;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ready = (len < myBytesReady) ? len : myBytesReady;
		if (ready > 0) {
			if (b != null) {
				System.arraycopy(myBuffer, myPositionInBuffer, b, off, ready);
			}
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
		return ready > 0 ? ready : -1;
	}

	@Override
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
			throw new ZipException("unexpected end of file at position " + offset());
		}
		return (high << 8) + low;
	}

	int read4Bytes() throws IOException {
		int firstByte = read();
		int secondByte = read();
		int thirdByte = read();
		int fourthByte = read();
		if (fourthByte < 0) {
			throw new ZipException("unexpected end of file at position " + offset());
		}
		return (fourthByte << 24) + (thirdByte << 16) + (secondByte << 8) + firstByte;
	}

	private static final boolean isUtf8String(byte[] array) {
		int nonLeadingCharsCounter = 0;
		for (byte b : array) {
			if (nonLeadingCharsCounter == 0) {
				if ((b & 0x80) != 0) {
					if ((b & 0xE0) == 0xC0) {
						nonLeadingCharsCounter = 1;
					} else if ((b & 0xF0) == 0xE0) {
						nonLeadingCharsCounter = 2;
					} else if ((b & 0xF8) == 0xF0) {
						nonLeadingCharsCounter = 3;
					} else {
						return false;
					}
				}
			} else {
				if ((b & 0xC0) != 0x80) {
					return false;
				}
				--nonLeadingCharsCounter;
			}
		}
		return nonLeadingCharsCounter == 0;
	}

	String readString(int stringLength) throws IOException {
		final byte[] array = new byte[stringLength];
		read(array);
		if (isUtf8String(array)) {
			return new String(array, "utf-8");
		}

		final char[] chars = new char[stringLength];
		for (int i = 0; i < stringLength; i++) {
			chars[i] = (char)(array[i] & 0xFF);
		}
		return new String(chars);
	}

	@Override
	public long skip(long n) throws IOException {
		if (myBytesReady >= n) {
			myBytesReady -= n;
			myPositionInBuffer += n;
			myCurrentPosition += n;
			return n;
		} else {
			long left = n - myBytesReady;
			myBytesReady = 0;

			left -= myFileInputStream.skip(left);
			while (left > 0) {
				int skipped = myFileInputStream.read(myBuffer, 0, Math.min((int)left, myBuffer.length));
				if (skipped <= 0) {
					break;
				}
				left -= skipped;
			}
			myCurrentPosition += n - left;
			return n - left;
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

	@Override
	public void close() throws IOException {
		myFileInputStream.close();
		myBytesReady = 0;
	}
}
