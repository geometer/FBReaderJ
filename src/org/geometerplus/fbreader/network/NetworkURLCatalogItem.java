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
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

public abstract class NetworkURLCatalogItem extends NetworkCatalogItem {
	// URL type values:
	public static final int URL_NONE = 0;
	public static final int URL_CATALOG = 1;
	public static final int URL_HTML_PAGE = 2;

	public final TreeMap<Integer,String> URLByType;

	/**
	 * Creates new NetworkURLCatalogItem instance with <code>Accessibility.ALWAYS</code> accessibility and <code>FLAGS_DEFAULT</code> flags.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param cover      cover url. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 */
	public NetworkURLCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer,String> urlByType) {
		this(link, title, summary, cover, urlByType, Accessibility.ALWAYS, FLAGS_DEFAULT);
	}

	/**
	 * Creates new NetworkURLCatalogItem instance with specified accessibility and type.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title         title of this library item. Must be not <code>null</code>.
	 * @param summary       description of this library item. Can be <code>null</code>.
	 * @param cover         cover url. Can be <code>null</code>.
	 * @param urlByType     map contains URLs and their types. Must be not <code>null</code>.
	 * @param accessibility value defines when this library item will be accessible
	 *                      in the network library view. 
	 * @param flags         value defines how to show book items in this catalog.
	 */
	public NetworkURLCatalogItem(INetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, Accessibility accessibility, int flags) {
		super(link, title, summary, cover, accessibility, flags);
		URLByType = new TreeMap<Integer,String>(urlByType);
	}

	/**
	 * Performs all necessary operations with NetworkOperationData and NetworkRequest
	 * to complete loading children items.
	 * 
	 * @param data Network operation data instance
	 * @param networkRequest initial network request
	 *  
	 * @throws ZLNetworkException when network operation couldn't be completed
	 */
	protected final void doLoadChildren(NetworkOperationData data,
			ZLNetworkRequest networkRequest) throws ZLNetworkException {
		while (networkRequest != null) {
			ZLNetworkManager.Instance().perform(networkRequest);
			if (data.Listener.confirmInterrupt()) {
				return;
			}
			networkRequest = data.resume();
		}
	}

	/**
	 * Override this method if result of the request depends not only from URL 
	 * (e.g. result of the POST request depends from the URL and the body of the request).
	 * 
	 * @return unique String for corresponding network request, for which:
	 * {@code item1.getFullRequestString().equals(item2.getFullRequestString())}
	 * iff network requests for items {@code item1} and {@code item2} are the same. 
	 */
	//public String getFullRequestString() {
	//	return URLByType.get(URL_CATALOG);
	//}

	@Override
	public String getStringId() {
		String id = URLByType.get(URL_CATALOG);
		if (id == null) {
			id = URLByType.get(URL_HTML_PAGE);
		}
		return id != null ? id : String.valueOf(hashCode());
	}
}
