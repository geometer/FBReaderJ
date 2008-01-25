package org.zlibrary.core.filesystem;

public interface ZLInputStream {
	boolean open();
	int read(String buffer, int maxSize);
	void close();

	void seek(int offset, boolean absoluteOffset);
	int offset();
	int sizeOfOpened();
}
