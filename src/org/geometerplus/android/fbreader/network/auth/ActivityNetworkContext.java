/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.network.auth;

import java.net.URI;
import java.util.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.*;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;

public final class ActivityNetworkContext extends AndroidNetworkContext {
	private final Activity myActivity;
	private volatile boolean myAuthorizationConfirmed;

	private volatile String myAccountName;

	public ActivityNetworkContext(Activity activity) {
		myActivity = activity;
	}

	public Context getContext() {
		return myActivity;
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean processed = true;
		try {
			switch (requestCode) {
				default:
					processed = false;
					break;
				case NetworkLibraryActivity.REQUEST_ACCOUNT_PICKER:
					if (resultCode == Activity.RESULT_OK && data != null) {
						myAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					} else {
						myAccountName = null;
					}
					break;
				case NetworkLibraryActivity.REQUEST_AUTHORISATION:
					if (resultCode == Activity.RESULT_OK) {
						myAuthorizationConfirmed = true;
					}
					break;
				case NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN:
					if (resultCode == Activity.RESULT_OK && data != null) {
						final ZLNetworkManager.CookieStore store = cookieStore();
						final Map<String,String> cookies =
							(Map<String,String>)data.getSerializableExtra(NetworkLibraryActivity.COOKIES_KEY);
						if (cookies != null) {
							for (Map.Entry<String,String> entry : cookies.entrySet()) {
								final BasicClientCookie2 c =
									new BasicClientCookie2(entry.getKey(), entry.getValue());
								c.setDomain(data.getData().getHost());
								c.setPath("/");
								final Calendar expire = Calendar.getInstance();
								expire.add(Calendar.YEAR, 1);
								c.setExpiryDate(expire.getTime());
								c.setSecure(true);
								c.setDiscard(false);
								store.addCookie(c);
							}
						}
					}
					break;
			}
		} finally {
			if (processed) {
				synchronized (this) {
					notifyAll();
				}
			}
			return processed;
		}
	}

	@Override
	protected Map<String,String> authenticateWeb(URI uri, String realm, Map<String,String> params) {
		System.err.println("+++ WEB AUTH +++");
		final String account = getAccountName(uri.getHost(), realm);
		String authUrl = url(uri, params, "auth-url-web");
		if (account != null) {
			final String urlWithAccount = params.get("auth-url-web-with-email");
			if (urlWithAccount != null) {
				authUrl = url(uri, urlWithAccount.replace("{email}", account));
			}
		}
		final String completeUrl = url(uri, params, "complete-url-web");
		final String verificationUrl = url(uri, params, "verification-url");
		if (authUrl == null || completeUrl == null || verificationUrl == null) {
			return errorMap("No data for web authentication");
		}

		final Intent intent = new Intent(myActivity, WebAuthorisationScreen.class);
		intent.setData(Uri.parse(authUrl));
		intent.putExtra(NetworkLibraryActivity.COMPLETE_URL_KEY, completeUrl);
		startActivityAndWait(intent, NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN);
		System.err.println("--- WEB AUTH ---");
		return verify(verificationUrl);
	}

	private Map<String,String> registerAccessToken(String account, String clientId, String authUrl, String authToken) {
		String code = null;
		try {
			code = GoogleAuthUtil.getToken(myActivity, account, String.format(
				"oauth2:server:client_id:%s:api_scope:%s", clientId,
				TextUtils.join(" ", new Object[] { Scopes.DRIVE_FILE, Scopes.PROFILE })
			), null);
			return runTokenAuthorization(authUrl, authToken, code);
		} catch (UserRecoverableAuthException e) {
			myAuthorizationConfirmed = false;
			startActivityAndWait(e.getIntent(), NetworkLibraryActivity.REQUEST_AUTHORISATION);
			if (myAuthorizationConfirmed) {
				return registerAccessToken(account, clientId, authUrl, authToken);
			} else {
				return errorMap("Authorization failed");
			}
		} catch (Exception e) {
			return errorMap(e);
		}
	}

	@Override
	protected Map<String,String> authenticateToken(URI uri, String realm, String authUrl, String clientId) {
		System.err.println("+++ TOKEN AUTH +++");
		try {
			String account = getAccountName(uri.getHost(), realm);
			if (account == null) {
				final Intent intent = AccountManager.newChooseAccountIntent(
					null, null, new String[] { "com.google" }, false, null, null, null, null
				);
				startActivityAndWait(intent, NetworkLibraryActivity.REQUEST_ACCOUNT_PICKER);
				account = myAccountName;
			}
			if (account == null) {
				return errorMap("No selected account");
			}
			final String authToken = GoogleAuthUtil.getToken(
				myActivity, account, String.format("audience:server:client_id:%s", clientId)
			);
			final Map<String,String> result = runTokenAuthorization(authUrl, authToken, null);
			if (result.containsKey("user")) {
				return result;
			}
			return registerAccessToken(account, clientId, authUrl, authToken);
		} catch (Exception e) {
			return errorMap(e);
		} finally {
			System.err.println("--- TOKEN AUTH ---");
		}
	}

	private void startActivityAndWait(Intent intent, int requestCode) {
		synchronized (this) {
			OrientationUtil.startActivityForResult(myActivity, intent, requestCode);
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
