package org.amse.ys.zip;

import java.io.*;

public class NoCompressionDecompressor extends Decompressor {
    private final LocalFileHeader myHeader;
    private final MyBufferedInputStream myStream;
    private int myCurrentPosition;

    public NoCompressionDecompressor(MyBufferedInputStream is, LocalFileHeader header) {
        super();
        myHeader = header;
        myStream = is;
    }

    public int read(byte b[], int off, int len) throws IOException {
        int i = 0;
        for (; i < len; ++i) {
            int value = read();
            if (value == -1) {
                break;
            }
			if (b != null) {
            	b[off + i] = (byte)value;
			}
        }
        return (i > 0) ? i : -1;
    }

    public int read() throws IOException {
        if (myCurrentPosition < myHeader.CompressedSize) {
            myCurrentPosition++;
            return myStream.read();
        } else {
            return -1;
        }
    }
    
    public int available() throws IOException {
        return (myHeader.UncompressedSize - myCurrentPosition);
    }
}
