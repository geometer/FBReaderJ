package org.zlibrary.core.options.config;

public final class ZLConfigWriterFactory {
	public static ZLWriter createConfigWriter(String path) {
		return new ZLConfigWriter(path);
	}
}
