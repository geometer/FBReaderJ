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

package org.geometerplus.fbreader.network;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

public abstract class NetworkCatalogItem extends NetworkLibraryItem {

	// catalog types:
	public static enum CatalogType {
		OTHER,
		BY_AUTHOR,
		BY_SERIES
	}

	// catalog accessibility types:
	public static enum Accessibility {
		NEVER,
		ALWAYS,
		SIGNED_IN,
		HAS_BOOKS
	}

	// URL type values:
	public static final int URL_NONE = 0;
	public static final int URL_CATALOG = 1;
	public static final int URL_HTML_PAGE = 2;

	private final Accessibility myAccessibility;
	private final CatalogType myCatalogType;
	public final TreeMap<Integer, String> URLByType;

	/**
	 * Creates new NetworkCatalogItem instance with <code>Accessibility.ALWAYS</code> accessibility and <code>CatalogType.OTHER</code> type.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param cover      cover url. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 */
	public NetworkCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer,String> urlByType) {
		this(link, title, summary, cover, urlByType, Accessibility.ALWAYS, CatalogType.OTHER);
	}

	/**
	 * Creates new NetworkCatalogItem instance with specified accessibility and <code>CatalogType.OTHER</code> type.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title         title of this library item. Must be not <code>null</code>.
	 * @param summary       description of this library item. Can be <code>null</code>.
	 * @param cover         cover url. Can be <code>null</code>.
	 * @param urlByType     map contains URLs and their types. Must be not <code>null</code>.
	 * @param accessibility value defines when this library item will be accessible
	 *                      in the network library view. 
	 */
	public NetworkCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, Accessibility accessibility) {
		this(link, title, summary, cover, urlByType, accessibility, CatalogType.OTHER);
	}

	/**
	 * Creates new NetworkCatalogItem instance with specified accessibility and type.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title         title of this library item. Must be not <code>null</code>.
	 * @param summary       description of this library item. Can be <code>null</code>.
	 * @param cover         cover url. Can be <code>null</code>.
	 * @param urlByType     map contains URLs and their types. Must be not <code>null</code>.
	 * @param accessibility value defines when this library item will be accessible
	 *                      in the network library view. 
	 * @param catalogType   value defines type of this catalog.
	 */
	public NetworkCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, Accessibility accessibility, CatalogType catalogType) {
		super(link, title, summary, cover);
		myAccessibility = accessibility;
		myCatalogType = catalogType;
		URLByType = new TreeMap<Integer, String>(urlByType);
	}

	public Map<String,String> extraData() {
		return Collections.emptyMap();
	}

	public abstract void loadChildren(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException;

	public boolean supportsResumeLoading() {
		return false;
	}

	public void resumeLoading(NetworkOperationData.OnNewItemListener listener) throws ZLNetworkException {
		throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
	}


	/**
	 * Method is called each time this item is displayed to the user.
	 *
	 * This method is called when UI-element corresponding to this item is shown to the User.
	 */
	public void onDisplayItem() {
	}

	public final CatalogType getCatalogType() {
		return myCatalogType;
	}

	public ZLBoolean3 getVisibility() {
		final NetworkAuthenticationManager mgr = Link.authenticationManager();
		switch (myAccessibility) {
			default:
				return ZLBoolean3.B3_FALSE;
			case ALWAYS:
				return ZLBoolean3.B3_TRUE;
			case SIGNED_IN:
				if (mgr == null) {
					return ZLBoolean3.B3_FALSE;
				}
				try {
					return mgr.isAuthorised(false) ?
							ZLBoolean3.B3_TRUE : ZLBoolean3.B3_UNDEFINED;
				} catch (ZLNetworkException e) {
					return ZLBoolean3.B3_UNDEFINED;
				}
			case HAS_BOOKS:
				if (mgr != null && mgr.purchasedBooks().size() > 0) {
					return ZLBoolean3.B3_TRUE;
				} else {
					return ZLBoolean3.B3_FALSE;
				}
		}
	}
}
