/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

abstract class Util implements UserRegistrationConstants {
	private static final String REGISTRATION_ACTION =
		"android.fbreader.action.NETWORK_LIBRARY_REGISTER";
	private static final String SMS_REFILLING_ACTION =
		"android.fbreader.action.NETWORK_LIBRARY_SMS_REFILLING";

	private static boolean testService(Activity activity, String action, String url) {
		if (url == null) {
			return false;
		}
		try {
			activity.startActivity(new Intent(action + "_TEST", Uri.parse(url)));
			return true;
		} catch (ActivityNotFoundException e) {
			return false;
		}
	}

	static boolean isRegistrationSupported(Activity activity, INetworkLink link) {
		return testService(
			activity,
			REGISTRATION_ACTION,
			link.getLink(INetworkLink.URL_SIGN_UP)
		);
	}

	static void runRegistrationDialog(Activity activity, INetworkLink link) {
		try {
			activity.startActivityForResult(new Intent(
				REGISTRATION_ACTION,
				Uri.parse(link.getLink(INetworkLink.URL_SIGN_UP))
			), USER_REGISTRATION_REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
		}
	}

	static void runAfterRegistration(NetworkAuthenticationManager mgr, Intent data) throws ZLNetworkException {
		final String userName = data.getStringExtra(USER_REGISTRATION_USERNAME);
		final String litresSid = data.getStringExtra(USER_REGISTRATION_LITRES_SID);
		mgr.initUser(userName, litresSid);
		if (userName.length() > 0 && litresSid.length() > 0) {
			try {
				mgr.initialize();
			} catch (ZLNetworkException e) {
				mgr.logOut();
				throw e;
			}
		}
	}

	static boolean isAccountRefillingSupported(Activity activity, INetworkLink link) {
		return
			isBrowserAccountRefillingSupported(activity, link) ||
			isSmsAccountRefillingSupported(activity, link);
	}

	static boolean isSmsAccountRefillingSupported(Activity activity, INetworkLink link) {
		return testService(
			activity,
			SMS_REFILLING_ACTION,
			link.getLink(INetworkLink.URL_MAIN)
		);
	}

	static boolean isBrowserAccountRefillingSupported(Activity activity, INetworkLink link) {
		return link.getLink(INetworkLink.URL_REFILL_ACCOUNT) != null;
	}
}
