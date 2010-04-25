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

	private NetworkLink myLink;
	private String myErrorMessage;
	private Runnable myOnSuccessRunnable;

	public void show(NetworkLink link, Runnable onSuccessRunnable) {
		myLink = link;
		myErrorMessage = null;
		myOnSuccessRunnable = onSuccessRunnable;
		if (NetworkLibraryActivity.Instance != null) {
			NetworkLibraryActivity.Instance.getTopLevelActivity().showDialog(NetworkLibraryActivity.DIALOG_AUTHENTICATION);
		}
	}

	public Dialog createDialog(final Activity activity) {
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

		final NetworkAuthenticationManager mgr = myLink.authenticationManager();

		// TODO: implement skipIP option

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateAccountDependents();
				library.synchronize();
				if (NetworkLibraryActivity.Instance != null) {
					NetworkLibraryActivity.Instance.getAdapter().resetTree();
					NetworkLibraryActivity.Instance.fireOnModelChanged();
				}
				if (message.what < 0) {
					myErrorMessage = (String) message.obj;
					activity.showDialog(NetworkLibraryActivity.DIALOG_AUTHENTICATION);
				} else if (message.what > 0) {
					if (myOnSuccessRunnable != null) {
						myOnSuccessRunnable.run();
					}
				}
			}
		};

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final NetworkAuthenticationManager mgr = myLink.authenticationManager();
				if (which == DialogInterface.BUTTON_POSITIVE) {
					AlertDialog alert = (AlertDialog) dialog;
					final String login = ((TextView) alert.findViewById(R.id.network_authentication_login)).getText().toString().trim();
					final String password = ((TextView) alert.findViewById(R.id.network_authentication_password)).getText().toString();

					if (login.length() == 0) {
						String err = myResource.getResource("loginIsEmpty").getValue();
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					mgr.UserNameOption.setValue(login);
					final Runnable runnable = new Runnable() {
						public void run() {
							String err = mgr.authorise(password);
							if (err != null) {
								mgr.logOut();
								handler.sendMessage(handler.obtainMessage(-1, err));
								return;
							}
							if (mgr.needsInitialization()) {
								err = mgr.initialize();
								if (err != null) {
									mgr.logOut();
									handler.sendMessage(handler.obtainMessage(-1, err));
									return;
								}
							}
							handler.sendEmptyMessage(1);
						}
					};
					((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("authentication", runnable, activity);
				} else {
					final Runnable runnable = new Runnable() {
						public void run() {
							if (mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE) {
								mgr.logOut();
								handler.sendEmptyMessage(0);
							}
						}
					};
					((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("signOut", runnable, activity);
				}
			}
		};

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		return new AlertDialog.Builder(activity)
			.setView(layout)
			.setTitle(myResource.getResource("title").getValue())
			.setPositiveButton(buttonResource.getResource("ok").getValue(), listener)
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
			.create();
	}

	public void prepareDialog(Dialog dialog) {
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();

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
