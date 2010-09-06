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

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.NetworkErrors;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class RegisterUserDialog extends NetworkDialog {

	private String myLogin;
	private String myPassword;
	private String myConfirmPassword;
	private String myEmail;

	private ArrayList<String> mySystemEmails = new ArrayList<String>();

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

		mySystemEmails.clear();
		collectEMails(myActivity.getApplicationContext(), mySystemEmails);

		if (!mySystemEmails.isEmpty()) {
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < mySystemEmails.size()) {
						myEmail = mySystemEmails.get(which);
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

					final int selected = mySystemEmails.indexOf(myEmail);
					final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
					final AlertDialog dialog = new AlertDialog.Builder(myActivity)
						.setSingleChoiceItems(mySystemEmails.toArray(new String[mySystemEmails.size()]), selected, listener)
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
			final String err = NetworkErrors.errorMessage(NetworkErrors.ERROR_LOGIN_WAS_NOT_SPECIFIED);
			sendError(true, false, err);
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
	public void prepareDialogInternal(Dialog dialog) {
		dialog.findViewById(R.id.network_register_email_button).setVisibility((mySystemEmails.size() > 1) ? View.VISIBLE : View.GONE);
		if (mySystemEmails.size() == 1 && (myEmail == null || myEmail.length() == 0)) {
			myEmail = mySystemEmails.get(0);
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


	private static void collectEMails(Context context, List<String> emails) {
		try {
			final Class<?> cls$AccountManager = Class.forName("android.accounts.AccountManager");
			final Class<?> cls$Account = Class.forName("android.accounts.Account");

			final Method meth$AccountManager$get = cls$AccountManager.getMethod("get", Context.class);
			final Method meth$AccountManager$getAccountsByType = cls$AccountManager.getMethod("getAccountsByType", String.class);
			final Field fld$Account$name = cls$Account.getField("name");

			if (meth$AccountManager$get.getReturnType() == cls$AccountManager
					&& meth$AccountManager$getAccountsByType.getReturnType().getComponentType() == cls$Account
					&& fld$Account$name.getType() == String.class) {
				final Object mgr = meth$AccountManager$get.invoke(null, context);
				final Object[] accountsByType = (Object[]) meth$AccountManager$getAccountsByType.invoke(mgr, "com.google"); 
				for (Object a: accountsByType) {
					final String value = (String) fld$Account$name.get(a);
					if (value != null && value.length() > 0) {
						emails.add(value);
					}
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		}
	}
}
