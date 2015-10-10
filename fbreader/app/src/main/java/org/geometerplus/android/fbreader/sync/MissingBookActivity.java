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

package org.geometerplus.android.fbreader.sync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.util.SimpleDialogActivity;

public class MissingBookActivity extends SimpleDialogActivity {
	public static String errorTitle() {
		return ZLResource.resource("errorMessage").getResource("bookIsMissingTitle").getValue();
	}

	public static String errorMessage(String title) {
		return ZLResource.resource("errorMessage").getResource("bookIsMissing").getValue()
			.replace("%s", title);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		final Intent intent = getIntent();
		final String title = intent.getStringExtra(BookDownloaderService.Key.BOOK_TITLE);
		setTitle(errorTitle());
		textView().setText(errorMessage(title));
		intent.setClass(this, BookDownloaderService.class);

		okButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startService(intent);
				finish();
			}
		});
		setButtonTexts("download", null);
	}
}
