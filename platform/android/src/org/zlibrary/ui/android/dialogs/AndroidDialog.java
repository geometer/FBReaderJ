package org.zlibrary.ui.android.dialogs;

import android.app.Dialog;
import android.os.Handler;
import android.content.Context;
import android.view.*;

import org.zlibrary.ui.android.library.ZLAndroidLibrary;

final class AndroidDialog extends Dialog {
	private final View myView;
	private final String myCaption;
	private Runnable myCancelAction;
	private Runnable myExitAction;

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
		if (myExitAction != null) {
			new Handler().post(myExitAction);
		}
		((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getWidget().requestFocus();
	}

	public void setCancelAction(Runnable action) {
		myCancelAction = action;
	}

	public void setExitAction(Runnable action) {
		myExitAction = action;
	}

	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (myCancelAction != null)) {
			myCancelAction.run();
			return true;
		}
		return super.onKeyDown(keyCode, keyEvent);
	}
}
