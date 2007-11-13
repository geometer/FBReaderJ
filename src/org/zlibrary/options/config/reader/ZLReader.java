package org.zlibrary.options.config.reader;

import java.io.File;

import org.zlibrary.options.config.ZLConfig;

public interface ZLReader {
	public ZLConfig read(File file);
}
