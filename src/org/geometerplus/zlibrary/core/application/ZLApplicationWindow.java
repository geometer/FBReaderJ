/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.application;

abstract public class ZLApplicationWindow {
	private ZLApplication myApplication;

	protected ZLApplicationWindow(ZLApplication application) {
		myApplication = application;
		myApplication.setWindow(this);
	}

	public ZLApplication getApplication() {
		return myApplication;
	}

	protected void init() {
		initMenu();
	}

	abstract protected void initMenu();
	abstract protected void refreshMenu();
	
	abstract protected void repaintView();
	abstract protected void scrollViewTo(int viewPage, int shift);
	abstract protected void startViewAutoScrolling(int viewPage);

	abstract protected void rotate();
	abstract protected boolean canRotate();

	abstract protected void close();

	abstract protected int getBatteryLevel();
}
