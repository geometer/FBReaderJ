/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLStringOptionEntry;

class ZLAndroidStringOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private EditText myEditor;
	protected ZLAndroidStringOptionView(ZLAndroidDialogContent tab, String name, ZLStringOptionEntry option) {
		super(tab, name, option);
	}

	void addAndroidViews() {
		final Context context = myTab.getContext();
		if (myName != null) {
			if (myLabel == null) {
				myLabel = new TextView(context);
				myLabel.setText(myName);
				myLabel.setPadding(0, 12, 0, 12);
				myLabel.setTextSize(18);
			}
			myTab.addAndroidView(myLabel, false);
		}

		final ZLStringOptionEntry stringEntry = (ZLStringOptionEntry)myOption;
		if (myEditor == null) {
			myEditor = new EditText(context) {
				protected boolean getDefaultEditable() {
					return stringEntry.isActive();
				}
			};
		}
		myEditor.setText(stringEntry.initialValue());

		myTab.addAndroidView(myEditor, true);
	}

	protected void reset() {
		if (myEditor != null) {
			final ZLStringOptionEntry stringEntry = (ZLStringOptionEntry)myOption;
			myEditor.setText(stringEntry.initialValue());	
		}
	}

	protected void _onAccept() {
		if (myEditor != null) {
			((ZLStringOptionEntry)myOption).onAccept(myEditor.getText().toString());
			myLabel = null;
			myEditor = null;
		}
	}
}
