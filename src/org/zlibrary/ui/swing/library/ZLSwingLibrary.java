package org.zlibrary.ui.swing.library;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.options.config.reader.ZLConfigReaderFactory;
import org.zlibrary.options.config.writer.ZLConfigWriterFactory;

import org.zlibrary.ui.swing.view.ZLSwingPaintContext;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;

public class ZLSwingLibrary extends ZLibrary {
	public ZLSwingPaintContext createPaintContext() {
		return new ZLSwingPaintContext();
	}

	public String getApplicationName() {
		// TODO: read from data/application.xml
		return "FBReaderJ";
	}

	private static String configDirectory() {
		return System.getProperty("user.home") + "/." + getInstance().getApplicationName();
	}

	public static void shutdown() {
		ZLConfigWriterFactory.createConfigWriter(configDirectory()).write();
	}

	void run(String[] args) {
		ZLConfigReaderFactory.createConfigReader(configDirectory()).read();

		// TODO: read from data/application.xml
		String applicationClassName = "org.fbreader.fbreader.FBReader";
		Class applicationClass = null;
		try {
			applicationClass = Class.forName(applicationClassName);
		} catch (ClassNotFoundException e) {
			exitOnException(e);
		}
		ZLApplication application = null;
		try {
			java.lang.reflect.Constructor constructor = applicationClass.getConstructor(String[].class);
			application = (ZLApplication)constructor.newInstance(new Object[] { args });
		} catch (Exception e) {
			exitOnException(e);
		}

		ZLSwingApplicationWindow mainWindow = new ZLSwingApplicationWindow(application);
		application.initWindow();
		mainWindow.run();
	}

	private void exitOnException(Exception e) {
		e.printStackTrace();
		shutdown();
		System.exit(0);
	}
}
