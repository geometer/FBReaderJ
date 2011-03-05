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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.TopUpItem;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

public class TopUpTree extends NetworkTree {
	public final TopUpItem Item;

	TopUpTree(NetworkCatalogTree parentTree, TopUpItem item) {
		super(parentTree);
		Item = item;
	}

	@Override
	public String getName() {
		return Item.Title;
	}

	@Override
	public String getSummary() {
		final NetworkAuthenticationManager mgr = Item.Link.authenticationManager();
		try {
			if (mgr.isAuthorised(false)) {
				final String account = mgr.currentAccount();
				if (account != null) {
					return Item.Summary.replace("%s", account);
				}
			}
		} catch (ZLNetworkException e) {
		}
		return null;
	}

	@Override
	protected ZLImage createCover() {
		return createCover(Item);
	}

	@Override
	public TopUpItem getHoldedItem() {
		return Item;
	}

	@Override
	protected String getStringId() {
		return "@TopUp Account";
	}
}
