/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.app.Dialog;
import android.os.Handler;
import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

class ZLAndroidDialog extends ZLDialog {
	private final AndroidDialog myDialog;
	private final LinearLayout myFooter;	

	ZLAndroidDialog(Context context, ZLResource resource) {
		myFooter = new LinearLayout(context);
		myFooter.setOrientation(LinearLayout.HORIZONTAL);
		myFooter.setHorizontalGravity(0x01);
		ZLAndroidDialogContentWithFooter tab =
			new ZLAndroidDialogContentWithFooter(context, resource, myFooter);
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
