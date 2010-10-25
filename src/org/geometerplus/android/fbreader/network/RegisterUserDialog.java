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

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.NetworkException;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class RegisterUserDialog extends NetworkDialog {
	private RegistrationUtils myRegistrationUtils;

	private String myLogin;
	private String myPassword;
	private String myConfirmPassword;
	private String myEmail;

	public RegisterUserDialog() {
		super("RegisterUserDialog");
	}

	@Override
	protected void clearData() {
		myLogin = myPassword = myConfirmPassword = myEmail = null;
	}

	@Override
	public View createLayout() {
		final View layout = myActivity.getLayoutInflater().inflate(R.layout.network_register_user_dialog, null);

		((TextView) layout.findViewById(R.id.network_register_login_text)).setText(myResource.getResource("login").getValue());
		((TextView) layout.findViewById(R.id.network_register_password_text)).setText(myResource.getResource("password").getValue());
		((TextView) layout.findViewById(R.id.network_register_confirm_password_text)).setText(myResource.getResource("confirmPassword").getValue());
		((TextView) layout.findViewById(R.id.network_register_email_text)).setText(myResource.getResource("email").getValue());

		myRegistrationUtils = new RegistrationUtils(myActivity.getApplicationContext());
		final List<String> emails = myRegistrationUtils.eMails();

		if (!emails.isEmpty()) {
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < emails.size()) {
						myEmail = emails.get(which);
					}
					dialog.dismiss();
				}
			};

			((Button) layout.findViewById(R.id.network_register_email_button)).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					myLogin = ((TextView) layout.findViewById(R.id.network_register_login)).getText().toString().trim();
					myPassword = ((TextView) layout.findViewById(R.id.network_register_password)).getText().toString();
					myConfirmPassword = ((TextView) layout.findViewById(R.id.network_register_confirm_password)).getText().toString();
					myEmail = ((TextView) layout.findViewById(R.id.network_register_email)).getText().toString().trim();

					final int selected = emails.indexOf(myEmail);
					final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
					final AlertDialog dialog = new AlertDialog.Builder(myActivity)
						.setSingleChoiceItems(emails.toArray(new String[emails.size()]), selected, listener)
						.setTitle(myResource.getResource("email").getValue())
						.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
						.create();

					dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							myActivity.showDialog(myId);
						}
					});

					myActivity.dismissDialog(myId);
					dialog.show();
				}
			});
		}

		return layout;
	}

	@Override
	protected void onPositive(DialogInterface dialog) {
		AlertDialog alert = (AlertDialog) dialog;
		myLogin = ((TextView) alert.findViewById(R.id.network_register_login)).getText().toString().trim();
		myPassword = ((TextView) alert.findViewById(R.id.network_register_password)).getText().toString();
		myConfirmPassword = ((TextView) alert.findViewById(R.id.network_register_confirm_password)).getText().toString();
		myEmail = ((TextView) alert.findViewById(R.id.network_register_email)).getText().toString().trim();

		if (myLogin.length() == 0) {
			myLogin = null;
			final ZLNetworkException error =
				new ZLNetworkException(NetworkException.ERROR_LOGIN_WAS_NOT_SPECIFIED);
			sendError(true, false, error.getMessage());
			return;
		}
		if (!myPassword.equals(myConfirmPassword)) {
			final String err = myResource.getResource("differentPasswords").getValue();
			myPassword = null;
			myConfirmPassword = null;
			sendError(true, false, err);
			return;
		}
		if (myEmail.length() == 0) {
			myEmail = null;
			final ZLNetworkException error =
				new ZLNetworkException(NetworkException.ERROR_EMAIL_WAS_NOT_SPECIFIED);
			sendError(true, false, error.getMessage());
			return;
		}
		final int atPos = myEmail.indexOf("@");
		if (atPos == -1 || myEmail.indexOf(".", atPos) == -1) {
			final ZLNetworkException error =
				new ZLNetworkException(NetworkException.ERROR_INVALID_EMAIL);
			sendError(true, false, error.getMessage());
			return;
		}
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				boolean doRestart = true;
				try {
					mgr.registerUser(myLogin, myPassword, myEmail);
					if (mgr.mayBeAuthorised(true) && mgr.needsInitialization()) {
						doRestart = false;
						mgr.initialize();
					}
				} catch (ZLNetworkException e) {
					mgr.logOut();
					sendError(doRestart, false, e.getMessage());
					return;
				}
				sendSuccess(false);
			}
		};
		UIUtil.wait("registerUser", runnable, myActivity);
	}

	@Override
	protected void onNegative(DialogInterface dialog) {
		sendCancel(false);
	}

	@Override
	public void prepareDialogInternal(Dialog dialog) {
		final List<String> emails = myRegistrationUtils.eMails();
		dialog.findViewById(R.id.network_register_email_button).setVisibility((emails.size() > 1) ? View.VISIBLE : View.GONE);
		if (!emails.isEmpty() && (myEmail == null || myEmail.length() == 0)) {
			myEmail = emails.get(0);
		}

		((TextView) dialog.findViewById(R.id.network_register_login)).setText(myLogin);
		((TextView) dialog.findViewById(R.id.network_register_password)).setText(myPassword);
		((TextView) dialog.findViewById(R.id.network_register_confirm_password)).setText(myConfirmPassword);
		((TextView) dialog.findViewById(R.id.network_register_email)).setText(myEmail);

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
