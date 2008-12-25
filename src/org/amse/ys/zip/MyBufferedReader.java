package org.amse.ys.zip;

import java.io.*;

final class MyBufferedReader extends InputStream {
    private FileInputStream myFile;
    private final String myFileName;
    private final byte[] myBuffer;
    int myBytesReady;
    int myPositionInBuffer;

    private int myCurrentPosition;
    
    public MyBufferedReader(String s, int arraySize) throws IOException {
        myFileName = s;
        myFile = new FileInputStream(s);
        myBuffer = new byte[arraySize];
        myBytesReady = 0;
        myPositionInBuffer = 0;
    }

    public MyBufferedReader(String s) throws IOException {
        this(s, 1 << 10);
    }

    public int available() throws IOException {
        return (myFile.available() + myBytesReady);
    }

    public void reset() throws IOException {
        setPosition(0);
    }

    public int read() throws IOException {        
        myCurrentPosition++;
        if (myBytesReady == 0) {
            myPositionInBuffer = 0;
            myBytesReady = myFile.read(myBuffer);
            if (myBytesReady == 0) {
                return -1;
            }
        }        
        myBytesReady--;        
        return (myBuffer[myPositionInBuffer++] & 255);        
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
               	myBytesReady = myFile.read(myBuffer);
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
            myFile.close();
            myFile = new FileInputStream(myFileName);
            myBytesReady = 0;
            skip(position);
        }
        myCurrentPosition = position;
    }

    public void close() throws IOException {
        myFile.close();
        myBytesReady = 0;
    }

}
