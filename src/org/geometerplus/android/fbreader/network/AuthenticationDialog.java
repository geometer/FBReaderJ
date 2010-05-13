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
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class AuthenticationDialog extends NetworkDialog {

	public AuthenticationDialog() {
		super("AuthenticationDialog");
	}

	public Dialog createDialog(final Activity activity) {
		final View layout = activity.getLayoutInflater().inflate(R.layout.network_authentication_dialog, null);

		setupLabel(layout, R.id.network_authentication_login_text, "login", R.id.network_authentication_login);
		setupLabel(layout, R.id.network_authentication_password_text, "password", R.id.network_authentication_password);

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				if (!NetworkView.Instance().isInitialized()) {
					return;
				}
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateVisibility();
				library.synchronize();
				NetworkView.Instance().fireModelChanged();
				if (message.what < 0) {
					myErrorMessage = (String) message.obj;
					activity.showDialog(NetworkDialog.DIALOG_AUTHENTICATION);
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
						final String err = myResource.getResource("loginIsEmpty").getValue();
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

		final TextView registerText = (TextView) layout.findViewById(R.id.network_authentication_register);
		registerText.setText(myResource.getResource("register").getValue());
		registerText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final NetworkAuthenticationManager mgr = myLink.authenticationManager();
				if (mgr.registrationSupported()) {
					activity.dismissDialog(NetworkDialog.DIALOG_AUTHENTICATION);
					NetworkDialog.show(activity, NetworkDialog.DIALOG_REGISTER_USER, myLink, new Runnable() {
						public void run() {
							if (mgr.isAuthorised(true).Status == ZLBoolean3.B3_TRUE) {
								if (myOnSuccessRunnable != null) {
									myOnSuccessRunnable.run();
								}
							} else {
								NetworkDialog.show(activity, NetworkDialog.DIALOG_AUTHENTICATION, myLink, myOnSuccessRunnable);
							}
						}
					});
				}
			}
		});

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

		((TextView) dialog.findViewById(R.id.network_authentication_login)).setText(mgr.UserNameOption.getValue());
		((TextView) dialog.findViewById(R.id.network_authentication_password)).setText("");

		final TextView error = (TextView) dialog.findViewById(R.id.network_authentication_error);
		if (myErrorMessage == null) {
			error.setVisibility(View.GONE);
			error.setText("");
		} else {
			error.setVisibility(View.VISIBLE);
			error.setText(myErrorMessage);
		}

		dialog.findViewById(R.id.network_authentication_register).setVisibility(mgr.registrationSupported() ? View.VISIBLE : View.GONE);

		View dlgView = dialog.findViewById(R.id.network_authentication_dialog);
		dlgView.invalidate();
		dlgView.requestLayout();
	}
}
