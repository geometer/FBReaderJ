/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.android.util.DeviceType;

public final class ZLAndroidLibrary extends ZLibrary {
	public final ZLBooleanOption ShowStatusBarOption = new ZLBooleanOption("LookNFeel", "ShowStatusBar", false);
	public final ZLBooleanOption OldShowActionBarOption = new ZLBooleanOption("LookNFeel", "ShowActionBar", true);
	public final ZLBooleanOption ShowActionBarOption = new ZLBooleanOption("LookNFeel", "ShowActionBarNew", false);
	public final ZLBooleanOption EnableFullscreenModeOption = new ZLBooleanOption("LookNFeel", "FullscreenMode", true);
	public final ZLBooleanOption DisableButtonLightsOption = new ZLBooleanOption("LookNFeel", "DisableButtonLights", !DeviceType.Instance().hasButtonLightsBug());
	{
		ShowStatusBarOption.setSpecialName("statusBar");
		OldShowActionBarOption.setSpecialName("actionBar");
		ShowActionBarOption.setSpecialName("actionBarNew");
		EnableFullscreenModeOption.setSpecialName("enableFullscreen");
		DisableButtonLightsOption.setSpecialName("disableButtonLights");
	}
	public final ZLIntegerRangeOption BatteryLevelToTurnScreenOffOption = new ZLIntegerRangeOption("LookNFeel", "BatteryLevelToTurnScreenOff", 0, 100, 50);
	public final ZLBooleanOption DontTurnScreenOffDuringChargingOption = new ZLBooleanOption("LookNFeel", "DontTurnScreenOffDuringCharging", true);
	public final ZLIntegerRangeOption ScreenBrightnessLevelOption = new ZLIntegerRangeOption("LookNFeel", "ScreenBrightnessLevel", 0, 100, 0);

	private final Application myApplication;

	ZLAndroidLibrary(Application application) {
		myApplication = application;
	}

	public AssetManager getAssets() {
		return myApplication.getAssets();
	}

	@Override
	public ZLResourceFile createResourceFile(String path) {
		return new AndroidAssetsFile(path);
	}

	@Override
	public ZLResourceFile createResourceFile(ZLResourceFile parent, String name) {
		return new AndroidAssetsFile((AndroidAssetsFile)parent, name);
	}

