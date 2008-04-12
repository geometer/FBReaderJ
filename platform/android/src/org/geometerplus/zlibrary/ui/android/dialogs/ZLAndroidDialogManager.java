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

import android.app.*;
import android.view.View;
import android.content.*;
import android.os.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

import org.geometerplus.zlibrary.ui.android.library.*;

public class ZLAndroidDialogManager extends ZLDialogManager {
	private final Activity myActivity;
	
	public ZLAndroidDialogManager(Activity activity) {
		myActivity = activity;
	}
	
	public void runSelectionDialog(String key, ZLTreeHandler handler, Runnable actionOnAccept) {
		new ZLAndroidSelectionDialog(myActivity, getDialogTitle(key), handler, actionOnAccept).run();
	}

	public void showErrorBox(String key, String message) {
		showAlert(0, key, message);
	}

	public void showInformationBox(String key, String message) {
		showAlert(0, key, message);
	}

	private void showAlert(int iconId, String key, String message) {
		AlertDialog.show(myActivity, null, iconId, message, getButtonText(OK_BUTTON), null, true, null);
	}

	private static class AlertListener implements DialogInterface.OnClickListener {
		private final Runnable myAction0, myAction1, myAction2;

		public AlertListener(Runnable action0, Runnable action1, Runnable action2) {
			myAction0 = action0;
			myAction1 = action1;
			myAction2 = action2;
		}

		public void onClick(DialogInterface dialog, int which) {
			Runnable action = null;
			switch (which) {
				case DialogInterface.BUTTON1:
					action = myAction0;
					break;
				case DialogInterface.BUTTON2:
					action = myAction1;
					break;
				case DialogInterface.BUTTON3:
					action = myAction2;
					break;
			}
			if (action != null) {
				new Handler().post(action);
			}
			dialog.dismiss();
		}
	}

	public void showQuestionBox(String key, String message, String button0, Runnable action0, String button1, Runnable action1, String button2, Runnable action2) {
		final AlertListener al = new AlertListener(action0, action1, action2);
		AlertDialog.show(myActivity, null, 0, message, button0, al, button1, al, button2, al, true, null);
	}

	public ZLAndroidApplicationWindow createApplicationWindow(ZLApplication application) {
		// TODO: implement
		//myApplicationWindow = new ZLAndroidApplicationWindow(application);
		//return myApplicationWindow;
		return null;
	}

	/*
	static JButton createButton(String key) {
		String text = getButtonText(key).replace("&", "");
		return new JButton(text);
	}
	*/

	public ZLOptionsDialog createOptionsDialog(String key, Runnable exitAction, Runnable applyAction, boolean showApplyButton) {
		return new ZLAndroidOptionsDialog(myActivity, getResource().getResource(key), exitAction, applyAction);
	}

	public ZLDialog createDialog(String key) {
		return new ZLAndroidDialog(myActivity, getResource().getResource(key));
	}

	public void wait(String key, final Runnable runnable) {
		final ProgressDialog progress = ProgressDialog.show(myActivity, null, getWaitMessageText(key), true, false);
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				progress.dismiss();
			}
		};
		new Thread(new Runnable() {
			public void run() {
				runnable.run();
				handler.sendEmptyMessage(0);
			}
		}).start();
	}
}
