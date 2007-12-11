package org.zlibrary.ui.swing.library;

import java.io.InputStream;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.config.ZLConfigReaderFactory;
import org.zlibrary.core.options.config.ZLConfigWriterFactory;
import org.zlibrary.core.resources.ZLResourceKey;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.dialogs.ZLDialogManager;

import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;

import org.zlibrary.ui.swing.view.ZLSwingPaintContext;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;
import org.zlibrary.ui.swing.dialogs.ZLSwingDialogManager;

public class ZLSwingLibrary extends ZLibrary {
	public ZLSwingPaintContext createPaintContext() {
		return new ZLSwingPaintContext();
	}

	public InputStream getResourceInputStream(String fileName) {
		return getClass().getClassLoader().getResourceAsStream(fileName);
	}

	private static String configDirectory() {
		return System.getProperty("user.home") + "/." + getInstance().getApplicationName();
	}

	public static void shutdown() {
		ZLConfigWriterFactory.createConfigWriter(configDirectory()).write();
	}

	public void run(String[] args) {
		ZLSwingDialogManager.createInstance();
		
		//new ZLSaxXMLProcessorFactory();
		new ZLOwnXMLProcessorFactory();
		loadProperties();

		ZLConfigReaderFactory.createConfigReader(configDirectory()).read();

		ZLApplication application = null;
		try {
			application = (ZLApplication)getApplicationClass().getConstructor(String[].class).newInstance(new Object[] { args });
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
			System.exit(0);
		}

		ZLSwingApplicationWindow mainWindow = new ZLSwingApplicationWindow(application);
		application.initWindow();
		mainWindow.run();
		
//		ZLDialogManager.getInstance().errorBox(new ZLResourceKey("noHelpBox"));
	}
}
