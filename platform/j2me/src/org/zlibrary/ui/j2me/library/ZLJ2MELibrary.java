package org.geometerplus.zlibrary.ui.j2me.library;

import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.*;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;

import org.geometerplus.zlibrary.ui.j2me.config.ZLJ2MEConfigManager;
import org.geometerplus.zlibrary.ui.j2me.view.ZLCanvas;
import org.geometerplus.zlibrary.ui.j2me.application.ZLJ2MEApplicationWindow;
import org.geometerplus.zlibrary.ui.j2me.image.ZLJ2MEImageManager;

final class ZLJ2MELibrary extends ZLibrary {
	private ZLCanvas myCanvas;

	public ZLPaintContext createPaintContext() {
		return myCanvas.getContext();
	}

	protected InputStream getFileInputStream(String fileName) {
		// TODO: implement
		try {
			FileConnection connection = (FileConnection)Connector.open("file:///Memory Card" + fileName, Connector.READ);
			if ((connection != null) && connection.exists()) {
				return connection.openInputStream();
			}
		} catch (Exception e) {
		}
		return null;
	}

	protected InputStream getResourceInputStream(String fileName) {
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
			myCanvas.setApplication(application);
			application.initWindow();
		} catch (Exception e) {
			e.printStackTrace();
			//finish();
		}

		Display.getDisplay(midlet).setCurrent(myCanvas);
	}
}
