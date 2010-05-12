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

package org.geometerplus.android.fbreader.network;

class NetworkNotifications {
	private static NetworkNotifications ourInstance;

	public static NetworkNotifications Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkNotifications();
		}
		return ourInstance;
	}

	//private static final int CATALOG_LOADING = 0;
	//private static final int NETWORK_SEARCH = 1;

	private static final int BOOK_DOWNLOADING_START = 0x10000000;
	private static final int BOOK_DOWNLOADING_END   = 0x1fffffff;

	private volatile int myBookDownloadingId = BOOK_DOWNLOADING_START;

	private NetworkNotifications() {
	}

	/*public int getCatalogLoadingId() {
		return CATALOG_LOADING;
	}*/

	/*public int getNetworkSearchId() {
		return NETWORK_SEARCH;
	}*/

	public synchronized int getBookDownloadingId() {
		final int id = myBookDownloadingId;
		if (myBookDownloadingId == BOOK_DOWNLOADING_END) {
			myBookDownloadingId = BOOK_DOWNLOADING_START;
		} else {
			++myBookDownloadingId;
		}
		return id;
	}
}
