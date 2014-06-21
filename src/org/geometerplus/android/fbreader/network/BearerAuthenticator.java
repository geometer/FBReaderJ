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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.android.fbreader.OrientationUtil;

public class BearerAuthenticator extends ZLNetworkManager.BearerAuthenticator {
	private static Map<Activity,BearerAuthenticator> ourAuthenticators =
		Collections.synchronizedMap(new HashMap<Activity,BearerAuthenticator>());

	public static void initBearerAuthenticator(Activity activity) {
		synchronized (ourAuthenticators) {
			BearerAuthenticator ba = ourAuthenticators.get(activity);
			if (ba == null) {
				ba = new BearerAuthenticator(activity);
				ourAuthenticators.put(activity, ba);
			}
			ZLNetworkManager.Instance().setBearerAuthenticator(ba);
		}
	}

	static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		final BearerAuthenticator ba = ourAuthenticators.get(activity);
		boolean processed = true;
		try {
			switch (requestCode) {
				default:
					processed = false;
					break;
				case NetworkLibraryActivity.REQUEST_ACCOUNT_PICKER:
					if (resultCode == Activity.RESULT_OK && data != null) {
						ba.myAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					}
					break;
				case NetworkLibraryActivity.REQUEST_AUTHORISATION:
					if (resultCode == Activity.RESULT_OK) {
						ba.myAuthorizationConfirmed = true;
					}
					break;
				case NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN:
					if (resultCode == Activity.RESULT_OK && data != null) {
						final CookieStore store = ZLNetworkManager.Instance().cookieStore();
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
				synchronized (ba) {
					ba.notifyAll();
				}
			}
			return processed;
		}
	}

	private final Activity myActivity;
	private volatile String myAccount;
	private volatile boolean myAuthorizationConfirmed;

	private BearerAuthenticator(Activity activity) {
		myActivity = activity;
	}

	@Override
	protected boolean authenticate(URI uri, Map<String,String> params) {
		System.err.println("AUTHENTICATE FOR " + uri);
		if (!"https".equalsIgnoreCase(uri.getScheme())) {
			return false;
		}
		return authenticateWeb(uri, params);
	}

	private String url(URI base, Map<String,String> params, String key) {
		final String path = params.get(key);
		if (path == null) {
			return null;
		}
		try {
			final URI relative = new URI(path);
			return relative.isAbsolute() ? null : base.resolve(relative).toString();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	private boolean authenticateWeb(URI uri, Map<String,String> params) {
		System.err.println("+++ WEB AUTH +++");
		final String authUrl = url(uri, params, "auth-url-web");
		final String completeUrl = url(uri, params, "complete-url-web");
		if (authUrl == null || completeUrl == null) {
			return false;
		}

		final Intent intent = new Intent(myActivity, WebAuthorisationScreen.class);
		intent.setData(Uri.parse(authUrl));
		intent.putExtra(NetworkLibraryActivity.COMPLETE_URL_KEY, completeUrl);
		startActivityAndWait(intent, NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN);
		System.err.println("--- WEB AUTH ---");
		return true;
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
