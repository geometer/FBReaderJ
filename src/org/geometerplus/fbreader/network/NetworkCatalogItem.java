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


public abstract class NetworkCatalogItem extends NetworkLibraryItem {

	public final int Visibility;

	public interface CatalogType {
		int OTHER = 0;
		int BY_AUTHORS = 1;
	}

	public interface VisibilityType {
		int NEVER = 0;
		int ALWAYS = 1;
		int LOGGED_USERS = 2;
	}

	/**
	 * Creates new NetworkCatalogItem instance with visibility set to <code>ALWAYS</code>.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 */
	public NetworkCatalogItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType) {
		this(link, title, summary, urlByType, VisibilityType.ALWAYS);
	}

	/**
	 * Creates new NetworkCatalogItem instance with specified visibility.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 * @param visibility value defines when this library item will be shown in the network library. 
	 *                   Can be on of the values listed in <code>VisibilityType</code> interface.
	 */
	public NetworkCatalogItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType, int visibility) {
		super(link, title, summary, urlByType);
		Visibility = visibility;
	}

	public abstract String loadChildren(List<NetworkLibraryItem> children); // returns Error Message

	public int catalogType() {
		return CatalogType.OTHER;
	}
}
