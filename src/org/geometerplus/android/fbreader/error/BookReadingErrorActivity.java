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

package org.geometerplus.android.fbreader.error;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.error.ErrorUtil;
import org.geometerplus.android.fbreader.util.SimpleDialogActivity;

public class BookReadingErrorActivity extends SimpleDialogActivity implements ErrorKeys {
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		final ZLResource resource = ZLResource.resource("error").getResource("bookReading");
		setTitle(resource.getResource("title").getValue());

		textView().setText(getIntent().getStringExtra(MESSAGE));

		okButton().setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "issues@fbreader.org" });
				sendIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(STACKTRACE));
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "FBReader " + new ErrorUtil(BookReadingErrorActivity.this).getVersionName() + " book reading issue report");
				sendIntent.setType("message/rfc822");
				startActivity(sendIntent);
				finish();
			}
		});
		cancelButton().setOnClickListener(finishListener());
		setButtonTexts("sendReport", "cancel");
	}
}
