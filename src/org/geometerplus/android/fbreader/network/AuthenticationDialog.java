/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.network;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.authentication.*;


class AuthenticationDialog {
	private static AuthenticationDialog ourDialog;

	public static AuthenticationDialog Instance() {
		if (ourDialog == null) {
			ourDialog = new AuthenticationDialog();
		}
		return ourDialog;
	}

	private final ZLResource myResource = ZLResource.resource("dialog").getResource("AuthenticationDialog");

	private NetworkCatalogTree myTree;
	private String myErrorMessage;

	public void show(NetworkCatalogTree tree) {
		myTree = tree;
		myErrorMessage = null;
		NetworkLibraryActivity.Instance.showDialog(NetworkLibraryActivity.DIALOG_AUTHENTICATION);
	}


	private class DialogHandler extends Handler {

		private final Activity myActivity;

		public DialogHandler(Activity activity) {
			myActivity = activity;
		}

		public void handleMessage(Message message) {
			if (message.what == -2) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateAccountDependents();
				library.synchronize();
				((NetworkLibraryActivity) myActivity).getAdapter().resetTree();
				((NetworkLibraryActivity) myActivity).getListView().invalidateViews();
			} else if (message.what == -1) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateAccountDependents();
				library.synchronize();
				((NetworkLibraryActivity) myActivity).getAdapter().resetTree();
				((NetworkLibraryActivity) myActivity).getListView().invalidateViews();
				myErrorMessage = (String) message.obj;
				myActivity.showDialog(NetworkLibraryActivity.DIALOG_AUTHENTICATION);
			} else if (message.what == 1) {
				final NetworkAuthenticationManager mgr = myTree.Item.Link.authenticationManager();
				if (mgr.UserNameOption.getValue().length() == 0) {
					myErrorMessage = myResource.getResource("loginIsEmpty").getValue();
					myActivity.showDialog(NetworkLibraryActivity.DIALOG_AUTHENTICATION);
					return;
				}
				final String password = (String) message.obj;
				final Runnable runnable = new Runnable() {
					public void run() {
						String err = mgr.authorise(password);
						if (err != null) {
							mgr.logOut();
							DialogHandler.this.sendMessage(DialogHandler.this.obtainMessage(-1, err));
							return;
						}
						if (mgr.needsInitialization()) {
							err = mgr.initialize();
							if (err != null) {
								mgr.logOut();
								DialogHandler.this.sendMessage(DialogHandler.this.obtainMessage(-1, err));
								return;
							}
						}
						DialogHandler.this.sendEmptyMessage(-2);
					}
				};
				((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("authentication", runnable, myActivity);
			} else {
				final NetworkAuthenticationManager mgr = myTree.Item.Link.authenticationManager();
				final Runnable runnable = new Runnable() {
					public void run() {
						if (mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE) {
							mgr.logOut();
							DialogHandler.this.sendEmptyMessage(-2);
						}
					}
				};
				((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("signOut", runnable, myActivity);
			}
		}
	}


	public Dialog createDialog(Activity activity) {
		final View layout = activity.getLayoutInflater().inflate(R.layout.network_authentication_dialog, null);

		final TextView login = (TextView) layout.findViewById(R.id.network_authentication_login);
		final TextView password = (TextView) layout.findViewById(R.id.network_authentication_password);
		login.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		password.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		final TextView loginText = (TextView) layout.findViewById(R.id.network_authentication_login_text);
		loginText.setText(myResource.getResource("login").getValue());
		loginText.getLayoutParams().height = login.getMeasuredHeight();

		final TextView passwordText = (TextView) layout.findViewById(R.id.network_authentication_password_text);
		passwordText.setText(myResource.getResource("password").getValue());
		passwordText.getLayoutParams().height = password.getMeasuredHeight();

		final NetworkAuthenticationManager mgr = myTree.Item.Link.authenticationManager();

		// TODO: implement skipIP option

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Handler handler = new DialogHandler(activity);
		return new AlertDialog.Builder(activity)
			.setView(layout)
			.setTitle(myResource.getResource("title").getValue())
			.setPositiveButton(buttonResource.getResource("ok").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					AlertDialog alert = (AlertDialog) dialog;
					final TextView loginView = (TextView) alert.findViewById(R.id.network_authentication_login);
					final TextView passwordView = (TextView) alert.findViewById(R.id.network_authentication_password);
					final String login = loginView.getText().toString();
					final String password = passwordView.getText().toString();
					mgr.UserNameOption.setValue(login);
					handler.sendMessage(handler.obtainMessage(1, password));
				}
			})
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					handler.sendEmptyMessage(0);
				}
			})
			.create();
	}

	public void prepareDialog(Dialog dialog) {
		final NetworkAuthenticationManager mgr = myTree.Item.Link.authenticationManager();

		final TextView login = (TextView) dialog.findViewById(R.id.network_authentication_login);
		login.setText(mgr.UserNameOption.getValue());

		final TextView password = (TextView) dialog.findViewById(R.id.network_authentication_password);
		password.setText("");

		final TextView error = (TextView) dialog.findViewById(R.id.network_authentication_error);
		if (myErrorMessage == null) {
			error.setVisibility(View.GONE);
			error.setText("");
		} else {
			error.setVisibility(View.VISIBLE);
			error.setText(myErrorMessage);
		}
		View dlgView = dialog.findViewById(R.id.network_authentication_dialog);
		dlgView.invalidate();
		dlgView.requestLayout();
	}
}
