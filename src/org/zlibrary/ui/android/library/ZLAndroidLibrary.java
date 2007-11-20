package org.zlibrary.ui.android.library;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
//import org.zlibrary.options.config.reader.ZLConfigReaderFactory;
//import org.zlibrary.options.config.writer.ZLConfigWriterFactory;

import org.zlibrary.ui.android.view.ZLAndroidPaintContext;
import org.zlibrary.ui.android.view.ZLAndroidWidget;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public class ZLAndroidLibrary extends ZLibrary {
	private ZLActivity myActivity;

	public ZLAndroidPaintContext createPaintContext() {
		ZLAndroidWidget widget = (ZLAndroidWidget)myActivity.findViewById(R.id.zlactivity);
		return widget.getPaintContext();
	}

	public String getApplicationName() {
		// TODO: read from data/application.xml
		return "FBReaderJ";
	}

	private static String configDirectory() {
		return System.getProperty("user.home") + "/." + getInstance().getApplicationName();
	}

	public static void shutdown() {
		//ZLConfigWriterFactory.createConfigWriter(configDirectory()).write();
	}

	void run(ZLActivity activity) {
		new ZLSaxXMLProcessorFactory();

		myActivity = activity;
		myActivity.setContentView(R.layout.main);
		//ZLConfigReaderFactory.createConfigReader(configDirectory()).read();

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
			application = (ZLApplication)applicationClass.newInstance();
		} catch (Exception e) {
			exitOnException(e);
		}

		ZLAndroidApplicationWindow mainWindow = new ZLAndroidApplicationWindow(application);
		application.initWindow();
	}

	private void exitOnException(Exception e) {
		shutdown();
		System.exit(0);
	}
}
