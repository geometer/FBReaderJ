package org.amse.ys.zip;

import java.io.*;

final class MyBufferedInputStream extends InputStream {
    private FileInputStream myFileInputStream;
    private final String myFileName;
    private final byte[] myBuffer;
    int myBytesReady;
    int myPositionInBuffer;
    private int myCurrentPosition;
    
    public MyBufferedInputStream(String s, int arraySize) throws IOException {
        myFileName = s;
        myFileInputStream = new FileInputStream(s);
        myBuffer = new byte[arraySize];
        myBytesReady = 0;
        myPositionInBuffer = 0;
    }

    public MyBufferedInputStream(String s) throws IOException {
        this(s, 1 << 10);
    }

    public int available() throws IOException {
        return (myFileInputStream.available() + myBytesReady);
    }

    int offset() {
        return myCurrentPosition;
    }

    public void reset() throws IOException {
        setPosition(0);
    }

    public int read() throws IOException {        
        myCurrentPosition++;
        if (myBytesReady == 0) {
            myPositionInBuffer = 0;
            myBytesReady = myFileInputStream.read(myBuffer);
            if (myBytesReady == 0) {
                return -1;
            }
        }        
        myBytesReady--;        
        return (myBuffer[myPositionInBuffer++] & 255);        
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
        while (n > 0) {
            if (myBytesReady > n) {
                myBytesReady -= n;
                myPositionInBuffer += n;
                n = 0;
            } else {
                n -= myBytesReady;
                myPositionInBuffer = 0;
                myBytesReady = myFileInputStream.read(myBuffer);
                if (myBytesReady <= 0) {
                    break;
                }
            }        
        }
    }

    public void backSkip(int n) {
        throw new RuntimeException("back skip not implemented");
    }

    public void setPosition(int position) throws IOException {
        if (myCurrentPosition < position) {
            skip(position - myCurrentPosition);
        } else {
            myFileInputStream.close();
            myFileInputStream = new FileInputStream(myFileName);
            myBytesReady = 0;
            skip(position);
        }
        myCurrentPosition = position;
    }

    public void close() throws IOException {
        myFileInputStream.close();
        myBytesReady = 0;
    }

}
