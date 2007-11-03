package org.zlibrary.core.library;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLPaintContext;

import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

public class ZLibrary {
	public static void init() {
	}

	public static void shutdown() {
	}

	public static void run(ZLApplication application) {
		ZLSwingApplicationWindow mainWindow = new ZLSwingApplicationWindow(application);
		application.initWindow();
		mainWindow.run();
	}

	public static ZLPaintContext getContext() {
		return ZLSwingLibrary.CONTEXT;
	}
}
