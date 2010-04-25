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

import java.io.*;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.AssetFileDescriptor;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.android.fbreader.network.BookDownloader;
import org.geometerplus.android.fbreader.network.BookDownloaderService;

public final class ZLAndroidLibrary extends ZLibrary {
	private ZLAndroidActivity myActivity;
	private final Application myApplication;
	private ZLAndroidWidget myWidget;

	ZLAndroidLibrary(Application application) {
		myApplication = application;
	}

	void setActivity(ZLAndroidActivity activity) {
		myActivity = activity;
		((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).setActivity(activity);
		myWidget = null;
	}

	public void rotateScreen() {
		if (myActivity != null)	{
			myActivity.rotate();
		}
	}

	public void finish() {
		if ((myActivity != null) && !myActivity.isFinishing()) {
			myActivity.finish();
		}
	}

	public ZLAndroidPaintContext getPaintContext() {
		return getWidget().getPaintContext();
	}

	public ZLAndroidWidget getWidget() {
		if (myWidget == null) {
			myWidget = (ZLAndroidWidget)myActivity.findViewById(R.id.main_view);
		}
		return myWidget;
	}

	public void openInBrowser(String reference) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		boolean externalUrl = true;
		if (BookDownloader.acceptsUri(Uri.parse(reference))) {
			intent.setClass(myActivity, BookDownloader.class);
			intent.putExtra(BookDownloaderService.SHOW_NOTIFICATIONS_KEY, BookDownloaderService.Notifications.ALL);
			externalUrl = false;
		}
		reference = NetworkLibrary.Instance().rewriteUrl(reference, externalUrl);
		intent.setData(Uri.parse(reference));
		myActivity.startActivity(intent);
	}

	@Override
	public ZLResourceFile createResourceFile(String path) {
		return new AndroidResourceFile(path);
	}

	@Override
	public String getVersionName() {
		try {
			return myApplication.getPackageManager().getPackageInfo(myApplication.getPackageName(), 0).versionName;
		} catch (Exception e) {
			return "";
		}
	}

	private final class AndroidResourceFile extends ZLResourceFile {
		private boolean myExists;
		private int myResourceId;

		AndroidResourceFile(String path) {
			super(path);
			final String fieldName =
				path.replace("/", "__").replace(".", "_").replace("-", "_").toLowerCase();
			try {
				myResourceId = R.raw.class.getField(fieldName).getInt(null);
				myExists = true;
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {
			}
		}

		@Override
		public boolean exists() {
			return myExists;
		}

		@Override
		public long size() {
			try {
				AssetFileDescriptor descriptor =
					myApplication.getResources().openRawResourceFd(myResourceId);
				long length = descriptor.getLength();
				descriptor.close();
				return length;
			} catch (IOException e) {
				return 0;
			} catch (Resources.NotFoundException e) {
				return 0;
			} 
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!myExists) {
				throw new IOException("File not found: " + getPath());
			}
			try {
				return myApplication.getResources().openRawResource(myResourceId);
			} catch (Resources.NotFoundException e) {
				throw new IOException(e.getMessage());
			}
		}
	}
}
