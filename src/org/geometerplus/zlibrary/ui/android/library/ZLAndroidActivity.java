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

import java.io.File;

import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public abstract class ZLAndroidActivity extends Activity {
	protected abstract ZLApplication createApplication(String fileName);

	private static final String REQUESTED_ORIENTATION_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.RequestedOrientation";
	private static final String ORIENTATION_CHANGE_COUNTER_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.ChangeCounter";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(REQUESTED_ORIENTATION_KEY, myOrientation);
		state.putInt(ORIENTATION_CHANGE_COUNTER_KEY, myChangeCounter);
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		if (state != null) {
			myOrientation = state.getInt(REQUESTED_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			myChangeCounter = state.getInt(ORIENTATION_CHANGE_COUNTER_KEY);
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		getLibrary().setActivity(this);

		final Intent intent = getIntent();
		String fileToOpen = null;
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri uri = intent.getData();
			if (uri != null) {
				fileToOpen = uri.getPath();
			}
			intent.setData(null);
		}

		if (((ZLAndroidApplication)getApplication()).myMainWindow == null) {
			ZLApplication application = createApplication(fileToOpen);
			((ZLAndroidApplication)getApplication()).myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} else if (fileToOpen != null) {
			ZLApplication.Instance().openFile(new ZLPhysicalFile(new File(fileToOpen)));
		}
		ZLApplication.Instance().repaintView();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (ZLAndroidApplication.Instance().AutoOrientationOption.getValue()) {
			setAutoRotationMode();
		} else {
			switch (myOrientation) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
					if (getRequestedOrientation() != myOrientation) {
						setRequestedOrientation(myOrientation);
						myChangeCounter = 0;
					}
					break;
				default:
					setAutoRotationMode();
					break;
			}
		}
	}

	@Override
	public void onPause() {
		ZLApplication.Instance().onWindowClosing();
		super.onPause();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		String fileToOpen = null;
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Uri uri = intent.getData();
			if (uri != null) {
				fileToOpen = uri.getPath();
			}
			intent.setData(null);
		}

		if (fileToOpen != null) {
			ZLApplication.Instance().openFile(new ZLPhysicalFile(new File(fileToOpen)));
		}
		ZLApplication.Instance().repaintView();
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		((ZLAndroidApplication)getApplication()).myMainWindow.buildMenu(menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	private int myChangeCounter;
	private int myOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private void setAutoRotationMode() {
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myOrientation = application.AutoOrientationOption.getValue() ?
			ActivityInfo.SCREEN_ORIENTATION_SENSOR : ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
		setRequestedOrientation(myOrientation);
		myChangeCounter = 0;
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		switch (getRequestedOrientation()) {
			default:
				break;
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				if (config.orientation != Configuration.ORIENTATION_PORTRAIT) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
				if (config.orientation != Configuration.ORIENTATION_LANDSCAPE) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
		}
	}

	void rotate() {
		View view = findViewById(R.id.main_view);
		if (view != null) {
			switch (getRequestedOrientation()) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				default:
					if (view.getWidth() > view.getHeight()) {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					} else {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					}
			}
			setRequestedOrientation(myOrientation);
			myChangeCounter = 0;
		}
	}
}
