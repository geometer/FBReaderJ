package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLStringOptionEntry;

class ZLAndroidStringOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private EditText myEditor;
	protected ZLAndroidStringOptionView(ZLAndroidDialogContent tab, String name, ZLStringOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		final Context context = myTab.getView().getContext();
		if (myName != null) {
			myLabel = new TextView(context);
			myLabel.setText(myName);
			myLabel.setPadding(0, 12, 0, 12);
			myLabel.setTextSize(18);
		}
		final ZLStringOptionEntry stringEntry = (ZLStringOptionEntry)myOption;
		myEditor = new EditText(context) {
			protected boolean getDefaultEditable() {
				return stringEntry.isActive();
			}
		};
		myEditor.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		myEditor.setText(stringEntry.initialValue());
	}

	void addAndroidViews() {
		myTab.addAndroidView(myLabel, false);
		myTab.addAndroidView(myEditor, true);
	}

	protected void reset() {
		if (myEditor != null) {
			final ZLStringOptionEntry stringEntry = (ZLStringOptionEntry)myOption;
			myEditor.setText(stringEntry.initialValue());	
		}
	}

	protected void _onAccept() {
		((ZLStringOptionEntry)myOption).onAccept(myEditor.getText().toString());
	}
}
