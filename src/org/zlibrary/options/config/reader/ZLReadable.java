package org.zlibrary.options.config.reader;

import java.io.*;

import org.zlibrary.options.config.*;

public interface ZLReadable {
	public ZLConfig read(InputStream input);
}