	@Override
	public String getVersionName() {
		try {
			final PackageInfo info =
				myApplication.getPackageManager().getPackageInfo(myApplication.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public String getFullVersionName() {
		try {
			final PackageInfo info =
				myApplication.getPackageManager().getPackageInfo(myApplication.getPackageName(), 0);
			return info.versionName + " (" + info.versionCode + ")";
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public String getCurrentTimeString() {
		return DateFormat.getTimeFormat(myApplication.getApplicationContext()).format(new Date());
	}

	private DisplayMetrics myMetrics;
	private DisplayMetrics getMetrics() {
		if (myMetrics == null) {
			myMetrics = myApplication.getApplicationContext().getResources().getDisplayMetrics();
		}
		return myMetrics;
	}

	@Override
	public int getDisplayDPI() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : (int)(160 * metrics.density);
	}

	@Override
	public int getWidthInPixels() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : metrics.widthPixels;
	}

	@Override
	public int getHeightInPixels() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : metrics.heightPixels;
	}

	@Override
	public List<String> defaultLanguageCodes() {
		final TreeSet<String> set = new TreeSet<String>();
		set.add(Locale.getDefault().getLanguage());
		final TelephonyManager manager = (TelephonyManager)myApplication.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager != null) {
			String country0 = manager.getSimCountryIso();
			if (country0 != null) {
				country0 = country0.toLowerCase();
			}
			String country1 = manager.getNetworkCountryIso();
			if (country1 != null) {
				country1 = country1.toLowerCase();
			}
			for (Locale locale : Locale.getAvailableLocales()) {
				final String country = locale.getCountry().toLowerCase();
				if (country != null && country.length() > 0 &&
					(country.equals(country0) || country.equals(country1))) {
					set.add(locale.getLanguage());
				}
			}
			if ("ru".equals(country0) || "ru".equals(country1)) {
				set.add("ru");
			} else if ("by".equals(country0) || "by".equals(country1)) {
				set.add("ru");
			} else if ("ua".equals(country0) || "ua".equals(country1)) {
				set.add("ru");
			}
		}
		set.add("multi");
		return new ArrayList<String>(set);
	}

	@Override
	public boolean supportsAllOrientations() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	private static interface StreamStatus {
		int UNKNOWN = -1;
		int NULL = 0;
		int OK = 1;
		int EXCEPTION = 2;
	}

	private final class AndroidAssetsFile extends ZLResourceFile {
		private final AndroidAssetsFile myParent;

		AndroidAssetsFile(AndroidAssetsFile parent, String name) {
			super(parent.getPath().length() == 0 ? name : parent.getPath() + '/' + name);
			myParent = parent;
		}

		AndroidAssetsFile(String path) {
			super(path);
			if (path.length() == 0) {
				myParent = null;
			} else {
				final int index = path.lastIndexOf('/');
				myParent = new AndroidAssetsFile(index >= 0 ? path.substring(0, path.lastIndexOf('/')) : "");
			}
		}

		@Override
		protected List<ZLFile> directoryEntries() {
			try {
				String[] names = myApplication.getAssets().list(getPath());
				if (names != null && names.length != 0) {
					ArrayList<ZLFile> files = new ArrayList<ZLFile>(names.length);
					for (String n : names) {
						files.add(new AndroidAssetsFile(this, n));
					}
					return files;
				}
			} catch (IOException e) {
			}
			return Collections.emptyList();
		}

		private int myStreamStatus = StreamStatus.UNKNOWN;
		private int streamStatus() {
			if (myStreamStatus == StreamStatus.UNKNOWN) {
				try {
					final InputStream stream = myApplication.getAssets().open(getPath());
					if (stream == null) {
						myStreamStatus = StreamStatus.NULL;
					} else {
						stream.close();
						myStreamStatus = StreamStatus.OK;
					}
				} catch (IOException e) {
					myStreamStatus = StreamStatus.EXCEPTION;
				}
			}
			return myStreamStatus;
		}

		@Override
		public boolean isDirectory() {
			return streamStatus() != StreamStatus.OK;
		}

		@Override
		public boolean exists() {
			if (streamStatus() == StreamStatus.OK) {
				return true;
			}
			final String path = getPath();
			if ("".equals(path)) {
				return true;
			}
			// a hack: we store help files in fb2 format
			if (path.endsWith(".fb2")) {
				return false;
			}
			try {
				String[] names = myApplication.getAssets().list(getPath());
				if (names != null && names.length != 0) {
					// directory exists
					return true;
				}
			} catch (IOException e) {
			}
			return false;
		}

		private long mySize = -1;
		@Override
		public long size() {
			if (mySize == -1) {
				mySize = sizeInternal();
			}
			return mySize;
		}

		private long sizeInternal() {
			try {
				AssetFileDescriptor descriptor = myApplication.getAssets().openFd(getPath());
				// for some files (archives, crt) descriptor cannot be opened
				if (descriptor == null) {
					return sizeSlow();
				}
				long length = descriptor.getLength();
				descriptor.close();
				return length;
			} catch (IOException e) {
				return sizeSlow();
			}
		}

		private long sizeSlow() {
			try {
				final InputStream stream = getInputStream();
				if (stream == null) {
					return 0;
				}
				long size = 0;
				final long step = 1024 * 1024;
				while (true) {
					// TODO: does skip work as expected for these files?
					long offset = stream.skip(step);
					size += offset;
					if (offset < step) {
						break;
					}
				}
				return size;
			} catch (IOException e) {
				return 0;
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return myApplication.getAssets().open(getPath());
		}

		@Override
		public ZLFile getParent() {
			return myParent;
		}
	}
}
