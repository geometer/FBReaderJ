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

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class GoToPageAction extends FBAndroidAction {
	GoToPageAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		LayoutInflater inflater = BaseActivity.getLayoutInflater();
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity);
		final View root = inflater.inflate(R.layout.goto_dialog, null);
		builder.setView(root);
		final Dialog d = builder.create();
		final NumberPicker np = (NumberPicker)root.findViewById(R.id.page_picker);
		np.setMinValue(0);
		np.setMaxValue(Reader.getTextView().pagePosition().Total);
		np.setValue(Reader.getTextView().pagePosition().Current);
		final View buttonsView = root.findViewById(R.id.goto_buttons);
		final Button okButton = (Button)buttonsView.findViewById(R.id.ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				np.clearFocus();
				Reader.getTextView().gotoPage(np.getValue());
				Reader.getViewWidget().reset();
				Reader.getViewWidget().repaint();
				d.dismiss();
			}
		});
		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		d.show();
	}
}
