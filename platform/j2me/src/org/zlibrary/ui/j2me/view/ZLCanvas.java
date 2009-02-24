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

package org.geometerplus.zlibrary.ui.j2me.view;

import javax.microedition.lcdui.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ZLCanvas extends Canvas {
	private final ZLJ2MEPaintContext myContext = new ZLJ2MEPaintContext(this);
	private ZLJ2MEViewWidget myWidget;
	private ZLApplication myApplication;

	void setViewWidget(ZLJ2MEViewWidget widget) {
		myWidget = widget;
	}

	public void setApplication(ZLApplication application) {
		myApplication = application;
	}

	public ZLPaintContext getContext() {
		return myContext;
	}

	public void paint(Graphics g) {
		myContext.begin(g);
		myWidget.getView().paint();
		myContext.end();
	}

	public void keyPressed(int keyCode) {
		if (myApplication == null) {
			return;
		}

		String keyName = null;
		switch (keyCode) {
			case -1:
				keyName = "<Up>";
				break;
			case -2:
				keyName = "<Down>";
				break;
			case -3:
				keyName = "<Left>";
				break;
			case -4:
				keyName = "<Right>";
				break;
			case -5:
				keyName = "<Center>";
				break;
			default:
				if (keyCode > 0) {
					keyName = "<" + (char)keyCode + ">";
				}
		}
		if (keyName != null) {
			myApplication.doActionByKey(keyName);
		}
	}
}
