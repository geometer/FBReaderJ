package org.zlibrary.core.options.config.reader;

public class ZLConfigReaderFactory {
	public static ZLReader createConfigReader(String path){
		return new ZLConfigReader(path);
	}
}
