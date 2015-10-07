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

package org.geometerplus.fbreader.network;

import java.util.*;

import org.fbreader.util.Boolean3;

import org.geometerplus.zlibrary.core.network.*;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public abstract class NetworkCatalogItem extends NetworkItem {
	// bit mask for flags parameter
	public static final int FLAG_SHOW_AUTHOR                              = 1 << 0;
	public static final int FLAG_GROUP_BY_AUTHOR                          = 1 << 1;
	public static final int FLAG_GROUP_BY_SERIES                          = 1 << 2;
	public static final int FLAG_GROUP_MORE_THAN_1_BOOK_BY_SERIES         = 1 << 3;
	public static final int FLAG_ADD_SEARCH_ITEM                          = 1 << 4;

	public static final int FLAGS_DEFAULT =
		FLAG_SHOW_AUTHOR |
		FLAG_GROUP_MORE_THAN_1_BOOK_BY_SERIES;
	public static final int FLAGS_GROUP =
		FLAG_GROUP_BY_AUTHOR |
		FLAG_GROUP_BY_SERIES |
		FLAG_GROUP_MORE_THAN_1_BOOK_BY_SERIES;

	// catalog accessibility types:
	public static enum Accessibility {
		NEVER,
		ALWAYS,
		SIGNED_IN,
		HAS_BOOKS
	}

	private final Accessibility myAccessibility;
	private int myFlags;

	public boolean UpdatingInProgress;

	/**
	 * Creates new NetworkCatalogItem instance with specified accessibility and type.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title         title of this library item. Must be not <code>null</code>.
	 * @param summary       description of this library item. Can be <code>null</code>.
	 * @param urls          collection of item-related URLs. Can be <code>null</code>.
	 * @param accessibility value defines when this library item will be accessible
	 *                      in the network library view.
	 * @param flags         describes how to show book items inside this catalog
	 */
	public NetworkCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags) {
		super(link, title, summary, urls);
		myAccessibility = accessibility;
		myFlags = flags;
	}

	public Map<String,String> extraData() {
		return Collections.emptyMap();
	}

	public abstract boolean canBeOpened();

	public abstract void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException;

	public boolean supportsResumeLoading() {
		return false;
	}

	public boolean canResumeLoading() {
		return false;
	}

	public void resumeLoading(NetworkItemsLoader loader) throws ZLNetworkException {
	}

	public int getFlags() {
		return myFlags;
	}

	public void setFlags(int flags) {
		myFlags = flags;
	}

	public Boolean3 getVisibility() {
		if (Link == null) {
			return Boolean3.TRUE;
		}

		final NetworkAuthenticationManager mgr = Link.authenticationManager();
		switch (myAccessibility) {
			default:
				return Boolean3.FALSE;
			case ALWAYS:
				return Boolean3.TRUE;
			case HAS_BOOKS:
				if (Link.getBasketItem() != null && Link.getBasketItem().bookIds().size() > 0) {
					return Boolean3.TRUE;
				}
				// go through!
			case SIGNED_IN:
				if (mgr == null) {
					return Boolean3.FALSE;
				}
				try {
					return mgr.isAuthorised(false) ?  Boolean3.TRUE : Boolean3.UNDEFINED;
				} catch (ZLNetworkException e) {
					return Boolean3.UNDEFINED;
				}
		}
	}

	public abstract String getStringId();

	/**
	 * Performs all necessary operations with NetworkOperationData and NetworkRequest
	 * to complete loading children items.
	 *
	 * @param data Network operation data instance
	 * @param networkRequest initial network request
	 *
	 * @throws ZLNetworkException when network operation couldn't be completed
	 */
	protected final void doLoadChildren(NetworkOperationData data, ZLNetworkRequest networkRequest) throws ZLNetworkException {
		if (networkRequest != null) {
			data.Loader.NetworkContext.perform(networkRequest);
			data.Loader.confirmInterruption();
		}
	}
}
