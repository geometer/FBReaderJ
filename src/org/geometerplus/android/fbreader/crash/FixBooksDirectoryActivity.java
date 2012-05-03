/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.FBReader;

public class FixBooksDirectoryActivity extends Activity {
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.books_directory_fix);

		final ZLResource resource = ZLResource.resource("crash").getResource("fixBooksDirectory");
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		setTitle(resource.getResource("title").getValue());

		final TextView textView = (TextView)findViewById(R.id.books_directory_fix_text);
		textView.setText(resource.getResource("text").getValue());

		final EditText directoryView = (EditText)findViewById(R.id.books_directory_fix_directory);
		directoryView.setText(Paths.mainBookDirectory());

		final View buttonsView = findViewById(R.id.books_directory_fix_buttons);
		final Button okButton = (Button)buttonsView.findViewById(R.id.ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Paths.BooksDirectoryOption().setValue(directoryView.getText().toString());
				startActivity(new Intent(FixBooksDirectoryActivity.this, FBReader.class));
				finish();
			}
		});

		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
