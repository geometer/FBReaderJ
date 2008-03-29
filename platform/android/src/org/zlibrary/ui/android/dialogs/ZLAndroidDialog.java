package org.zlibrary.ui.android.dialogs;

import android.app.Dialog;
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
		myTab = new ZLAndroidDialogContent(context, resource, myFooter);
		myDialog = new AndroidDialog(context, resource.getResource(ZLDialogManager.DIALOG_TITLE).getValue());
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
			myDialog.dismiss();
			if (myAction != null) {
				myAction.run();
			}
			return true;
		}
	}

	private class AndroidDialog extends Dialog {
		private final String myCaption;

		AndroidDialog(Context context, String caption) {
			super(context);
			myCaption = caption;
		}

		public void onStart() {
			final ZLAndroidDialogContent content = (ZLAndroidDialogContent)myTab;
			final ListView contentView = (ListView)content.getView();
			//contentView.addFooterView(myFooter, null, false);
			setContentView(contentView);
			setTitle(myCaption);
		}

		protected void onStop() {
			((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getWidget().requestFocus();
		}
	}
}
