package org.zlibrary.ui.android.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
/*
import android.content.DialogInterface;
import android.os.Handler;
*/

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.resources.ZLResource;
/*
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

import org.zlibrary.ui.android.library.*;
*/

class ZLAndroidDialog extends ZLDialog {
	private final AndroidDialog myDialog;

	ZLAndroidDialog(Context context, ZLResource resource) {
		myTab = new ZLAndroidDialogContent(context, resource);
		myDialog = new AndroidDialog(context, resource.getResource(ZLDialogManager.DIALOG_TITLE).getValue());
	}

	public void run() {
		myDialog.show();
	}

	public void addButton(String key, Runnable action) {
		// TODO: implement
	}

	private class AndroidDialog extends Dialog {
		private final String myCaption;

		AndroidDialog(Context context, String caption) {
			super(context);
			myCaption = caption;
		}

		protected void onStart() {
			setContentView(((ZLAndroidDialogContent)myTab).getView());
			setTitle(myCaption);
		}

		protected void onStop() {
		}
	}

}
