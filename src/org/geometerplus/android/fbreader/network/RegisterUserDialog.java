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

import android.app.Dialog;
import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.NetworkErrors;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class RegisterUserDialog extends NetworkDialog {

	private String myLogin;
	private String myPassword;
	private String myEmail;

	public RegisterUserDialog() {
		super("RegisterUserDialog");
	}

	@Override
	protected void clearData() {
		myLogin = myPassword = myEmail = null;
	}

	@Override
	public View createLayout() {
		final View layout = myActivity.getLayoutInflater().inflate(R.layout.network_register_user_dialog, null);

		((TextView) layout.findViewById(R.id.network_register_login_text)).setText(myResource.getResource("login").getValue());
		((TextView) layout.findViewById(R.id.network_register_password_text)).setText(myResource.getResource("password").getValue());
		((TextView) layout.findViewById(R.id.network_register_confirm_password_text)).setText(myResource.getResource("confirmPassword").getValue());
		((TextView) layout.findViewById(R.id.network_register_email_text)).setText(myResource.getResource("email").getValue());

		return layout;
	}

	@Override
	protected void onPositive(DialogInterface dialog) {
		AlertDialog alert = (AlertDialog) dialog;
		myLogin = ((TextView) alert.findViewById(R.id.network_register_login)).getText().toString().trim();
		myPassword = ((TextView) alert.findViewById(R.id.network_register_password)).getText().toString();
		final String confirmPassword = ((TextView) alert.findViewById(R.id.network_register_confirm_password)).getText().toString();
		myEmail = ((TextView) alert.findViewById(R.id.network_register_email)).getText().toString().trim();

		if (myLogin.length() == 0) {
			myLogin = null;
			final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_LOGIN_WAS_NOT_SPECIFIED);
			sendError(true, false, err);
			return;
		}
		if (!myPassword.equals(confirmPassword)) {
			final String err = myResource.getResource("differentPasswords").getValue();
			myPassword = null;
			sendError(true, false, err);
			return;
		}
		if (myEmail.length() == 0) {
			myEmail = null;
			final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_EMAIL_WAS_NOT_SPECIFIED);
			sendError(true, false, err);
			return;
		}
		final int atPos = myEmail.indexOf("@");
		if (atPos == -1 || myEmail.indexOf(".", atPos) == -1) {
			final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_INVALID_EMAIL);
			sendError(true, false, err);
			return;
		}
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				String err = mgr.registerUser(myLogin, myPassword, myEmail);
				if (err != null) {
					mgr.logOut();
					sendError(true, false, err);
					return;
				}
				if (mgr.isAuthorised(true).Status != ZLBoolean3.B3_FALSE && mgr.needsInitialization()) {
					err = mgr.initialize();
					if (err != null) {
						mgr.logOut();
						sendError(false, false, err);
						return;
					}
				}
				sendSuccess(false);
			}
		};
		((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("registerUser", runnable, myActivity);
	}

	@Override
	protected void onNegative(DialogInterface dialog) {
		sendCancel(false);
	}

	@Override
	public void prepareDialog(Dialog dialog) {
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
