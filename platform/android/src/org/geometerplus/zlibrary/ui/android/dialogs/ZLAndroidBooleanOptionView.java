package org.geometerplus.zlibrary.ui.android.dialogs;

import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLBooleanOptionEntry;

class ZLAndroidBooleanOptionView extends ZLAndroidOptionView {
	CheckBox myCheckBox;

	protected ZLAndroidBooleanOptionView(ZLAndroidDialogContent tab, String name, ZLBooleanOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		final ZLBooleanOptionEntry booleanEntry = (ZLBooleanOptionEntry)myOption;
		CheckBox checkBox = new CheckBox(myTab.getView().getContext()) {
			public boolean onTouchEvent(MotionEvent event) {
				final boolean checked = isChecked();
				final boolean code = super.onTouchEvent(event);
				if (checked != isChecked()) {
					booleanEntry.onStateChanged(!checked);
				}
				return code;
			}
		};
		checkBox.setText(myName);	
		checkBox.setChecked(booleanEntry.initialState());
		myCheckBox = checkBox;
	}

	void addAndroidViews() {
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
		((ZLBooleanOptionEntry)myOption).onAccept(myCheckBox.isChecked());
	}
}
