package org.zlibrary.core.filesystem;

public interface ZLOutputStream {
	boolean open();
	void write(String str);
	void close();

}
