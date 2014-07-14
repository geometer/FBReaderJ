/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;

import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;

class PluginFileOpener implements FBReaderApp.PluginFileOpener {
	private final FBReader myReader;

	PluginFileOpener(FBReader reader) {
		myReader = reader;
	}

	private void showErrorDialog(final String errName, final ExternalFormatPlugin plugin) {
		myReader.runOnUiThread(new Runnable() {
			public void run() {
				final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
				final AlertDialog dialog = new AlertDialog.Builder(myReader)
					.setTitle(title)
					.setIcon(0)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse("market://search?q=" + plugin.packageName()));
							myReader.startActivity(i);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							myReader.onPluginNotFound();
						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							myReader.onPluginNotFound();
						}
					})
					.create();
				if (myReader.myIsPaused) {
					myReader.myDialogToShow = dialog;
				} else {
					dialog.show();
				}
			}
		});
	}

	public void openFile(final ExternalFormatPlugin plugin, Book book, Bookmark bookmark) {
		final Intent launchIntent = PluginUtil.createIntent(plugin, PluginUtil.ACTION_VIEW);
		FBReaderIntents.putBookExtra(launchIntent, book);
		FBReaderIntents.putBookmarkExtra(launchIntent, bookmark);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				try {
					String date = DateFormat.getDateTimeInstance().format(new Date());
					new ZLStringOption("Security", "PluginCalled", "").setValue(plugin.packageName() + date);
					launchIntent.putExtra("SECURITY_CODE", date);
					myReader.startActivity(launchIntent);
					myReader.overridePendingTransition(0, 0);
				} catch (ActivityNotFoundException e) {
					myReader.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showErrorDialog("noPlugin", plugin);
						}
					});
				}
			}
		});
	}
}
