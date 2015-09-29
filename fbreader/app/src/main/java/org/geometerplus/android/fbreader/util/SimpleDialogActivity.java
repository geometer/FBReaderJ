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

package org.geometerplus.android.fbreader.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public abstract class SimpleDialogActivity extends Activity {
	private TextView myTextView;
	private View myButtonsView;
	private Button myOkButton;
	private Button myCancelButton;
	private ZLResource myButtonsResource;
	private View.OnClickListener myFinishListener;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		myTextView = null;
		myButtonsView = null;
		myOkButton = null;
		myCancelButton = null;
		setContentView(R.layout.simple_dialog);
	}

	protected final TextView textView() {
		if (myTextView == null) {
			myTextView = (TextView)findViewById(R.id.simple_dialog_text);
		}
		return myTextView;
	}

	protected final View buttonsView() {
		if (myButtonsView == null) {
			myButtonsView = findViewById(R.id.simple_dialog_buttons);
		}
		return myButtonsView;
	}

	protected final Button okButton() {
		if (myOkButton == null) {
			myOkButton = (Button)buttonsView().findViewById(R.id.ok_button);
		}
		return myOkButton;
	}

	protected final Button cancelButton() {
		if (myCancelButton == null) {
			myCancelButton = (Button)buttonsView().findViewById(R.id.cancel_button);
		}
		return myCancelButton;
	}

	private final ZLResource buttonsResource() {
		if (myButtonsResource == null) {
			myButtonsResource = ZLResource.resource("dialog").getResource("button");
		}
		return myButtonsResource;
	}

	protected final void setButtonTexts(String okKey, String cancelKey) {
		if (okKey != null) {
			okButton().setText(buttonsResource().getResource(okKey).getValue());
			okButton().setVisibility(View.VISIBLE);
		} else {
			okButton().setVisibility(View.GONE);
		}
		if (cancelKey != null) {
			cancelButton().setText(buttonsResource().getResource(cancelKey).getValue());
			cancelButton().setVisibility(View.VISIBLE);
		} else {
			cancelButton().setVisibility(View.GONE);
		}
	}

	protected final View.OnClickListener finishListener() {
		if (myFinishListener == null) {
			myFinishListener = new View.OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			};
		}
		return myFinishListener;
	}
}
