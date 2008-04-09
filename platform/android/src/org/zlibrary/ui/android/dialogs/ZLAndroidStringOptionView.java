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
			final TextView label = new TextView(context);
			label.setText(myName);
			label.setPadding(0, 12, 0, 12);
			label.setTextSize(18);
			myLabel = label;
			addAndroidView(label, false);
		}
		final ZLStringOptionEntry stringEntry = (ZLStringOptionEntry)myOption;
		myEditor = new EditText(context) {
			protected boolean getDefaultEditable() {
				return stringEntry.isActive();
			}
		};
		myEditor.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		myEditor.setText(stringEntry.initialValue());
		addAndroidView(myEditor, true);
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
