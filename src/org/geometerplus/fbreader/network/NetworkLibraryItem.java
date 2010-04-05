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


public abstract class NetworkLibraryItem {

	// URL type values:
	public static final int URL_NONE = 0;
	public static final int URL_CATALOG = 1;
	public static final int URL_HTML_PAGE = 2;
	public static final int URL_COVER = 3;

	public final NetworkLink Link;
	public final String Title;
	public final String Summary;
	public final TreeMap<Integer, String> URLByType;

	//public org.geometerplus.fbreader.network.atom.ATOMEntry dbgEntry;

	/**
	 * Creates new NetworkLibraryItem instance.
	 *
	 * @param link       corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param title      title of this library item. Must be not <code>null</code>.
	 * @param summary    description of this library item. Can be <code>null</code>.
	 * @param urlByType  map contains URLs and their types. Must be not <code>null</code>.
	 */
	protected NetworkLibraryItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType) {
		Link = link;
		Title = title;
		Summary = summary;
		URLByType = new TreeMap<Integer, String>(urlByType);
	}
}
