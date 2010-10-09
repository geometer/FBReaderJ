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

package org.geometerplus.fbreader.network.authentication.litres;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

public class LitResBookshelfItem extends NetworkCatalogItem {

	private boolean myForceReload;


	public LitResBookshelfItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType) {
		super(link, title, summary, cover, urlByType);
	}

	public LitResBookshelfItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, cover, urlByType, visibility);
	}

	public LitResBookshelfItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility, int catalogType) {
		super(link, title, summary, cover, urlByType, visibility, catalogType);
	}

	@Override
	public void onDisplayItem() {
		myForceReload = false;
	}

	@Override
	public void loadChildren(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		LitResAuthenticationManager mgr = (LitResAuthenticationManager) Link.authenticationManager();
		if (mgr.isAuthorised(true).Status == ZLBoolean3.B3_FALSE) {
			throw new ZLNetworkException(NetworkErrors.ERROR_AUTHENTICATION_FAILED);
		}
		try {
			if (myForceReload) {
				mgr.reloadPurchasedBooks();
			}
		} finally {
			myForceReload = true;
			// TODO: implement asynchronous loading
			LinkedList<NetworkLibraryItem> children = new LinkedList<NetworkLibraryItem>();
			mgr.collectPurchasedBooks(children);
			Collections.sort(children, new NetworkBookItemComparator());
			for (NetworkLibraryItem item: children) {
				listener.onNewItem(Link, item);
			}
			listener.commitItems(Link);
		}
	}
}
