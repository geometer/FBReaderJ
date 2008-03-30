package org.zlibrary.ui.android.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import org.zlibrary.ui.android.library.ZLAndroidLibrary;

class AndroidDialog extends Dialog {
	private final View myView;
	private final String myCaption;

	AndroidDialog(Context context, View view, String caption) {
		super(context);
		myView = view;
		myCaption = caption;
	}

	protected void onStart() {
		setContentView(myView);
		setTitle(myCaption);
	}

	protected void onStop() {
		((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getWidget().requestFocus();
	}
}
