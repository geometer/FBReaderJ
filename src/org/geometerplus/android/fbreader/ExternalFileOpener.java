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

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;

class ExternalFileOpener implements FBReaderApp.ExternalFileOpener {
	private final FBReader myReader;

	ExternalFileOpener(FBReader reader) {
		myReader = reader;
	}

	public void openFile(ExternalFormatPlugin plugin, Book book, Bookmark bookmark) {
	}

	private void showErrorDialog(final ExternalFormatPlugin plugin, final Book book) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final String title =
			dialogResource.getResource("missingPlugin").getResource("title").getValue()
				.replace("%s", plugin.supportedFileType());
		final AlertDialog.Builder builder = new AlertDialog.Builder(myReader)
			.setTitle(title)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PackageUtil.installFromMarket(myReader, plugin.packageName());
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					myReader.onPluginNotFound(book);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					myReader.onPluginNotFound(book);
				}
			});

		final Runnable showDialog = new Runnable() {
			public void run() {
				builder.create().show();
			}
		};
		if (!myReader.IsPaused) {
			myReader.runOnUiThread(showDialog);
		} else {
			myReader.OnResumeAction = showDialog;
		}
	}
}
