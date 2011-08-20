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

package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.network.NetworkCatalogActions;

public class SignOutAction extends Action {
	public SignOutAction(Activity activity) {
		super(activity, ActionCode.SIGNOUT, "signOut", -1);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!(tree instanceof NetworkCatalogRootTree)) {
			return false;
		}

		final NetworkCatalogItem item = ((NetworkCatalogRootTree)tree).Item;
		final NetworkAuthenticationManager mgr = item.Link.authenticationManager();
		return mgr != null && mgr.mayBeAuthorised(false);
	}

	@Override
	public void run(NetworkTree tree) {
		NetworkCatalogActions.doSignOut(myActivity, (NetworkCatalogRootTree)tree);
	}

	@Override
	public String getOptionsLabel(NetworkTree tree) {
		final NetworkAuthenticationManager mgr =
			(((NetworkCatalogRootTree)tree).Item).Link.authenticationManager();
		final String userName =
			mgr != null && mgr.mayBeAuthorised(false) ? mgr.currentUserName() : "";
		return super.getOptionsLabel(tree).replace("%s", userName);
	}

	@Override
	public String getContextLabel(NetworkTree tree) {
		final NetworkAuthenticationManager mgr =
			(((NetworkCatalogRootTree)tree).Item).Link.authenticationManager();
		final String userName =
			mgr != null && mgr.mayBeAuthorised(false) ? mgr.currentUserName() : "";
		return super.getContextLabel(tree).replace("%s", userName);
	}
}
