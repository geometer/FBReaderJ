/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import java.lang.reflect.*;
import java.io.*;

import android.app.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.*;
import android.os.PowerManager;
import android.net.Uri;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.library.Book;

import org.geometerplus.zlibrary.core.filetypes.*;
import org.geometerplus.zlibrary.core.image.ZLImage;

public abstract class ZLAndroidActivity extends Activity {
	protected abstract ZLApplication createApplication();
	
	private static class ExtFileOpener implements ZLApplication.ExternalFileOpener {
		private final Activity myActivity;

		public ExtFileOpener(Activity activity) {
			myActivity = activity;
		}

		private void showErrorDialog(final String errName) {
			myActivity.runOnUiThread(new Runnable() {
				public void run() {
					final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
					final AlertDialog dialog = new AlertDialog.Builder(myActivity)
						.setTitle(title)
						.setIcon(0)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.create();
					if (((ZLAndroidActivity)myActivity).myIsPaused) {
						((ZLAndroidActivity)myActivity).myDialogToShow = dialog;
					} else {
						dialog.show();
					}
				}
			});
		}

		public boolean openFile(ZLFile f, String appData) {
			if (f == null) {
				showErrorDialog("unzipFailed");
				return false;
			}
			String extension = f.getExtension();
			Uri uri = Uri.parse("file://" + f.getPath());
			Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
			LaunchIntent.setPackage(appData);
			LaunchIntent.setData(uri);
			FileType ft = FileTypeCollection.Instance.typeForFile(f);
			for (MimeType type : ft.mimeTypes()) {
				LaunchIntent.setDataAndType(uri, type.Name);
				try {
					myActivity.startActivity(LaunchIntent);
					return true;
				} catch (ActivityNotFoundException e) {
				}
			}
			showErrorDialog("externalNotFound");
			return false;
		}
	}

	private static class PluginFileOpener implements ZLApplication.PluginFileOpener {
		private final Activity myActivity;

		public PluginFileOpener(Activity activity) {
			myActivity = activity;
		}

		private void showErrorDialog(final String errName) {
			final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
			final AlertDialog dialog = new AlertDialog.Builder(myActivity)
				.setTitle(title)
				.setIcon(0)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
			if (((ZLAndroidActivity)myActivity).myIsPaused) {
				((ZLAndroidActivity)myActivity).myDialogToShow = dialog;
			} else {
				dialog.show();
			}
		}
		
		private void showErrorDialog(final String errName, final String appData, final long bookId) {
			myActivity.runOnUiThread(new Runnable() {
				public void run() {
					final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
					final AlertDialog dialog = new AlertDialog.Builder(myActivity)
						.setTitle(title)
						.setIcon(0)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								   Intent i = new Intent(Intent.ACTION_VIEW);
								   i.setData(Uri.parse("market://search?q=" + appData));
								   myActivity.startActivity(i);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								((ZLAndroidActivity)myActivity).onPluginAbsent(bookId);
							}
						})
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								((ZLAndroidActivity)myActivity).onPluginAbsent(bookId);
							}
						})
						.create();
						if (((ZLAndroidActivity)myActivity).myIsPaused) {
							((ZLAndroidActivity)myActivity).myDialogToShow = dialog;
						} else {
								dialog.show();
						}
					}
			});
		}

		public void openFile(ZLFile f, String appData, String bookmark, long bookId) {
			if (f == null) {
				showErrorDialog("unzipFailed");
				return;
			}
			Uri uri = Uri.parse("file://" + f.getPath());
			Intent LaunchIntent = new Intent("android.fbreader.action.VIEW_PLUGIN");
			LaunchIntent.setPackage(appData);
			LaunchIntent.setData(uri);
			LaunchIntent.putExtra("BOOKMARK", bookmark);
			LaunchIntent.putExtra("BOOKID", bookId);
			LaunchIntent.putExtra("TITLE", Book.getById(bookId).getTitle() != null ? Book.getById(bookId).getTitle() : "");	
			FileType ft = FileTypeCollection.Instance.typeForFile(f);
			for (MimeType type : ft.mimeTypes()) {
				LaunchIntent.setDataAndType(uri, type.Name);
				try {
					myActivity.startActivity(LaunchIntent);
					return;
				} catch (ActivityNotFoundException e) {
				}
			}
			showErrorDialog("noPlugin", appData, bookId);
			return;
		}

		@Override
		public String readMetaInfo(ZLFile f, String appData) {
				return null;
		}

		@Override
		public ZLImage readImage(ZLFile f, String appData) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	protected abstract void onPluginAbsent(long bookId);
	
	private static final String REQUESTED_ORIENTATION_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.RequestedOrientation";
	private static final String ORIENTATION_CHANGE_COUNTER_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.ChangeCounter";

	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	final void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		getLibrary().ScreenBrightnessLevelOption().setValue(percent);
	}

	final int getScreenBrightness() {
		final int level = (int)(100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private void setButtonLight(boolean enabled) {
		try {
			final WindowManager.LayoutParams attrs = getWindow().getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, enabled ? -1.0f : 0.0f);
				getWindow().setAttributes(attrs);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}

	protected abstract ZLFile fileFromIntent(Intent intent);

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		getLibrary().setActivity(this);

		final ZLAndroidApplication androidApplication = (ZLAndroidApplication)getApplication();
		if (androidApplication.myMainWindow == null) {
			final ZLApplication application = createApplication();
			androidApplication.myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		}

			new Thread() {
				public void run() {
					getPostponedInitAction().run();
				}
			}.start();

		ZLApplication.Instance().getViewWidget().repaint();
//		if (!ZLApplication.Instance().externalFileOpenerIsSet()) {
			ZLApplication.Instance().setExternalFileOpener(new ExtFileOpener(this));
//		}
//		if (!ZLApplication.Instance().pluginFileOpenerIsSet()) {
			ZLApplication.Instance().setPluginFileOpener(new PluginFileOpener(this));
//		}
		
	}

	@Override
	protected void onDestroy() {
		Log.d("zlandroidactivity", "ondestroy");
		super.onDestroy();
	}

	protected abstract Runnable getPostponedInitAction();

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock =
						((PowerManager)getSystemService(POWER_SERVICE)).
							newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			ZLApplication.Instance().startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	protected boolean myIsPaused = false;
	protected AlertDialog myDialogToShow = null;
	
	@Override
	public void onResume() {
		super.onResume();
		switchWakeLock(
			getLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
			ZLApplication.Instance().getBatteryLevel()
		);
		myStartTimer = true;
		final int brightnessLevel =
			getLibrary().ScreenBrightnessLevelOption().getValue();
		if (brightnessLevel != 0) {
			setScreenBrightness(brightnessLevel);
		} else {
			setScreenBrightnessAuto();
		}
		if (getLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(false);
		}

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		myIsPaused = false;
		if (myDialogToShow != null) {
			myDialogToShow.show();
			myDialogToShow = null;
		}
	}

	@Override
	public void onPause() {
		myIsPaused = true;
		unregisterReceiver(myBatteryInfoReceiver);
		ZLApplication.Instance().stopTimer();
		switchWakeLock(false);
		if (getLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(true);
		}
		ZLApplication.Instance().onWindowClosing();
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		ZLApplication.Instance().onWindowClosing();
		super.onLowMemory();
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final View view = findViewById(R.id.main_view);
		return (view != null && view.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		final View view = findViewById(R.id.main_view);
		return (view != null && view.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
			application.myMainWindow.setBatteryLevel(level);
			switchWakeLock(
				getLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
			);
		}
	};
}
