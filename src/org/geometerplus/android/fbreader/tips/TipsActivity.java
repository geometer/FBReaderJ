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

package org.geometerplus.android.fbreader.tips;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.tips.*;

public class TipsActivity extends Activity {
	private final TipsManager myManager = TipsManager.Instance();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.tip);
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource resource = dialogResource.getResource("tips");
		final CheckBox checkBox = (CheckBox)findViewById(R.id.tip_checkbox);
		checkBox.setText(resource.getResource("dontShowAgain").getValue());

		final Button okButton =
			(Button)findViewById(R.id.tip_buttons).findViewById(R.id.ok_button);
		okButton.setText(dialogResource.getResource("button").getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myManager.ShowTipsOption.setValue(!checkBox.isChecked());
				finish();
			}
		});

		final Button nextTipButton =
			(Button)findViewById(R.id.tip_buttons).findViewById(R.id.cancel_button);
		nextTipButton.setText(resource.getResource("more").getValue());
		nextTipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTip(nextTipButton);
			}
		});

		showTip(nextTipButton);
	}

	private void showTip(Button nextTipButton) {
		final Tip tip = myManager.getNextTip();
		if (tip != null) {
			setTitle(tip.Title);
			final TextView textView = ((TextView)findViewById(R.id.tip_text));
			textView.setText(tip.Content);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		nextTipButton.setEnabled(myManager.hasNextTip());
	}
}
