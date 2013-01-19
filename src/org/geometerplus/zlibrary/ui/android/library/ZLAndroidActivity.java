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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filetypes.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.library.Book;

public abstract class ZLAndroidActivity extends Activity {
	protected static class ExtFileOpener implements ZLApplication.ExternalFileOpener {
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

	protected static class PluginFileOpener implements ZLApplication.PluginFileOpener {
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
	
	protected abstract ZLFile fileFromIntent(Intent intent);

	protected abstract Runnable getPostponedInitAction();

	protected boolean myIsPaused = false;
	protected AlertDialog myDialogToShow = null;

	protected static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}
}
