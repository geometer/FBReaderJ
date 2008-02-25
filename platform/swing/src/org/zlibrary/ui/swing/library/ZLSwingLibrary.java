package org.zlibrary.ui.swing.library;

import java.io.*;
import javax.swing.UIManager;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.xmlconfig.ZLXMLConfigManager;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.dialogs.ZLDialogManager;

//import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;

import org.zlibrary.ui.swing.view.ZLSwingPaintContext;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;
import org.zlibrary.ui.swing.dialogs.ZLSwingDialogManager;
import org.zlibrary.ui.swing.image.ZLSwingImageManager;

public class ZLSwingLibrary extends ZLibrary {
	public ZLSwingPaintContext createPaintContext() {
		return new ZLSwingPaintContext();
	}

	protected InputStream getFileInputStream(String fileName) {
		try {
			return new BufferedInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	protected InputStream getResourceInputStream(String fileName) {
		return getClass().getClassLoader().getResourceAsStream(fileName);
	}

	public static void shutdown() {
		ZLXMLConfigManager.release();
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	public void run(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", getApplicationName());
		//new ZLSaxXMLProcessorFactory();
		new ZLOwnXMLProcessorFactory();
		loadProperties();

		new ZLXMLConfigManager(System.getProperty("user.home") + "/." + getInstance().getApplicationName());
		new ZLSwingImageManager();
		new ZLSwingDialogManager();

		ZLApplication application = null;
		try {
			application = (ZLApplication)getApplicationClass().getConstructor(String[].class).newInstance(new Object[] { args });
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
		}

		ZLSwingApplicationWindow mainWindow =
			((ZLSwingDialogManager)ZLSwingDialogManager.getInstance()).createApplicationWindow(application);
		application.initWindow();
		mainWindow.run();
		
//		ZLDialogManager.getInstance().errorBox(new "noHelpBox");
	}

	public void openInBrowser(String reference) {
		String os = System.getProperty("os.name");
		try {
			if (os.startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + reference);
			} else if (os.startsWith("Mac OS")) {
				Class fileManager = Class.forName("com.apple.eio.FileManager");
				java.lang.reflect.Method openURL = fileManager.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { reference });
			} else { // os is UNIX
				Runtime.getRuntime().exec(new String[] { "iceweasel", reference });
			}
		} catch (Exception e) {
			System.out.println("to open in browser: " + reference);
			e.printStackTrace();
		}
	}
	
	/*public void init() {
		ZLibrary.FileNameDelimiter = "\\";
		ZLibrary.PathDelimiter = ";";
	}*/
}
