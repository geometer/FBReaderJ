package org.amse.ys.zip;

import java.util.*;
import java.io.*;

public abstract class Decompressor {
	public Decompressor(MyBufferedInputStream is, LocalFileHeader header) {
	}

	/**
	 * byte b[] -- target buffer for bytes; might be null
	 */
	public abstract int read(byte b[], int off, int len) throws IOException;

	public abstract int read() throws IOException;

	protected Decompressor() {
	}

	private static Queue<DeflatingDecompressor> ourDeflators = new LinkedList<DeflatingDecompressor>();

	static void storeDecompressor(Decompressor decompressor) {
		if (decompressor instanceof DeflatingDecompressor) {
			synchronized (ourDeflators) {
				ourDeflators.add((DeflatingDecompressor)decompressor);
			}
		}
	}

	static Decompressor init(MyBufferedInputStream is, LocalFileHeader header) throws IOException {
		switch (header.CompressionMethod) {
			case 0:
				return new NoCompressionDecompressor(is, header);
			case 8:
				synchronized (ourDeflators) {
					if (!ourDeflators.isEmpty()) {
						DeflatingDecompressor decompressor = ourDeflators.poll();
						decompressor.reset(is, header);
						return decompressor;
					}
				}
				return new DeflatingDecompressor(is, header);
			default:
				throw new ZipException("Unsupported method of compression");
		}
	}

	public int available() throws IOException {
		return -1;
	}
}
