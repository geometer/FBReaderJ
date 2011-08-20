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

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;
import org.geometerplus.android.fbreader.network.NetworkCatalogActions;

public class SignOutAction extends CatalogAction {
	private final NetworkLibraryActivity myActivity;

	public SignOutAction(NetworkLibraryActivity activity) {
		super(ActionCode.SIGNOUT, "signOut");
		myActivity = activity;
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!super.isVisible(tree)) {
			return false;
		}

		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		final NetworkAuthenticationManager mgr = item.Link.authenticationManager();
		return mgr != null && mgr.mayBeAuthorised(false);
	}

	@Override
	public void run(NetworkTree tree) {
		NetworkCatalogActions.doSignOut(myActivity, (NetworkCatalogTree)tree);
	}

	@Override
	public String getLabel(NetworkTree tree) {
		final NetworkAuthenticationManager mgr =
			(((NetworkCatalogTree)tree).Item).Link.authenticationManager();
		final String userName =
			mgr != null && mgr.mayBeAuthorised(false) ? mgr.currentUserName() : "";
		return super.getLabel(tree).replace("%s", userName);
	}
}
