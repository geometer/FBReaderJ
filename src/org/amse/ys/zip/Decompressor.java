package org.amse.ys.zip;

import java.util.*;
import java.io.*;

public abstract class Decompressor {
    public Decompressor(MyBufferedInputStream is, LocalFileHeader header) {
    }

    public abstract int read(byte b[], int off, int len) throws IOException;
    public abstract int read() throws IOException;

    protected Decompressor() {
    }

    private static Queue<AbstractDeflatingDecompressor> ourDeflators = new LinkedList<AbstractDeflatingDecompressor>();

    static void storeDecompressor(Decompressor decompressor) {
        if (decompressor instanceof AbstractDeflatingDecompressor) {
            synchronized (ourDeflators) {
                ourDeflators.add((AbstractDeflatingDecompressor)decompressor);
            }
        }
    }

    public static Decompressor init(MyBufferedInputStream is, LocalFileHeader header) throws ZipException {
        switch (header.CompressionMethod) {
        case 0:
            return new NoCompressionDecompressor(is, header);
        case 8:
            synchronized (ourDeflators) {
                if (!ourDeflators.isEmpty()) {
                    AbstractDeflatingDecompressor decompressor = ourDeflators.poll();
                    decompressor.reset(is, header);
                    return decompressor;
                }
            }
            return 
				NativeDeflatingDecompressor.INITIALIZED
					? new NativeDeflatingDecompressor(is, header)
					: new DeflatingDecompressor(is, header);
        default:
            throw new ZipException("Unsupported method of compression");
        }
    }
    
    public int available() {
        return -1;
    }
}
