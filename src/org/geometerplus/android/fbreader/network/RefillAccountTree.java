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

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibraryItem;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

class RefillAccountTree extends NetworkTree implements ZLAndroidTree {
	public final INetworkLink Link;

	public RefillAccountTree(NetworkCatalogTree parentTree) {
		super(parentTree.Level + 1);
		Link = parentTree.Item.Link;
	}

	public RefillAccountTree(INetworkLink link) {
		super(1);
		Link = link;
	}

	@Override
	public String getName() {
		return ZLResource.resource("networkView").getResource("refillTitle").getValue();
	}

	@Override
	public String getSummary() {
		final NetworkAuthenticationManager mgr = Link.authenticationManager();
		try {
			if (mgr.isAuthorised(false)) {
				final String account = mgr.currentAccount();
				if (account != null) {
					return ZLResource.resource("networkView").getResource("refillSummary").getValue().replace("%s", account);
				}
			}
		} catch (ZLNetworkException e) {
		}
		return null;
	}

	@Override
	public NetworkLibraryItem getHoldedItem() {
		return null;
	}

	public int getCoverResourceId() {
		return R.drawable.ic_list_library_wallet;
	}
}
