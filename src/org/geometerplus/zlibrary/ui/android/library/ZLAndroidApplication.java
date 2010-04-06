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

package org.geometerplus.zlibrary.ui.android.library;

import java.util.HashMap;

import android.app.Application;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.sqliteconfig.ZLSQLiteConfig;

import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

public class ZLAndroidApplication extends Application {
	private static ZLAndroidApplication ourApplication;

	public final ZLBooleanOption AutoOrientationOption = new ZLBooleanOption("LookNFeel", "AutoOrientation", false);
	public final ZLBooleanOption ShowStatusBarOption = new ZLBooleanOption("LookNFeel", "ShowStatusBar", false);
	public final ZLBooleanOption DontTurnScreenOffOption = new ZLBooleanOption("LookNFeel", "DontTurnScreenOff", true);

	public static ZLAndroidApplication Instance() {
		return ourApplication;
	}

	public ZLAndroidApplication() {
		ourApplication = this;
	}

	ZLAndroidApplicationWindow myMainWindow;

	public void onCreate() {
		super.onCreate();
		new ZLSQLiteConfig(this);
		new ZLAndroidImageManager();
		new ZLAndroidDialogManager();
		new ZLAndroidLibrary(this);
	}

	public void onTerminate() {
		super.onTerminate();
	}

	public void putData(Object key, Object value) {
		myData.put(key, value);
	}

	public void removeData(Object key) {
		myData.remove(key);
	}

	public Object getData(Object key) {
		return myData.get(key);
	}

	private final HashMap myData = new HashMap();
}
