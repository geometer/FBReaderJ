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

package org.geometerplus.android.fbreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class GotoPageNumberAction extends FBAndroidAction {
	GotoPageNumberAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final View root = BaseActivity.getLayoutInflater().inflate(R.layout.goto_page_number, null);
		final AlertDialog dialog = new AlertDialog.Builder(BaseActivity).setView(root).create();

		final ZLTextView textView = Reader.getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();

		final NumberPicker picker = (NumberPicker)root.findViewById(R.id.goto_page_number_picker);
		picker.setMinValue(1);
		picker.setMaxValue(pagePosition.Total);
		picker.setValue(pagePosition.Current);

		final View buttonsView = root.findViewById(R.id.goto_page_number_buttons);
		final Button okButton = (Button)buttonsView.findViewById(R.id.ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				picker.clearFocus();
				textView.gotoPage(picker.getValue());
				Reader.getViewWidget().reset();
				Reader.getViewWidget().repaint();
				BaseActivity.hideBars();
				dialog.dismiss();
			}
		});
		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				picker.setFocusable(true);
				picker.setFocusableInTouchMode(true);
				picker.requestFocus();
			}
		});
		dialog.show();
	}
}
