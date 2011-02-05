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

package org.geometerplus.fbreader.network.authentication.litres;

import java.util.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

class SortedCatalogItem extends NetworkCatalogItem {
	private final List<NetworkLibraryItem> myChildren = new LinkedList<NetworkLibraryItem>();

	private SortedCatalogItem(NetworkCatalogItem parent, ZLResource resource, List<NetworkLibraryItem> children) {
		super(parent.Link, resource.getValue(), resource.getResource("summary").getValue(), "", parent.URLByType);
		myChildren.addAll(children);
	}

	public SortedCatalogItem(NetworkCatalogItem parent, String resourceKey, List<NetworkLibraryItem> children) {
		this(parent, ZLResource.resource("networkView").getResource(resourceKey), children);
	}

	@Override
	public void onDisplayItem() {
	}

	@Override
	public void loadChildren(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		for (NetworkLibraryItem child : myChildren) {
			listener.onNewItem(Link, child);
		}
		listener.commitItems(Link);
	}
}

public class LitResBookshelfItem extends NetworkCatalogItem {
	private boolean myForceReload;

	public LitResBookshelfItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, cover, urlByType, visibility, CATALOG_OTHER);
	}

	@Override
	public void onDisplayItem() {
		myForceReload = false;
	}

	@Override
	public void loadChildren(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		final LitResAuthenticationManager mgr = (LitResAuthenticationManager) Link.authenticationManager();

		// TODO: Maybe it's better to call isAuthorised(true) directly 
		// and let exception fly through???
		if (!mgr.mayBeAuthorised(true)) {
			throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
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
			if (children.size() <= 5) {
				Collections.sort(children, new NetworkBookItemComparator());
				for (NetworkLibraryItem item : children) {
					listener.onNewItem(Link, item);
				}
			} else {
				listener.onNewItem(Link, new SortedCatalogItem(this, "byDate", children));
				listener.onNewItem(Link, new SortedCatalogItem(this, "byAuthor", children));
				listener.onNewItem(Link, new SortedCatalogItem(this, "byTitle", children));
			}
			listener.commitItems(Link);
		}
	}
}
