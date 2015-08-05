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

import android.app.Activity;
import android.content.*;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.network.litres.AutoRegistrationActivity;
import org.geometerplus.android.fbreader.network.litres.UserRegistrationActivity;

public class AuthorisationMenuActivity extends MenuActivity {
	public static void runMenu(Context context, INetworkLink link) {
		context.startActivity(
			Util.intentByLink(new Intent(context, AuthorisationMenuActivity.class), link)
		);
	}

	public static void runMenu(Activity activity, INetworkLink link, int code) {
		activity.startActivityForResult(
			Util.intentByLink(new Intent(activity, AuthorisationMenuActivity.class), link), code
		);
	}

	private INetworkLink myLink;

	@Override
	protected void init() {
		final String baseUrl = getIntent().getData().toString();
		final ZLResource resource = NetworkLibrary.resource();

		setTitle(resource.getResource("authorisationMenuTitle").getValue());
		myLink = Util.networkLibrary(this).getLinkByUrl(baseUrl);

		if (myLink.getUrlInfo(UrlInfo.Type.SignIn) != null) {
			myInfos.add(new PluginApi.MenuActionInfo(
				Uri.parse(baseUrl + "/signIn"),
				resource.getResource("signIn").getValue(),
				0
			));
			if (myLink.authenticationManager() != null) {
				myInfos.add(new PluginApi.MenuActionInfo(
					Uri.parse(baseUrl + "/signUp"),
					resource.getResource("signUp").getValue(),
					1
				));
				myInfos.add(new PluginApi.MenuActionInfo(
					Uri.parse(baseUrl + "/quickBuy"),
					resource.getResource("quickBuy").getValue(),
					2
				));
			}
		}
	}

	@Override
	protected String getAction() {
		return Util.AUTHORISATION_ACTION;
	}

	@Override
	protected void runItem(final PluginApi.MenuActionInfo info) {
		try {
			final NetworkAuthenticationManager mgr = myLink.authenticationManager();
			if (info.getId().toString().endsWith("/signIn")) {
				Util.runAuthenticationDialog(AuthorisationMenuActivity.this, myLink, null);
			} else if (info.getId().toString().endsWith("/signUp")) {
				startActivity(Util.authorisationIntent(myLink, this, UserRegistrationActivity.class));
			} else if (info.getId().toString().endsWith("/quickBuy")) {
				startActivity(Util.authorisationIntent(myLink, this, AutoRegistrationActivity.class));
			}
		} catch (Exception e) {
			// do nothing
		}
	}
}
