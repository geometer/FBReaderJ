package org.amse.ys.zip;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ZipInputStream extends InputStream {
    private final MyBufferedInputStream myBaseStream;
    private final LocalFileHeader myHeader;
    private final Decompressor myDecompressor;

    public ZipInputStream(String fileName, LocalFileHeader header) throws IOException, WrongZipFormatException {
        if (!header.sizeIsKnown()) {
            throw new RuntimeException("Reading of files with flag 3 is not implemented yet");
        }
        myBaseStream = new MyBufferedInputStream(fileName);
        myBaseStream.setPosition(header.OffsetOfLocalData);
        myHeader = header;
        myDecompressor = Decompressor.init(myBaseStream, header);
    }

    public int available() {
        return myDecompressor.available();
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        try {
            return myDecompressor.read(b, off, len);
        } catch (WrongZipFormatException e) {
            throw new IOException("when reading occured exception " + e.getMessage());
        }
    }

    public int read() throws IOException {
        try {
            return myDecompressor.read();
        } catch (WrongZipFormatException e) {
            throw new IOException("when reading occured exception " + e.getMessage());
        }
    }

    public void close() throws IOException {
        myBaseStream.close();
    }
}
