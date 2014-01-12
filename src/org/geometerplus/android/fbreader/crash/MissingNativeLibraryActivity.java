/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.crash;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

public class MissingNativeLibraryActivity extends Activity {
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.missing_native_library);

		final ZLResource resource = ZLResource.resource("crash").getResource("missingNativeLibrary");
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		setTitle(resource.getResource("title").getValue());

		final TextView textView = (TextView)findViewById(R.id.native_library_missing_text);
		textView.setText(resource.getResource("text").getValue());

		final View buttonsView = findViewById(R.id.native_library_missing_buttons);
		final Button okButton = (Button)buttonsView.findViewById(R.id.ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setVisibility(View.GONE);
	}
}
