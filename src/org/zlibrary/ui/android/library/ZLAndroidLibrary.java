package org.zlibrary.ui.android.library;

import java.io.InputStream;

import android.content.Resources;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.application.ZLApplication;

//import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.zlibrary.ui.android.sqliteconfig.ZLSQLiteConfigManager;

import org.zlibrary.ui.android.view.ZLAndroidPaintContext;
import org.zlibrary.ui.android.view.ZLAndroidWidget;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.zlibrary.ui.android.image.ZLAndroidImageManager;

public final class ZLAndroidLibrary extends ZLibrary {
	private ZLAndroidActivity myActivity;
	private ZLAndroidApplicationWindow myMainWindow;
	private ZLAndroidWidget myWidget;

	public ZLAndroidPaintContext createPaintContext() {
		return getWidget().getPaintContext();
	}

	ZLAndroidApplicationWindow getMainWindow() {
		return myMainWindow;
	}

	public ZLAndroidWidget getWidget() {
		if (myWidget == null) {
			myWidget = (ZLAndroidWidget)myActivity.findViewById(R.id.zlandroidactivity);
		}
		return myWidget;
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

	public static void shutdown() {
		ZLSQLiteConfigManager.release();
	}

	public void finish() {
		shutdown();
		if (myActivity != null) {
			myActivity.finish();
		}
	}

	void run(ZLAndroidActivity activity) {
		myActivity = activity;

		//new ZLSaxXMLProcessorFactory();
		new ZLOwnXMLProcessorFactory();
		new ZLSQLiteConfigManager();
		loadProperties();
		new ZLAndroidImageManager();

		activity.setContentView(R.layout.main);

		try {
			ZLApplication application = (ZLApplication)getApplicationClass().newInstance();
			myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} catch (Exception e) {
			finish();
		}
	}
}
