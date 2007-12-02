package org.zlibrary.core.options.config;

public final class ZLConfigReaderFactory {
	public static ZLReader createConfigReader(String path){
		return new ZLConfigReader(path);
	}
}
