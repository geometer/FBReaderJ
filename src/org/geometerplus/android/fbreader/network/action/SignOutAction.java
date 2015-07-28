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

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.sync.SyncUtil;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;

import org.geometerplus.android.util.UIUtil;

public class SignOutAction extends Action {
	private final ZLNetworkContext myNetworkContext;

	public SignOutAction(Activity activity, ZLNetworkContext context) {
		super(activity, ActionCode.SIGNOUT, "signOut", -1);
		myNetworkContext = context;
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!(tree instanceof NetworkCatalogRootTree)) {
			return false;
		}

		final INetworkLink link = tree.getLink();
		if (link instanceof ISyncNetworkLink) {
			return ((ISyncNetworkLink)link).isLoggedIn(myNetworkContext);
		}

		final NetworkAuthenticationManager mgr = link.authenticationManager();
		return mgr != null && mgr.mayBeAuthorised(false);
	}

	@Override
	public void run(NetworkTree tree) {
		final INetworkLink link = tree.getLink();
		if (link instanceof ISyncNetworkLink) {
			((ISyncNetworkLink)link).logout(myNetworkContext);
			((NetworkCatalogRootTree)tree).clearCatalog();
			return;
		}

		final NetworkAuthenticationManager mgr = link.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				if (mgr.mayBeAuthorised(false)) {
					mgr.logOut();
					myActivity.runOnUiThread(new Runnable() {
						public void run() {
							myLibrary.invalidateVisibility();
							myLibrary.synchronize();
						}
					});
				}
			}
		};
		UIUtil.wait("signOut", runnable, myActivity);
	}

	private String accountName(NetworkTree tree) {
		final INetworkLink link = tree.getLink();
		if (link instanceof ISyncNetworkLink) {
			return SyncUtil.getAccountName(myNetworkContext);
		}

		final NetworkAuthenticationManager mgr = link.authenticationManager();
		return mgr != null && mgr.mayBeAuthorised(false) ? mgr.getVisibleUserName() : null;
	}

	@Override
	public String getOptionsLabel(NetworkTree tree) {
		final String account = accountName(tree);
		return super.getOptionsLabel(tree).replace("%s", account != null ? account : "");
	}

	@Override
	public String getContextLabel(NetworkTree tree) {
		final String account = accountName(tree);
		return super.getContextLabel(tree).replace("%s", account != null ? account : "");
	}
}
