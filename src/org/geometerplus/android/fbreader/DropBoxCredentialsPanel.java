/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class DropBoxCredentialsPanel extends ControlButtonPanel {

	FBReaderApp myfbreader;

	DropBoxCredentialsPanel(FBReaderApp fbReader) {
		super(fbReader);
		myfbreader = fbReader;
	}

	public void runDropBoxCrendentials() {
		if (!getVisibility()) {
			show(true);
		}
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root) {
		final FBReader myactivity = activity;
		myControlPanel = new ControlPanel(activity, root, true);

		final View layout = activity.getLayoutInflater().inflate(R.layout.dropboxcredentials, myControlPanel, false);

		final EditText dropboxemail = (EditText)layout.findViewById(R.id.dropboxemail);
		final EditText dropboxpasswd = (EditText)layout.findViewById(R.id.dropboxpasswd);

		final Button btnOk = (Button)layout.findViewById(android.R.id.button1);
		final Button btnCancel = (Button)layout.findViewById(android.R.id.button3);

		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				if (v != btnCancel) {
					DropBoxSyncer dbsyncer = DropBoxSyncer.getDropBoxSyncer();
					dbsyncer.init(
						myfbreader.Model.Book.File,
						(ZLTextView)Reader.getCurrentView(),
						myactivity						
					);
					dbsyncer.setDropBoxCredentials(dropboxemail.getText().toString(), dropboxpasswd.getText().toString());
					dbsyncer.run();
				}
				hide(true);
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());

		myControlPanel.addView(layout);
	}

}
