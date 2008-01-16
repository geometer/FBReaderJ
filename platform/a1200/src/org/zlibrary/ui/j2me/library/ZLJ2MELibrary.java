package org.zlibrary.ui.j2me.library;

import java.io.InputStream;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

//import android.content.Resources;
//import android.content.Intent;
//import android.net.ContentURI;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.ZLApplication;

//import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;

import org.zlibrary.ui.j2me.config.ZLJ2MEConfigManager;
import org.zlibrary.ui.j2me.view.ZLJ2MEPaintContext;
import org.zlibrary.ui.j2me.view.ZLCanvas;
//import org.zlibrary.ui.android.view.ZLAndroidWidget;
//import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
//import org.zlibrary.ui.android.image.ZLAndroidImageManager;

final class ZLJ2MELibrary extends ZLibrary {
	public ZLPaintContext createPaintContext() {
		// TODO: implement
		return new ZLJ2MEPaintContext();
	}

	public InputStream getResourceInputStream(String fileName) {
		return getClass().getResourceAsStream("/" + fileName);
	}

/*
	public static void shutdown() {
		ZLSQLiteConfigManager.release();
	}

	public void finish() {
		shutdown();
		if (myActivity != null) {
			myActivity.finish();
		}
	}
*/

	public void openInBrowser(String reference) {
		// TODO: implement
	}

	void run(MIDlet midlet) {
		new ZLOwnXMLProcessorFactory();
		new ZLJ2MEConfigManager();
		loadProperties();
		//new ZLAndroidImageManager();

		try {
			ZLApplication application = (ZLApplication)getApplicationClass().newInstance();
			//myMainWindow = new ZLAndroidApplicationWindow(application);
			//application.initWindow();
		} catch (Exception e) {
			e.printStackTrace();
			//finish();
		}
		ZLCanvas canvas = new ZLCanvas();
		Display.getDisplay(midlet).setCurrent(canvas);
	}
}
