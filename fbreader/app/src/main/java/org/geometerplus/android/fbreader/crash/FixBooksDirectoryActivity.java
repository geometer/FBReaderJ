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

package org.geometerplus.android.fbreader.crash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.FileChooserUtil;

public class FixBooksDirectoryActivity extends Activity {
	private TextView myDirectoryView;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.books_directory_fix);

		final ZLResource resource = ZLResource.resource("crash").getResource("fixBooksDirectory");
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		final String title = resource.getResource("title").getValue();
		setTitle(title);

		final TextView textView = (TextView)findViewById(R.id.books_directory_fix_text);
		textView.setText(resource.getResource("text").getValue());

		myDirectoryView = (TextView)findViewById(R.id.books_directory_fix_directory);

		final View buttonsView = findViewById(R.id.books_directory_fix_buttons);
		final Button okButton = (Button)buttonsView.findViewById(R.id.ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());

		final View selectButton = findViewById(R.id.books_directory_fix_select_button);

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				final ZLStringOption tempDirectoryOption = Paths.TempDirectoryOption(FixBooksDirectoryActivity.this);
				myDirectoryView.setText(tempDirectoryOption.getValue());
				selectButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						FileChooserUtil.runDirectoryChooser(
							FixBooksDirectoryActivity.this,
							1,
							title,
							tempDirectoryOption.getValue(),
							true
						);
					}
				});
				okButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						final String newDirectory = myDirectoryView.getText().toString();
						tempDirectoryOption.setValue(newDirectory);
						startActivity(new Intent(FixBooksDirectoryActivity.this, FBReader.class));
						finish();
					}
				});
			}
		});

		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			myDirectoryView.setText(FileChooserUtil.folderPathFromData(data));
		}
	}
}
