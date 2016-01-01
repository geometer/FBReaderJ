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

package org.geometerplus.android.fbreader.network.auth;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Window;
import android.webkit.*;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.android.util.OrientationUtil;

public class WebAuthorisationScreen extends Activity {
	public static final String COMPLETE_URL_KEY = "android.fbreader.data.complete.url";

	private final ActivityNetworkContext myNetworkContext = new ActivityNetworkContext(this);

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		SQLiteCookieDatabase.init(this);
		CookieSyncManager.createInstance(getApplicationContext());
		CookieManager.getInstance().removeAllCookie();
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null || data.getHost() == null) {
			finish();
			return;
		}
		final String completeUrl = intent.getStringExtra(COMPLETE_URL_KEY);

		OrientationUtil.setOrientation(this, intent);
		final WebView view = new WebView(this);
		view.getSettings().setJavaScriptEnabled(true);

		view.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				setProgress(progress * 100);
			}
		});
		view.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				setTitle(url);
				if (url != null && url.startsWith(completeUrl)) {
					final HashMap<String,String> cookies = new HashMap<String,String>();
					final String cookieString = CookieManager.getInstance().getCookie(url);
					if (cookieString != null) {
						// cookieString is a string like NAME=VALUE [; NAME=VALUE]
						for (String pair : cookieString.split(";")) {
							final String[] parts = pair.split("=", 2);
							if (parts.length != 2) {
								continue;
							}
							cookies.put(parts[0].trim(), parts[1].trim());
						}
					}
					storeCookies(data.getHost(), cookies);
					WebAuthorisationScreen.this.setResult(RESULT_OK);
					finish();
				}
			}

			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.ECLAIR_MR1) {
					// hack for auth problem in android 2.1
					handler.proceed();
				} else {
					super.onReceivedSslError(view, handler, error);
				}
			}
		});
		setContentView(view);
		view.loadUrl(intent.getDataString());
	}

	private void storeCookies(String host, Map<String,String> cookies) {
		final ZLNetworkManager.CookieStore store = myNetworkContext.cookieStore();

		for (Map.Entry<String,String> entry : cookies.entrySet()) {
			final BasicClientCookie2 c =
				new BasicClientCookie2(entry.getKey(), entry.getValue());
			c.setDomain(host);
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
