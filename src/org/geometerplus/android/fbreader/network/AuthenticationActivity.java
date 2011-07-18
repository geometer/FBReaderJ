/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.R;

public class AuthenticationActivity extends Activity {
	private static final String AREA_KEY = "area";
	private static final String HOST_KEY = "host";
	static final String SCHEME_KEY = "scheme";
	static final String USERNAME_KEY = "username";
	static final String PASSWORD_KEY = "password";
	static final String ERROR_KEY = "error";
	static final String SHOW_SIGNUP_LINK_KEY = "showSignupLink";
                  
	static final int RESULT_SIGNUP = RESULT_FIRST_USER;

	static class CredentialsCreator implements ZLNetworkManager.CredentialsCreator {
		private final Activity myActivity;
		private final int myCode;

		private volatile String myUsername;
		private volatile String myPassword;
        
		CredentialsCreator(Activity activity, int code) {
			myActivity = activity;
			myCode = code;
		}

		synchronized void onDataReceived(int resultCode, Intent data) {
			if (resultCode == RESULT_OK && data != null) {
				myUsername = data.getStringExtra(USERNAME_KEY);
				myPassword = data.getStringExtra(PASSWORD_KEY);
			}
			notify();
		}

		public Credentials createCredentials(String scheme, AuthScope scope) {
			if (!"basic".equalsIgnoreCase(scope.getScheme())) {
				return null;
			}

			final Intent intent = new Intent();
			final String host = scope.getHost();
			final String area = scope.getRealm();
			final ZLStringOption option = new ZLStringOption("username", host + ":" + area, "");
			intent.setClass(myActivity, AuthenticationActivity.class);
			intent.putExtra(HOST_KEY, host);
			intent.putExtra(AREA_KEY, area);
			intent.putExtra(SCHEME_KEY, scheme);
			intent.putExtra(USERNAME_KEY, option.getValue());
			myActivity.startActivityForResult(intent, myCode);
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
        
			Credentials creds = null;
			if (myUsername != null && myPassword != null) {
				option.setValue(myUsername);
				creds = new UsernamePasswordCredentials(myUsername, myPassword);
			}
			myUsername = null;
			myPassword = null;
			return creds;
		}
	}

	private ZLResource myResource;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setResult(RESULT_CANCELED);
		setContentView(R.layout.authentication);

		final Intent intent = getIntent();
		final String host = intent.getStringExtra(HOST_KEY);
		final String area = intent.getStringExtra(AREA_KEY);
		final String username = intent.getStringExtra(USERNAME_KEY);
		final String error = intent.getStringExtra(ERROR_KEY);
		final boolean showSignupLink = intent.getBooleanExtra(SHOW_SIGNUP_LINK_KEY, false);

		myResource = ZLResource.resource("dialog").getResource("AuthenticationDialog");

		setTitle(host != null ? host : myResource.getResource("title").getValue());

		if (area != null && !"".equals(area)) {
			findTextView(R.id.authentication_subtitle).setText(area);
		} else {
			findTextView(R.id.authentication_subtitle).setVisibility(View.GONE);
		}
		final TextView warningView = findTextView(R.id.authentication_unencrypted_warning);
		if ("https".equalsIgnoreCase(intent.getStringExtra(SCHEME_KEY))) {
			warningView.setVisibility(View.GONE);
		} else {
			warningView.setText(myResource.getResource("unencryptedWarning").getValue());
		}
		findTextView(R.id.authentication_username_label).setText(
			myResource.getResource("login").getValue()
		);
		findTextView(R.id.authentication_password_label).setText(
			myResource.getResource("password").getValue()
		);

		final TextView usernameView = findTextView(R.id.authentication_username);
		usernameView.setText(username);

		final TextView errorView = findTextView(R.id.authentication_error);
		if (error != null && !"".equals(error)) {
			errorView.setVisibility(View.VISIBLE);
			errorView.setText(error);
		} else {
			errorView.setVisibility(View.GONE);
		}

		if (showSignupLink) {
			findViewById(R.id.authentication_signup_box).setVisibility(View.VISIBLE);
			final TextView signupView = (TextView)findViewById(R.id.authentication_signup);
			signupView.setText(myResource.getResource("register").getValue());
			signupView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					setResult(RESULT_SIGNUP);
					finish();
				}
			});
		} else {
			findViewById(R.id.authentication_signup_box).setVisibility(View.GONE);
		}

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		final Button okButton = findButton(R.id.authentication_ok_button);
		okButton.setText(buttonResource.getResource("ok").getValue());
		okButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				final Intent data = new Intent();
				data.putExtra(
					USERNAME_KEY,
					usernameView.getText().toString()
				);
				data.putExtra(
					PASSWORD_KEY,
					findTextView(R.id.authentication_password).getText().toString()
				);
				setResult(RESULT_OK, data);
				finish();
			}
		});

		final Button cancelButton = findButton(R.id.authentication_cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}

	private Button findButton(int resourceId) {
		return (Button)findViewById(resourceId);
	}
}
