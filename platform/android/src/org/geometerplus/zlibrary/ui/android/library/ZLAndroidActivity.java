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

package org.geometerplus.zlibrary.ui.android.library;

import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.config.ZLConfig;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public abstract class ZLAndroidActivity extends Activity {
	protected abstract ZLApplication createApplication(String fileName);

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		getLibrary().setActivity(this);

		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		final String fileToOpen = (uri != null) ? uri.getPath() : null;
		intent.setData(null);

		if (((ZLAndroidApplication)getApplication()).myMainWindow == null) {
			ZLApplication application = createApplication(fileToOpen);
			((ZLAndroidApplication)getApplication()).myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} else if (fileToOpen != null) {
			ZLApplication.Instance().openFile(fileToOpen);
		}
		ZLApplication.Instance().refreshWindow();
	}

	public void onPause() {
		ZLApplication.Instance().onWindowClosing();
		ZLConfig.Instance().shutdown();
		super.onPause();
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		((ZLAndroidApplication)getApplication()).myMainWindow.buildMenu(menu);
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.zlandroidactivity);
		return (view != null) ? view.onKeyDown(keyCode, event) : true;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.zlandroidactivity);
		return (view != null) ? view.onKeyUp(keyCode, event) : true;
	}
}
