package org.amse.ys.zip;

import java.io.IOException;

abstract class AbstractDeflatingDecompressor extends Decompressor {
    abstract void reset(MyBufferedInputStream inputStream, LocalFileHeader header) throws IOException;
}
