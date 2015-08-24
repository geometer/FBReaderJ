/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.network.litres;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.authentication.litres.*;

import org.geometerplus.android.fbreader.network.Util;
import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;

abstract class RegistrationActivity extends Activity implements UserRegistrationConstants {
	protected final ActivityNetworkContext myNetworkContext = new ActivityNetworkContext(this);

	protected ZLResource myResource;

	protected String myCatalogURL;
	private String mySignInURL;
	private String mySignUpURL;
	protected String myRecoverPasswordURL;

	protected void reportSuccess(String username, String password, String sid) {
		final Intent data = new Intent(Util.SIGNIN_ACTION);
		data.putExtra(USER_REGISTRATION_USERNAME, username);
		data.putExtra(USER_REGISTRATION_PASSWORD, password);
		data.putExtra(USER_REGISTRATION_LITRES_SID, sid);
		data.putExtra(CATALOG_URL, myCatalogURL);
		sendBroadcast(data);
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		myCatalogURL = intent.getStringExtra(CATALOG_URL);
		mySignInURL = intent.getStringExtra(SIGNIN_URL);
		mySignUpURL = intent.getStringExtra(SIGNUP_URL);
		myRecoverPasswordURL = intent.getStringExtra(RECOVER_PASSWORD_URL);
	}

	protected static interface NetworkRunnable {
		void run() throws ZLNetworkException;
	}

	protected class RegistrationNetworkRunnable implements NetworkRunnable {
		final String Username;
		final String Password;
		final String Email;
		final LitResRegisterUserXMLReader XmlReader = new LitResRegisterUserXMLReader();

		public RegistrationNetworkRunnable(String username, String password, String email) {
			Username = username;
			Password = password;
			Email = email;
		}

		public void run() throws ZLNetworkException {
			final LitResNetworkRequest request = new LitResNetworkRequest(mySignUpURL, XmlReader);
			request.addPostParameter("new_login", Username);
			request.addPostParameter("new_pwd1", Password);
			request.addPostParameter("mail", Email);
			myNetworkContext.perform(request);
		}
	}

	protected class SignInNetworkRunnable implements NetworkRunnable {
		final String Username;
		final String Password;
		final LitResLoginXMLReader XmlReader = new LitResLoginXMLReader();

		public SignInNetworkRunnable(String username, String password) {
			Username = username;
			Password = password;
		}

		public void run() throws ZLNetworkException {
			final LitResNetworkRequest request = new LitResNetworkRequest(mySignInURL, XmlReader);
			request.addPostParameter("login", Username);
			request.addPostParameter("pwd", Password);
			myNetworkContext.perform(request);
		}
	}

	protected static interface PostRunnable {
		void run(ZLNetworkException exception);
	}

	protected synchronized void runWithMessage(String key, final NetworkRunnable action, final PostRunnable postAction) {
		final String message =
			ZLResource.resource("dialog").getResource("waitMessage").getResource(key).getValue();
		final ProgressDialog progress = ProgressDialog.show(this, null, message, true, false);

		new Thread(new Runnable() {
			public void run() {
				try {
					action.run();
					postRun(null);
				} catch (ZLNetworkException e) {
					postRun(e);
				}
			}

			private void postRun(final ZLNetworkException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						progress.dismiss();
						postAction.run(e);
					}
				});
			}
		}).start();
	}

	protected void setupEmailControl(View emailControl, String eMailToSkip) {
		final Button emailListButton = (Button)emailControl.findViewById(R.id.lr_email_button);
		final TextView emailTextView = (TextView)emailControl.findViewById(R.id.lr_email_edit);

		final List<String> emails = new RegistrationUtils(getApplicationContext()).eMails();

		emailListButton.setVisibility(emails.size() > 1 ? View.VISIBLE : View.GONE);
		if (!emails.isEmpty()) {
			emailTextView.setText(emails.get(0));
			for (String e : emails) {
				if (!e.equals(eMailToSkip)) {
					emailTextView.setText(e);
					break;
				}
			}

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
					final AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
						.setSingleChoiceItems(emails.toArray(new String[emails.size()]), selected, listener)
						.setTitle(myResource.getResource("email").getValue())
						.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
						.create();

					dialog.show();
				}
			});
		}
	}
}
