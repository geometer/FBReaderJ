package org.amse.ys.zip;

abstract class AbstractDeflatingDecompressor extends Decompressor {
    abstract void reset(MyBufferedInputStream inputStream, LocalFileHeader header);
}
