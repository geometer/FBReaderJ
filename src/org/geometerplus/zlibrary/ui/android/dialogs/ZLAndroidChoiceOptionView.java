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
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLChoiceOptionEntry;

class ZLAndroidChoiceOptionView extends ZLAndroidOptionView {
	private RadioGroup myGroup;
	private RadioButton myButtons[];

	protected ZLAndroidChoiceOptionView(ZLAndroidDialogContent tab, String name, ZLChoiceOptionEntry option) {
		super(tab, name, option);
	}

	void addAndroidViews() {
		if (myGroup == null) {
			final Context context = myTab.getContext();
			myGroup = new RadioGroup(context);
			myGroup.setOrientation(RadioGroup.VERTICAL);
    
			final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
			final int choiceNumber = choiceEntry.choiceNumber();
			myButtons = new RadioButton[choiceNumber];
			for (int i = 0; i < choiceNumber; ++i) {
				final RadioButton button = new RadioButton(context);
				myButtons[i] = button;
				button.setId(i + 1);
				button.setText(choiceEntry.getText(i));
				myGroup.addView(button, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
			}
			myGroup.check(choiceEntry.initialCheckedIndex() + 1);
		}
		myTab.addAndroidView(myGroup, true);
	}

	protected void reset() {
		if (myGroup != null) {
			final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
			myGroup.check(choiceEntry.initialCheckedIndex() + 1);
		}
	}

	protected void _onAccept() {
		if (myGroup != null) {
			((ZLChoiceOptionEntry)myOption).onAccept(myGroup.getCheckedRadioButtonId() - 1);
			myGroup = null;
			myButtons = null;
		}
	}

	protected void _setActive(boolean active) {
		if (myGroup != null) {
			myGroup.setEnabled(active);
			final RadioButton[] buttons = myButtons;
			for (int i = buttons.length - 1; i >= 0; --i) {
				buttons[i].setEnabled(active);
			}
		}
	}
}
