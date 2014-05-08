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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.webkit.*;

import org.geometerplus.android.fbreader.OrientationUtil;

public class AuthorisationScreen extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		CookieSyncManager.createInstance(getApplicationContext());

		requestWindowFeature(Window.FEATURE_PROGRESS);
		OrientationUtil.setOrientation(this, getIntent());
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
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				final String cookies = CookieManager.getInstance().getCookie(url);
				System.err.println("PAGE = " + url);
				System.err.println("COOKIES = " + cookies);
				AuthorisationScreen.this.setResult(RESULT_OK, new Intent().putExtra(
					NetworkLibraryActivity.COOKIES_KEY, cookies
				));
				finish();
			}
		});
		setContentView(view);
		view.loadUrl(getIntent().getDataString());
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}
}
