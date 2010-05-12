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

package org.geometerplus.fbreader.network;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;


public abstract class NetworkCatalogItem extends NetworkLibraryItem {

	// catalog types:
	public static final int CATALOG_OTHER = 0;
	public static final int CATALOG_BY_AUTHORS = 1;

	// catalog visibility types:
	public static final int VISIBLE_ALWAYS = 1;
	public static final int VISIBLE_LOGGED_USER = 2;

	// URL type values:
	public static final int URL_NONE = 0;
	public static final int URL_CATALOG = 1;
	public static final int URL_HTML_PAGE = 2;

	public final int Visibility;
	public final int CatalogType;
	public final TreeMap<Integer, String> URLByType;

	/**
	 * Creates new NetworkCatalogItem instance with <code>VISIBLE_ALWAYS</code> visibility and <code>CATALOG_OTHER</code> type.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param cover      cover url. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 */
	public NetworkCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType) {
		this(link, title, summary, cover, urlByType, VISIBLE_ALWAYS, CATALOG_OTHER);
	}

	/**
	 * Creates new NetworkCatalogItem instance with specified visibility and <code>CATALOG_OTHER</code> type.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param cover      cover url. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 * @param visibility value defines when this library item will be shown in the network library. 
	 *                   Can be one of the VISIBLE_* values.
	 */
	public NetworkCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility) {
		this(link, title, summary, cover, urlByType, visibility, CATALOG_OTHER);
	}

	/**
	 * Creates new NetworkCatalogItem instance with specified visibility and type.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param cover      cover url. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 * @param visibility value defines when this library item will be shown in the network library. 
	 *                   Can be one of the VISIBLE_* values.
	 * @param catalogType value defines type of this catalog. Can be one of the CATALOG_* values.
	 */
	public NetworkCatalogItem(NetworkLink link, String title, String summary, String cover, Map<Integer, String> urlByType, int visibility, int catalogType) {
		super(link, title, summary, cover);
		Visibility = visibility;
		CatalogType = catalogType;
		URLByType = new TreeMap<Integer, String>(urlByType);
	}

	public abstract String loadChildren(NetworkOperationData.OnNewItemListener listener); // returns Error Message

	/*public final String loadChildren(final List<NetworkLibraryItem> children) {
		return loadChildren(new CatalogListener() {
			public void onNewItem(NetworkLibraryItem item) {
				children.add(item);
			}
		});
	}*/

	/**
	 * Method is called each time this item is displayed to the user.
	 *
	 * This method is called when UI-element corresponding to this item is shown to the User.
	 */
	public void onDisplayItem() {
	}

	public int getVisibility() {
		if (Visibility == VISIBLE_ALWAYS) {
			return ZLBoolean3.B3_TRUE;
		}
		if (Visibility == VISIBLE_LOGGED_USER) {
			if (Link.authenticationManager() == null) {
				return ZLBoolean3.B3_FALSE;
			}
			return (Link.authenticationManager().isAuthorised(false).Status == ZLBoolean3.B3_TRUE) ?
				ZLBoolean3.B3_TRUE : ZLBoolean3.B3_UNDEFINED;
		}
		return ZLBoolean3.B3_FALSE;
	}
}
