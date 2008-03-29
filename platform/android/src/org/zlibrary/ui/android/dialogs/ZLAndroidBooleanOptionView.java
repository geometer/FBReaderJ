package org.zlibrary.ui.android.dialogs;

import android.view.View;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;

class ZLAndroidBooleanOptionView extends ZLAndroidOptionView {
	protected ZLAndroidBooleanOptionView(ZLAndroidDialogContent tab, String name, ZLBooleanOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		CheckBox view = new CheckBox(myTab.getView().getContext());
		view.setText(myName);	
		view.setChecked(((ZLBooleanOptionEntry)myOption).initialState());
		myView = view;
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry)myOption).onAccept(((CheckBox)myView).isChecked());
	}
}
