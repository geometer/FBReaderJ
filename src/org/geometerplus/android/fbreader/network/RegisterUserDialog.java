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
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkErrors;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class RegisterUserDialog extends NetworkDialog {

	private String myLogin;
	private String myPassword;
	private String myEmail;

	public RegisterUserDialog() {
		super("RegisterUserDialog");
	}

	private void clearUserInfo() {
		myLogin = myPassword = myEmail = null;
	}

	public Dialog createDialog(final Activity activity) {
		final View layout = activity.getLayoutInflater().inflate(R.layout.network_register_user_dialog, null);

		setupLabel(layout, R.id.network_register_login_text, "login", R.id.network_register_login);
		setupLabel(layout, R.id.network_register_password_text, "password", R.id.network_register_password);
		setupLabel(layout, R.id.network_register_confirm_password_text, "confirmPassword", R.id.network_register_confirm_password);
		setupLabel(layout, R.id.network_register_email_text, "email", R.id.network_register_email);

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateVisibility();
				library.synchronize();
				if (NetworkView.Instance().isInitialized()) {
					NetworkView.Instance().fireModelChanged();
				}
				if (message.what < 0) {
					if (message.what == -2) {
						final ZLResource dialogResource = ZLResource.resource("dialog");
						final ZLResource boxResource = dialogResource.getResource("networkError");
						final ZLResource buttonResource = dialogResource.getResource("button");
						new AlertDialog.Builder(activity)
							.setTitle(boxResource.getResource("title").getValue())
							.setMessage((String) message.obj)
							.setIcon(0)
							.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
							.create().show();
					} else {
						myErrorMessage = (String) message.obj;
						activity.showDialog(NetworkDialog.DIALOG_REGISTER_USER);
						return;
					}
				} else if (message.what > 0) {
					if (myOnSuccessRunnable != null) {
						myOnSuccessRunnable.run();
					}
				}
				clearUserInfo();
			}
		};

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final NetworkAuthenticationManager mgr = myLink.authenticationManager();
				if (which == DialogInterface.BUTTON_POSITIVE) {
					AlertDialog alert = (AlertDialog) dialog;
					myLogin = ((TextView) alert.findViewById(R.id.network_register_login)).getText().toString().trim();
					myPassword = ((TextView) alert.findViewById(R.id.network_register_password)).getText().toString();
					final String confirmPassword = ((TextView) alert.findViewById(R.id.network_register_confirm_password)).getText().toString();
					myEmail = ((TextView) alert.findViewById(R.id.network_register_email)).getText().toString().trim();

					if (myLogin.length() == 0) {
						myLogin = null;
						final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_LOGIN_WAS_NOT_SPECIFIED);
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					if (!myPassword.equals(confirmPassword)) {
						final String err = myResource.getResource("differentPasswords").getValue();
						myPassword = null;
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					if (myEmail.length() == 0) {
						myEmail = null;
						final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_EMAIL_WAS_NOT_SPECIFIED);
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					final int atPos = myEmail.indexOf("@");
					if (atPos == -1 || myEmail.indexOf(".", atPos) == -1) {
						final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_INVALID_EMAIL);
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					final Runnable runnable = new Runnable() {
						public void run() {
							String err = mgr.registerUser(myLogin, myPassword, myEmail);
							if (err != null) {
								mgr.logOut();
								handler.sendMessage(handler.obtainMessage(-1, err));
								return;
							}
							if (mgr.isAuthorised(true).Status != ZLBoolean3.B3_FALSE && mgr.needsInitialization()) {
								err = mgr.initialize();
								if (err != null) {
									mgr.logOut();
									handler.sendMessage(handler.obtainMessage(-2, err));
									return;
								}
							}
							handler.sendEmptyMessage(1);
						}
					};
					((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("registerUser", runnable, activity);
				} else {
					handler.sendEmptyMessage(0);
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

		((TextView) dialog.findViewById(R.id.network_register_login)).setText((myLogin != null) ? myLogin : "");
		((TextView) dialog.findViewById(R.id.network_register_password)).setText((myPassword != null) ? myPassword : "");
		((TextView) dialog.findViewById(R.id.network_register_confirm_password)).setText((myPassword != null) ? myPassword : "");
		((TextView) dialog.findViewById(R.id.network_register_email)).setText((myEmail != null) ? myEmail : "");

		final TextView error = (TextView) dialog.findViewById(R.id.network_register_error);
		if (myErrorMessage == null) {
			error.setVisibility(View.GONE);
			error.setText("");
		} else {
			error.setVisibility(View.VISIBLE);
			error.setText(myErrorMessage);
		}

		View dlgView = dialog.findViewById(R.id.network_register_user_dialog);
		dlgView.invalidate();
		dlgView.requestLayout();
	}
}
