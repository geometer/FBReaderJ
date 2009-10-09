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

package org.geometerplus.zlibrary.ui.j2me.application;

import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.j2me.view.*;

public class ZLJ2MEApplicationWindow extends ZLApplicationWindow {
	private final ZLCanvas myCanvas;

	public ZLJ2MEApplicationWindow(ZLApplication application, ZLCanvas canvas) {
		super(application);
		myCanvas = canvas;
	}

	protected void initMenu() {
		// TODO: implement
	}

	public void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled) {
		// TODO: implement
	}
	
	protected ZLViewWidget createViewWidget() {
		return new ZLJ2MEViewWidget(myCanvas);
	}
	
	public void addToolbarItem(ZLApplication.Toolbar.Item item) {
		// TODO: implement
	}

	public void close() {
		// TODO: implement
	}

	public void setCaption(String caption) {
		// TODO: implement
	}

	public void setFullscreen(boolean fullscreen) {
		// TODO: implement
	}

	public boolean isFullscreen() {
		// TODO: implement
		return false;
	}
}
