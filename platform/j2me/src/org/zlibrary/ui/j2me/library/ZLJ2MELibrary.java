/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

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
