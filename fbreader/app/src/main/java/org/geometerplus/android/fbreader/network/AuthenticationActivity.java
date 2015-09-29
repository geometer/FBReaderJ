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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

public class AuthenticationActivity extends Activity {
	private static final Map<Long,Runnable> ourOnSuccessRunnableMap =
		Collections.synchronizedMap(new HashMap<Long,Runnable>());
	private static volatile long ourNextCode;

	static Intent registerRunnable(Intent intent, Runnable action) {
		synchronized (ourOnSuccessRunnableMap) {
			if (action != null) {
				ourOnSuccessRunnableMap.put(ourNextCode, action);
				intent.putExtra(RUNNABLE_KEY, ourNextCode);
				++ourNextCode;
			}
		}
		return intent;
	}

	private static final String AREA_KEY = "area";
	private static final String HOST_KEY = "host";
	private static final String RUNNABLE_KEY = "onSuccess";
	static final String SCHEME_KEY = "scheme";
	static final String USERNAME_KEY = "username";
	static final String PASSWORD_KEY = "password";
	static final String ERROR_KEY = "error";
	static final String CUSTOM_AUTH_KEY = "customAuth";

	static void initCredentialsCreator(Activity activity) {
		final ZLNetworkManager manager = ZLNetworkManager.Instance();
		if (manager.getCredentialsCreator() == null) {
			manager.setCredentialsCreator(new CredentialsCreator(activity));
		}
	}

	static class CredentialsCreator extends ZLNetworkManager.CredentialsCreator {
		private final Context myContext;

		CredentialsCreator(Activity activity) {
			myContext = activity.getApplicationContext();
		}

		@Override
		protected void startAuthenticationDialog(String host, String area, String scheme, String username) {
			final Intent intent = new Intent();
			intent.setClass(myContext, AuthenticationActivity.class);
			intent.putExtra(HOST_KEY, host);
			intent.putExtra(AREA_KEY, area);
			intent.putExtra(SCHEME_KEY, scheme);
			intent.putExtra(USERNAME_KEY, username);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			myContext.startActivity(intent);
		}
	}

	private ZLResource myResource;
	private INetworkLink myLink;
	private Button myOkButton;
	private Timer myOkButtonUpdater;
	private TextView myUsernameView;
	private boolean myCustomAuthentication;
	private Runnable myOnSuccessRunnable;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.authentication);

		final Intent intent = getIntent();

		final String host = intent.getStringExtra(HOST_KEY);
		final String area = intent.getStringExtra(AREA_KEY);
		final String username = intent.getStringExtra(USERNAME_KEY);
		final String error = intent.getStringExtra(ERROR_KEY);
		myCustomAuthentication = intent.getBooleanExtra(CUSTOM_AUTH_KEY, false);

		if (myCustomAuthentication) {
			myLink = Util.networkLibrary(this).getLinkByUrl(String.valueOf(intent.getData()));
			if (myLink == null) {
				finish();
				return;
			}
			setResult(RESULT_CANCELED, Util.intentByLink(new Intent(), myLink));
		} else {
			myLink = null;
			setResult(RESULT_CANCELED);
		}

		myOnSuccessRunnable = ourOnSuccessRunnableMap.remove(intent.getLongExtra(RUNNABLE_KEY, -1));

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

		myUsernameView = findTextView(R.id.authentication_username);
		myUsernameView.setText(username);

		setError(error);

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		final View buttonsView = findViewById(R.id.authentication_buttons);
		myOkButton = (Button)buttonsView.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				final String username = myUsernameView.getText().toString();
				final String password = findTextView(R.id.authentication_password).getText().toString();
				if (myCustomAuthentication) {
					runCustomAuthentication(username, password);
				} else {
					finishOk(username, password);
				}
			}
		});

		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (myLink != null) {
							final NetworkAuthenticationManager mgr = myLink.authenticationManager();
							if (mgr.mayBeAuthorised(false)) {
								mgr.logOut();
							}
						}
						final NetworkLibrary library = Util.networkLibrary(AuthenticationActivity.this);
						library.invalidateVisibility();
						library.synchronize();
					}
				});
				finish();
			}
		});
	}

	private void setError(String error) {
		final TextView errorView = findTextView(R.id.authentication_error);
		if (error != null && !"".equals(error)) {
			errorView.setVisibility(View.VISIBLE);
			errorView.setText(error);
			findTextView(R.id.authentication_password).setText("");
		} else {
			errorView.setVisibility(View.GONE);
		}
	}

	private void finishOk(String username, String password) {
		final ZLNetworkManager.CredentialsCreator creator =
			ZLNetworkManager.Instance().getCredentialsCreator();
		if (creator != null) {
			creator.setCredentials(username, password);
		}
		finish();
	}

	private void runCustomAuthentication(final String username, final String password) {
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				try {
					mgr.authorise(username, password);
					if (mgr.needsInitialization()) {
						mgr.initialize();
					}
					finishOk(username, password);
					if (myOnSuccessRunnable != null) {
						myOnSuccessRunnable.run();
					}
					final NetworkLibrary library = Util.networkLibrary(AuthenticationActivity.this);
					library.invalidateVisibility();
					library.synchronize();
				} catch (final ZLNetworkException e) {
					mgr.logOut();
					runOnUiThread(new Runnable() {
						public void run() {
							setError(e.getMessage());
						}
					});
				}
			}
		};
		UIUtil.wait("authentication", runnable, this);
	}

	private TextView findTextView(int resourceId) {
		return (TextView)findViewById(resourceId);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (myOkButtonUpdater == null) {
			myOkButtonUpdater = new Timer();
			myOkButtonUpdater.schedule(new TimerTask() {
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							myOkButton.setEnabled(myUsernameView.getText().length() > 0);
						}
					});
				}
			}, 0, 100);
		}
	}

	@Override
	protected void onPause() {
		if (myOkButtonUpdater != null) {
			myOkButtonUpdater.cancel();
			myOkButtonUpdater.purge();
			myOkButtonUpdater = null;
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		final ZLNetworkManager.CredentialsCreator creator =
			ZLNetworkManager.Instance().getCredentialsCreator();
		if (creator != null) {
			creator.release();
		}
		super.onStop();
	}
}
