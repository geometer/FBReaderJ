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

//import java.lang.reflect.*;
//import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
//import android.app.Dialog;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
//import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

//import org.geometerplus.android.util.UIUtil;

import org.geometerplus.zlibrary.core.resources.ZLResource;
//import org.geometerplus.zlibrary.core.network.ZLNetworkException;

//import org.geometerplus.fbreader.network.NetworkException;
//import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

public class UserRegistrationActivity extends Activity {
	private final ZLResource myResource = ZLResource.resource("userRegistration");

	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}

	private Button findButton(int resourceId) {
		return (Button)findViewById(resourceId);
	}

	private String getViewText(int resourceId) {
		return findTextView(resourceId).getText().toString().trim();
	}

	private void setViewTextFromResource(int resourceId, String fbResourceKey) {
		findTextView(resourceId).setText(myResource.getResource(fbResourceKey).getValue());
	}

	private void setErrorMessageFromResource(String resourceKey) {
		final TextView errorLabel = findTextView(R.id.user_registration_error);
		errorLabel.setVisibility(View.VISIBLE);
		errorLabel.setText(myResource.getResource("error").getResource(resourceKey).getValue());
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		setContentView(R.layout.user_registration);

		setTitle(myResource.getResource("title").getValue());
		setViewTextFromResource(R.id.user_registration_login_text, "login");
		setViewTextFromResource(R.id.user_registration_password_text, "password");
		setViewTextFromResource(R.id.user_registration_confirm_password_text, "confirmPassword");
		setViewTextFromResource(R.id.user_registration_email_text, "email");

		final TextView errorLabel = findTextView(R.id.user_registration_error);
		errorLabel.setVisibility(View.GONE);
		errorLabel.setText("");

		final Intent intent = getIntent();

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button okButton = findButton(R.id.user_registration_ok_button);
		final Button cancelButton = findButton(R.id.user_registration_cancel_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String userName = getViewText(R.id.user_registration_login);
				final String password = getViewText(R.id.user_registration_password);
				final String confirmPassword = getViewText(R.id.user_registration_confirm_password);
				final String email = getViewText(R.id.user_registration_email);

				if (userName.length() == 0) {
					setErrorMessageFromResource("usernameNotSpecified");
					return;
				}
				if (!password.equals(confirmPassword)) {
					setErrorMessageFromResource("passwordDoesNotMatch");
					return;
				}
				if (password.length() == 0) {
					setErrorMessageFromResource("passwordNotSpecified");
					return;
				}
				if (email.length() == 0) {
					setErrorMessageFromResource("emailNotSpecified");
					return;
				}
				final int atPos = email.indexOf("@");
				if (atPos == -1 || email.indexOf(".", atPos) == -1) {
					setErrorMessageFromResource("invalidEMail");
					return;
				}
				final Intent data = new Intent();
				data.putExtra("userName", userName);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		final List<String> emails = new RegistrationUtils(getApplicationContext()).eMails();

		final Button emailListButton = findButton(R.id.user_registration_email_button);
		final TextView emailTextView = findTextView(R.id.user_registration_email);
		emailListButton.setVisibility(emails.size() > 1 ? View.VISIBLE : View.GONE);
		if (!emails.isEmpty()) {
			emailTextView.setText(emails.get(0));
		}

		if (!emails.isEmpty()) {
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && which < emails.size()) {
						emailTextView.setText(emails.get(which));
					}
					dialog.dismiss();
				}
			};

			emailListButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final String selectedEmail = emailTextView.getText().toString().trim();
					final int selected = emails.indexOf(selectedEmail);
					final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
					final AlertDialog dialog = new AlertDialog.Builder(UserRegistrationActivity.this)
						.setSingleChoiceItems(emails.toArray(new String[emails.size()]), selected, listener)
						.setTitle(myResource.getResource("email").getValue())
						.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
						.create();

					dialog.show();
				}
			});
		}
	}
/*
	@Override
	protected void onPositive(DialogInterface dialog) {
		AlertDialog alert = (AlertDialog) dialog;

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
*/
}
