package org.amse.ys.zip;

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
            System.arraycopy(myBuffer, from, buffer, offset, firstPart);
            System.arraycopy(myBuffer, 0, buffer, offset + firstPart, secondPart);
            myCurrentPosition = secondPart;
        } else {
            System.arraycopy(myBuffer, from, buffer, offset, length);
            myCurrentPosition = from + length;
        }
        myBytesReady -= length;
    }

    public byte read() {
        if (myBytesReady == 0) {
            throw new RuntimeException("reading from empty buffer");
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

    public void repeat(int length, int distance) {
        if (myBytesReady + length > DICTIONARY_LENGTH) {
            throw new RuntimeException("circular buffer overflow");
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
