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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;

public class ListenerCallback extends BroadcastReceiver implements UserRegistrationConstants {
	@Override
	public void onReceive(Context context, final Intent intent) {
		final NetworkLibrary library = Util.networkLibrary(context);

		if (Util.SIGNIN_ACTION.equals(intent.getAction())) {
			final String url = intent.getStringExtra(CATALOG_URL);
			final INetworkLink link = library.getLinkByUrl(url);
			if (link != null) {
				final NetworkAuthenticationManager mgr = link.authenticationManager();
				if (mgr instanceof LitResAuthenticationManager) {
					new Thread(new Runnable() {
						public void run() {
							try {
								processSignup((LitResAuthenticationManager)mgr, intent);
							} catch (ZLNetworkException e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			}
		} else {
			library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		}
	}

	private static void processSignup(LitResAuthenticationManager mgr, Intent data) throws ZLNetworkException {
		mgr.initUser(
			data.getStringExtra(USER_REGISTRATION_USERNAME),
			data.getStringExtra(USER_REGISTRATION_LITRES_SID),
			"",
			false
		);
		//if (!mgr.isAuthorised(true)) {
		//	throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
		//}
		try {
			mgr.authorise(
				data.getStringExtra(USER_REGISTRATION_USERNAME),
				data.getStringExtra(USER_REGISTRATION_PASSWORD)
			);
			mgr.initialize();
		} catch (ZLNetworkException e) {
			mgr.logOut();
			throw e;
		}
	}
}
