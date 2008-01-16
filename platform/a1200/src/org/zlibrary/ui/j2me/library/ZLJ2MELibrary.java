package org.zlibrary.ui.j2me.library;

import java.io.InputStream;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;

import org.zlibrary.ui.j2me.config.ZLJ2MEConfigManager;
import org.zlibrary.ui.j2me.view.ZLCanvas;
import org.zlibrary.ui.j2me.application.ZLJ2MEApplicationWindow;
import org.zlibrary.ui.j2me.image.ZLJ2MEImageManager;

final class ZLJ2MELibrary extends ZLibrary {
	private ZLCanvas myCanvas;

	public ZLPaintContext createPaintContext() {
		return myCanvas.getContext();
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
		new ZLJ2MEImageManager();

		myCanvas = new ZLCanvas();

		try {
			ZLApplication application = (ZLApplication)getApplicationClass().newInstance();
			new ZLJ2MEApplicationWindow(application, myCanvas);
			application.initWindow();
		} catch (Exception e) {
			e.printStackTrace();
			//finish();
		}

		Display.getDisplay(midlet).setCurrent(myCanvas);
	}
}
