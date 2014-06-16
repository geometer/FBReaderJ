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

import android.content.Intent;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

public class BearerAuthenticator {
	static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case NetworkLibraryActivity.REQUEST_WEB_AUTHORISATION_SCREEN:
				if (resultCode == NetworkLibraryActivity.RESULT_OK) {
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
				}
				return true;
		}
		return false;
	}
}
