package org.zlibrary.options.config.reader;

import java.io.File;

import org.zlibrary.options.ZLConfig;

public interface ZLReadable {
	public ZLConfig readFile(File file);
}
