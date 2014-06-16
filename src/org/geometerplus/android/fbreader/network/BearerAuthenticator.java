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

package org.geometerplus.android.fbreader.network;

import java.util.Calendar;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.android.fbreader.OrientationUtil;

public class BearerAuthenticator extends ZLNetworkManager.BearerAuthenticator {
	public static void initBearerAuthenticator(Activity activity) {
		ZLNetworkManager.Instance().setBearerAuthenticator(new BearerAuthenticator(activity));
	}

	static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		final BearerAuthenticator ba =
			(BearerAuthenticator)ZLNetworkManager.Instance().getBearerAuthenticator();
		switch (requestCode) {
			case NetworkLibraryActivity.REQUEST_ACCOUNT_PICKER:
				if (resultCode != NetworkLibraryActivity.RESULT_OK || data == null) {
					return true;
				}
				synchronized (ba) {
					ba.myAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					ba.notifyAll();
				}
				return true;
			case NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN:
				if (resultCode != NetworkLibraryActivity.RESULT_OK || data == null) {
					return true;
				}
				synchronized (ba) {
					final CookieStore store = ZLNetworkManager.Instance().cookieStore();
					final Map<String,String> cookies =
						(Map<String,String>)data.getSerializableExtra(NetworkLibraryActivity.COOKIES_KEY);
					if (cookies == null) {
						return true;
					}
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
					ba.notifyAll();
				}
				return true;
		}
		return false;
	}

	private final Activity myActivity;
	private volatile String myAccount;

	private BearerAuthenticator(Activity activity) {
		myActivity = activity;
	}

	@Override
	protected boolean authenticate(Map<String,String> params) {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(myActivity)
			== ConnectionResult.SUCCESS
			? authenticateToken(params)
			: authenticateWeb(params);
	}

	private boolean authenticateWeb(Map<String,String> params) {
		System.err.println("+++ WEB AUTH +++");
		final String authUrl = params.get("auth-url-web");
		final String completeUrl = params.get("complete-url-web");
		if (authUrl == null || completeUrl == null) {
			return false;
		}

		final Intent intent = new Intent(myActivity, WebAuthorisationScreen.class);
		intent.setData(Uri.parse(authUrl));
		intent.putExtra(NetworkLibraryActivity.COMPLETE_URL_KEY, completeUrl);
		OrientationUtil.startActivityForResult(
			myActivity, intent, NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN
		);
		startWaiting();
		System.err.println("--- WEB AUTH ---");
		return true;
	}

	private boolean authenticateToken(Map<String,String> params) {
		System.err.println("+++ TOKEN AUTH +++");
		try {
			final String authUrl = params.get("auth-url-token");
			final String clientId = params.get("client-id");
			if (authUrl == null || clientId == null) {
				return false;
			}

			final Intent intent = AccountManager.newChooseAccountIntent(
				null, null, new String[] { "com.google" }, false, null, null, null, null
			);
			myActivity.startActivityForResult(
				intent, NetworkLibraryActivity.REQUEST_ACCOUNT_PICKER
			);
			startWaiting();
			final String authToken = GoogleAuthUtil.getToken(
				myActivity, myAccount, String.format("audience:server:client_id:%s", clientId)
			);
			ZLNetworkManager.Instance().perform(new ZLNetworkRequest(authUrl, authToken, true) {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("--- TOKEN AUTH ---");
		return true;
	}

	private void startWaiting() {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
//					String accessToken = null;
//					try {
//						//accessToken = GoogleAuthUtil.getToken(myActivity, "geometer@fbreader.org", "oauth2:server:client_id:420992307317-f6a2v16e96rfeargv2avtr4otfa7dmc5.apps.googleusercontent.com:api_scope:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/drive", null);
//						accessToken = GoogleAuthUtil.getToken(myActivity, "geometer@fbreader.org", "oauth2:server:client_id:420992307317-i60n5g5pa7a56caung72gahebnpk2ktv.apps.googleusercontent.com:api_scope:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/drive", null);
//					} catch (UserRecoverableAuthException e) {
//						myActivity.startActivity(e.getIntent());
//					}
					//System.err.println("ACCESS TOKEN = " + accessToken);
				/*
				*/
