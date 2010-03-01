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

	public interface URLType {
		int URL_NONE = 0;
		int URL_CATALOG = 1;
		int URL_BOOK_EPUB = 2;
		int URL_BOOK_MOBIPOCKET = 3;
		int URL_BOOK_FB2_ZIP = 4;
		int URL_BOOK_PDF = 5;
		int URL_BOOK_DEMO_FB2_ZIP = 6;
		int URL_HTML_PAGE = 7;
		int URL_COVER = 8;
	}


	public final NetworkLink Link;
	public final String Title;
	public final String Summary;
	public final TreeMap<Integer, String> URLByType;

	protected NetworkLibraryItem(NetworkLink link, String title, String summary, Map<Integer, String> urlByType) {
		Link = link;
		Title = title;
		Summary = summary;
		URLByType = new TreeMap<Integer, String>(urlByType);
	}

}
