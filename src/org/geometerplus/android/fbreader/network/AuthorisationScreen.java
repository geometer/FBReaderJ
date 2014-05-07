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
import android.os.Bundle;
import android.webkit.WebView;

import org.geometerplus.android.fbreader.OrientationUtil;

public class AuthorisationScreen extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		OrientationUtil.setOrientation(this, getIntent());
		final WebView view = new WebView(this);
		view.getSettings().setJavaScriptEnabled(true);
		setContentView(view);
		System.err.println("URL 1: " + getIntent().getData().toString());
		view.loadUrl(getIntent().getData().toString());
	}
}
