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

import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public abstract class NetworkURLCatalogItem extends NetworkCatalogItem {
	/**
	 * Creates new NetworkURLCatalogItem instance with specified accessibility and type.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title         title of this library item. Must be not <code>null</code>.
	 * @param summary       description of this library item. Can be <code>null</code>.
	 * @param urls          collection of item-related URLs. Can be <code>null</code>.
	 * @param accessibility value defines when this library item will be accessible
	 *                      in the network library view.
	 * @param flags         value defines how to show book items in this catalog.
	 */
	protected NetworkURLCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags) {
		super(link, title, summary, urls, accessibility, flags);
	}

	protected String getCatalogUrl() {
		return getUrl(UrlInfo.Type.Catalog);
	}

	@Override
	public boolean canBeOpened() {
		return getCatalogUrl() != null;
	}

	@Override
	public String getStringId() {
		String id = getUrl(UrlInfo.Type.Catalog);
		if (id == null) {
			id = getUrl(UrlInfo.Type.HtmlPage);
		}
		return id != null ? id : String.valueOf(hashCode());
	}
}
