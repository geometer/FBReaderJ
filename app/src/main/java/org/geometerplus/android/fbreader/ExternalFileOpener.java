/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.math.BigInteger;
import java.util.Random;

import android.app.AlertDialog;
import android.content.*;

import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.ExternalFormatPlugin;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.util.PackageUtil;

class ExternalFileOpener implements FBReaderApp.ExternalFileOpener {
	private final String myPluginCode = new BigInteger(80, new Random()).toString();
	private final FBReader myReader;
	private volatile AlertDialog myDialog;

	ExternalFileOpener(FBReader reader) {
		myReader = reader;
	}

	public void openFile(final ExternalFormatPlugin plugin, final Book book, Bookmark bookmark) {
		if (myDialog != null) {
			myDialog.dismiss();
			myDialog = null;
		}

		final Intent intent = PluginUtil.createIntent(plugin, FBReaderIntents.Action.PLUGIN_VIEW);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		new ZLStringOption("PluginCode", plugin.packageName(), "").setValue(myPluginCode);
		intent.putExtra("PLUGIN_CODE", myPluginCode);

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				try {
					myReader.startActivity(intent);
					myReader.overridePendingTransition(0, 0);
				} catch (ActivityNotFoundException e) {
					showErrorDialog(plugin, book);
				}
			}
		});
	}

	private void showErrorDialog(final ExternalFormatPlugin plugin, final Book book) {
		final ZLResource rootResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = rootResource.getResource("button");
		final ZLResource dialogResource = rootResource.getResource("missingPlugin");
		final AlertDialog.Builder builder = new AlertDialog.Builder(myReader)
			.setTitle(dialogResource.getValue())
			.setMessage(dialogResource.getResource("message").getValue().replace("%s", plugin.supportedFileType()))
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PackageUtil.installFromMarket(myReader, plugin.packageName());
					myDialog = null;
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					myReader.onPluginNotFound(book);
					myDialog = null;
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					myReader.onPluginNotFound(book);
					myDialog = null;
				}
			});

		final Runnable showDialog = new Runnable() {
			public void run() {
				myDialog = builder.create();
				myDialog.show();
			}
		};
		if (!myReader.IsPaused) {
			myReader.runOnUiThread(showDialog);
		} else {
			myReader.OnResumeAction = showDialog;
		}
	}
}
