package org.zlibrary.ui.android.dialogs;

import android.app.Dialog;
import android.os.Handler;
import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.resources.ZLResource;

import org.zlibrary.ui.android.library.ZLAndroidLibrary;

class ZLAndroidDialog extends ZLDialog {
	private final AndroidDialog myDialog;
	private final LinearLayout myFooter;	

	ZLAndroidDialog(Context context, ZLResource resource) {
		myFooter = new LinearLayout(context);
		myFooter.setOrientation(LinearLayout.HORIZONTAL);
		myFooter.setHorizontalGravity(0x01);
		ZLAndroidDialogContent tab =
			new ZLAndroidDialogContent(context, resource, null, myFooter);
		myTab = tab;
		myDialog = new AndroidDialog(context, tab.getView(), resource.getResource(ZLDialogManager.DIALOG_TITLE).getValue());
	}

	public void run() {
		myDialog.show();
	}

	public void addButton(String key, Runnable action) {
		final DialogButton button = new DialogButton(myDialog, ZLDialogManager.getButtonText(key).replace("&", ""), action);
		myFooter.addView(button, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	private static class DialogButton extends Button {
		private Dialog myDialog;
		private Runnable myAction;

		DialogButton(Dialog dialog, String text, Runnable action) {
			super(dialog.getContext());
			setText(text);
			myDialog = dialog;
			myAction = action;
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (myAction != null) {
				new Handler().post(myAction);
			}
			myDialog.dismiss();
			return true;
		}
	}
}
