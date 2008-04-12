/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
	RadioGroup myGroup;

	protected ZLAndroidChoiceOptionView(ZLAndroidDialogContent tab, String name, ZLChoiceOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		final Context context = myTab.getView().getContext();
		myGroup = new RadioGroup(context);
		myGroup.setOrientation(RadioGroup.VERTICAL);

		final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
		final int choiceNumber = choiceEntry.choiceNumber();
		for (int i = 0; i < choiceNumber; i++) {
			final RadioButton button = new RadioButton(context);
			button.setId(i + 1);
			button.setText(choiceEntry.getText(i));
			myGroup.addView(button, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
		}
		myGroup.check(choiceEntry.initialCheckedIndex() + 1);
	}

	void addAndroidViews() {
		myTab.addAndroidView(myGroup, true);
	}

	protected void reset() {
		final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
		myGroup.check(choiceEntry.initialCheckedIndex() + 1);
	}

	protected void _onAccept() {
		((ZLChoiceOptionEntry)myOption).onAccept(myGroup.getCheckedRadioButtonId() - 1);
	}
}
