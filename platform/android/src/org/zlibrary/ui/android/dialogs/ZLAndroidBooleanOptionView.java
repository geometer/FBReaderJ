package org.zlibrary.ui.android.dialogs;

import android.view.View;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;

class ZLAndroidBooleanOptionView extends ZLAndroidOptionView {
	CheckBox myCheckBox;

	protected ZLAndroidBooleanOptionView(ZLAndroidDialogContent tab, String name, ZLBooleanOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		CheckBox checkBox = new CheckBox(myTab.getView().getContext());
		checkBox.setText(myName);	
		checkBox.setChecked(((ZLBooleanOptionEntry)myOption).initialState());
		myCheckBox = checkBox;
		addAndroidView(checkBox, true);
	}

	protected void reset() {
		if (myCheckBox != null) {
			myCheckBox.setChecked(((ZLBooleanOptionEntry)myOption).initialState());
		}
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry)myOption).onAccept(myCheckBox.isChecked());
	}
}
