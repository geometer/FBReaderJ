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

import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.tree.TopUpTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.network.TopupMenuActivity;

public class TopupAction extends Action {
	public TopupAction(Activity activity) {
		super(activity, ActionCode.TOPUP, "topup", -1);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (tree instanceof TopUpTree) {
			return true;
		} else if (tree instanceof NetworkCatalogRootTree) {
			final NetworkCatalogItem item = ((NetworkCatalogRootTree)tree).Item;
			final NetworkAuthenticationManager mgr = item.Link.authenticationManager();
			return
				mgr != null &&
				mgr.mayBeAuthorised(false) &&
				mgr.currentAccount() != null &&
				TopupMenuActivity.isTopupSupported(item.Link);
		} else {
			return false;
		}
	}

	@Override
	protected void run(NetworkTree tree) {
		INetworkLink link = null;
		if (tree instanceof TopUpTree) {
			link = ((TopUpTree)tree).Item.Link;
		} else if (tree instanceof NetworkCatalogRootTree) {
			link = ((NetworkCatalogRootTree)tree).Item.Link;
		}
		if (link != null) {
			TopupMenuActivity.runMenu(myActivity, link, null);
		}
	}

	@Override
	public String getContextLabel(NetworkTree tree) {
		INetworkLink link = null;
		if (tree instanceof TopUpTree) {
			link = ((TopUpTree)tree).Item.Link;
		} else if (tree instanceof NetworkCatalogRootTree) {
			link = ((NetworkCatalogRootTree)tree).Item.Link;
		}
		Money account = null;
		if (link != null) {
			final NetworkAuthenticationManager mgr = link.authenticationManager();
			if (mgr != null && mgr.mayBeAuthorised(false)) {
				account = mgr.currentAccount();
			}
		}
		return super.getContextLabel(tree).replace("%s", account != null ? account.toString() : "");
	}
}
