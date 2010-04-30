package org.amse.ys.zip;

import java.io.IOException;

final class CircularBuffer {
    static final int DICTIONARY_LENGTH = (1 << 15);
    private static final int DICTIONARY_MASK = DICTIONARY_LENGTH - 1;

    private final byte[] myBuffer = new byte[DICTIONARY_LENGTH];
    private int myBytesReady; // number of bytes can be read
    private int myCurrentPosition; // the next byte to read is
                           // myDictionary[myCurrentPosition]

        void reset() {
                myBytesReady = 0;
                myCurrentPosition = 0;
        }

    public int available() {
        return myBytesReady;
    }

    public void read(byte[] buffer, int offset, int length) {
        int from = myCurrentPosition;
        if (from + length > DICTIONARY_LENGTH) {
            final int firstPart = DICTIONARY_LENGTH - from;
            final int secondPart = length - firstPart;
            if (buffer != null) {
                System.arraycopy(myBuffer, from, buffer, offset, firstPart);
                System.arraycopy(myBuffer, 0, buffer, offset + firstPart, secondPart);
            }
            myCurrentPosition = secondPart;
        } else {
            if (buffer != null) {
                System.arraycopy(myBuffer, from, buffer, offset, length);
            }
            myCurrentPosition = from + length;
        }
        myBytesReady -= length;
    }

    public byte read() throws IOException {
        if (myBytesReady == 0) {
            throw new ZipException("reading from empty buffer");
        }
        final byte result = myBuffer[myCurrentPosition++];
        myCurrentPosition &= DICTIONARY_MASK;
        myBytesReady--;
        return result;
    }

    public void writeByte(byte toWrite) {
        myBuffer[(myCurrentPosition + myBytesReady) & DICTIONARY_MASK] = toWrite;
        myBytesReady++;
    }

    public void repeat(int length, int distance) throws IOException {
        if (myBytesReady + length > DICTIONARY_LENGTH) {
            throw new ZipException("circular buffer overflow");
        }
        int writePoint = (myCurrentPosition + myBytesReady) & DICTIONARY_MASK;
        int readPoint = (writePoint - distance) & DICTIONARY_MASK;
        for (int i = 0; i < length; i++) {
            myBuffer[writePoint++] = myBuffer[readPoint++];
            writePoint &= DICTIONARY_MASK;
            readPoint &= DICTIONARY_MASK;
        }
        myBytesReady += length;
    }
}
