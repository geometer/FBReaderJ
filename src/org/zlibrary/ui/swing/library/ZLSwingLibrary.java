package org.zlibrary.ui.swing.library;

import org.zlibrary.ui.swing.view.ZLSwingPaintContext;
import org.zlibrary.options.config.reader.ZLConfigReaderFactory;
import org.zlibrary.options.config.writer.ZLConfigWriterFactory;

public class ZLSwingLibrary {
	final public static ZLSwingPaintContext CONTEXT = new ZLSwingPaintContext();

	private static String configDirectory() {
		return System.getProperty("user.home") + "/.FBReaderJ";
	}

	public static void init() {
		ZLConfigReaderFactory.createConfigReader(configDirectory()).read();
	}

	public static void shutdown() {
		ZLConfigWriterFactory.createConfigWriter(configDirectory()).write();
	}
}
