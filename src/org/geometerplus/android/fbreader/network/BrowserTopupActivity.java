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
import android.os.Bundle;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.INetworkLink;

public class BrowserTopupActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		String url = getIntent().getData().toString();
		if (url.endsWith("/browser")) {
			url = url.substring(0, url.length() - "/browser".length());
			final INetworkLink link = NetworkLibrary.Instance().getLinkByUrl(url);
			Util.openInBrowser(this, link.authenticationManager().topupLink());
		}
		finish();
	}
}
