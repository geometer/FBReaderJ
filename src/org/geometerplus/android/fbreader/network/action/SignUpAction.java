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

package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.network.Util;
import org.geometerplus.android.fbreader.network.litres.UserRegistrationActivity;

public class SignUpAction extends Action {
	public SignUpAction(Activity activity) {
		super(activity, ActionCode.SIGNUP, "signUp", -1);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!(tree instanceof NetworkCatalogRootTree)) {
			return false;
		}

		final NetworkAuthenticationManager mgr = tree.getLink().authenticationManager();
		return mgr != null && !mgr.mayBeAuthorised(false);
	}

	@Override
	public void run(NetworkTree tree) {
		final INetworkLink link = tree.getLink();
		try {
			myActivity.startActivity(Util.authorisationIntent(
				link, myActivity, UserRegistrationActivity.class
			));
		} catch (ActivityNotFoundException e) {
		}
	}
}
