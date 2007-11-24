package org.zlibrary.core.options.config.writer;

public class ZLConfigWriterFactory {
	public static ZLWriter createConfigWriter(String path){
		return new ZLConfigWriter(path);
	}
}
