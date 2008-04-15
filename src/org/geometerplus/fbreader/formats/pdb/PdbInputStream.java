package org.geometerplus.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class PdbInputStream extends InputStream {
	private final InputStream myBase;
	private int myOffset = 0;
	private final int mySize;
	
	public PdbInputStream(ZLFile file) throws IOException {
		mySize = (int)file.size();
		myBase = file.getInputStream();
	}
	
	public int read() throws IOException {
		int result = myBase.read();
		if (result != -1) {
			myOffset ++;
		}
		return result;
	}

	public int available() throws IOException {
		return super.available();
	}

	public void close() throws IOException {
		myOffset = 0;
		super.close();
	}

	public synchronized void mark(int readlimit) {
		super.mark(readlimit);
	}

	public boolean markSupported() {
		return super.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return super.read(b);
	}

	public synchronized void reset() throws IOException {
//		myOffset = 0;
		super.reset();
	}

	public int offset() {
		return myOffset;
	}
	
	public int sizeOfOpened() {
		return mySize - myOffset;
	}
}
