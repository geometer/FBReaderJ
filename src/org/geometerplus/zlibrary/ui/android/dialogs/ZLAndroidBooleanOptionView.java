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

import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLBooleanOptionEntry;

class ZLAndroidBooleanOptionView extends ZLAndroidOptionView {
	CheckBox myCheckBox;

	protected ZLAndroidBooleanOptionView(ZLAndroidDialogContent tab, String name, ZLBooleanOptionEntry option) {
		super(tab, name, option);
	}

	void addAndroidViews() {
		if (myCheckBox == null) {
			final ZLBooleanOptionEntry booleanEntry = (ZLBooleanOptionEntry)myOption;
			myCheckBox = new CheckBox(myTab.getContext()) {
				public boolean onTouchEvent(MotionEvent event) {
					final boolean checked = isChecked();
					final boolean code = super.onTouchEvent(event);
					if (checked != isChecked()) {
						booleanEntry.onStateChanged(!checked);
					}
					return code;
				}
			};
			myCheckBox.setText(myName);	
			myCheckBox.setChecked(booleanEntry.initialState());
		}

		myTab.addAndroidView(myCheckBox, true);
	}

	protected void reset() {
		final ZLBooleanOptionEntry booleanEntry = (ZLBooleanOptionEntry)myOption;
		booleanEntry.onReset();
		if (myCheckBox != null) {
			myCheckBox.setChecked(booleanEntry.initialState());
		}
	}

	protected void _onAccept() {
		if (myCheckBox != null) {
			((ZLBooleanOptionEntry)myOption).onAccept(myCheckBox.isChecked());
		}
	}

	protected void _setActive(boolean active) {
		if (myCheckBox != null) {
			myCheckBox.setEnabled(active);
		}
	}
}
