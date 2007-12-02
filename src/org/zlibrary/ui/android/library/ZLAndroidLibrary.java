package org.zlibrary.ui.android.library;

import java.io.InputStream;

import android.content.Resources;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
//import org.zlibrary.options.config.reader.ZLConfigReaderFactory;
//import org.zlibrary.options.config.writer.ZLConfigWriterFactory;

import org.zlibrary.ui.android.view.ZLAndroidPaintContext;
import org.zlibrary.ui.android.view.ZLAndroidWidget;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public final class ZLAndroidLibrary extends ZLibrary {
	private ZLAndroidActivity myActivity;
	private ZLAndroidApplicationWindow myMainWindow;

	public ZLAndroidPaintContext createPaintContext() {
		ZLAndroidWidget widget = (ZLAndroidWidget)myActivity.findViewById(R.id.zlandroidactivity);
		return widget.getPaintContext();
	}

	ZLAndroidApplicationWindow getMainWindow() {
		return myMainWindow;
	}

	public InputStream getResourceInputStream(String fileName) {
		final String fieldName = fileName.replace("/", "__").replace(".", "_").toLowerCase();
		int resourceId;
		try {
			resourceId = R.raw.class.getField(fieldName).getInt(null);
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		return myActivity.getResources().openRawResource(resourceId);
	}

	/*
	private static String configDirectory() {
		return System.getProperty("user.home") + "/." + getInstance().getApplicationName();
	}
	*/

	public static void shutdown() {
		//ZLConfigWriterFactory.createConfigWriter(configDirectory()).write();
	}

	void run(ZLAndroidActivity activity) {
		myActivity = activity;

		new ZLSaxXMLProcessorFactory();
		loadProperties();

		myActivity.setContentView(R.layout.main);
		//ZLConfigReaderFactory.createConfigReader(configDirectory()).read();

		try {
			ZLApplication application = (ZLApplication)getApplicationClass().newInstance();
			myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} catch (Exception e) {
			shutdown();
			System.exit(0);
		}
	}
}
