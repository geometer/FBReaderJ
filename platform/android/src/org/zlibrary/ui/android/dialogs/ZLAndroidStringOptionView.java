package org.zlibrary.ui.android.dialogs;

import android.view.View;
import android.content.Context;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLStringOptionEntry;

class ZLAndroidStringOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
	private TextView myEditor;
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
		EditText editor = new EditText(context);
		editor.setText(((ZLStringOptionEntry)myOption).initialValue());	
		myEditor = editor;
		addAndroidView(editor, true);
	}

	protected void reset() {
		if (myEditor != null) {
			myEditor.setText(((ZLStringOptionEntry)myOption).initialValue());	
		}
	}

	protected void _onAccept() {
		((ZLStringOptionEntry)myOption).onAccept(myEditor.getText().toString());
	}
}
