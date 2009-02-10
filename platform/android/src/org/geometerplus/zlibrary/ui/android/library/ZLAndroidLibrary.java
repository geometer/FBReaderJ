/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import java.io.*;

import android.content.res.Resources;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.geometerplus.zlibrary.core.sqliteconfig.ZLSQLiteConfig;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

public final class ZLAndroidLibrary extends ZLibrary {
	private ZLAndroidActivity myActivity;
	private ZLAndroidApplicationWindow myMainWindow;
	private ZLAndroidWidget myWidget;
	private final ZLAndroidDialogManager myDialogManager;

	ZLAndroidLibrary(ZLAndroidActivity activity) {
		myActivity = activity;

		new ZLOwnXMLProcessorFactory();
		loadProperties();
		new ZLSQLiteConfig(activity.getApplication(), getApplicationName());
		new ZLAndroidImageManager();
		myDialogManager = new ZLAndroidDialogManager(activity);

		try {
			ZLApplication application = (ZLApplication)getApplicationClass().newInstance();
			myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} catch (Exception e) {
			finish();
		}
	}

	void setActivity(ZLAndroidActivity activity) {
		myActivity = activity;
		myDialogManager.setActivity(activity);
		myWidget = null;
	}

	public void finish() {
		if ((myActivity != null) && !myActivity.isFinishing()) {
			myActivity.finish();
		}
	}

	public ZLAndroidPaintContext getPaintContext() {
		return getWidget().getPaintContext();
	}

	ZLAndroidApplicationWindow getMainWindow() {
		return myMainWindow;
	}

	public ZLAndroidWidget getWidget() {
		if (myWidget == null) {
			myWidget = (ZLAndroidWidget)myActivity.findViewById(R.id.zlandroidactivity);
		}
		return myWidget;
	}

	protected InputStream getFileInputStream(String fileName) {
		try {
			//return new BufferedInputStream(new FileInputStream(fileName), 16384);
			return new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	protected InputStream getResourceInputStream(String fileName) {
		final String fieldName = fileName.replace("/", "__").replace(".", "_").replace("-", "_").toLowerCase();
		int resourceId;
		try {
			resourceId = R.raw.class.getField(fieldName).getInt(null);
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		return myActivity.getResources().openRawResource(resourceId);
	}

	public void openInBrowser(String reference) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(reference));
		myActivity.startActivity(intent);
	}
}
