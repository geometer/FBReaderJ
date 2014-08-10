/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.sync;

import java.util.HashMap;
import java.util.Map;

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.book.IBookCollection;

class SyncData {
	private final ZLIntegerOption myGeneration =
		new ZLIntegerOption("SyncData", "Generation", -1);
	private final ZLStringOption myCurrentBookHash =
		new ZLStringOption("SyncData", "CurrentBookHash", "");
	private final ZLStringOption myCurrentBookTimestamp =
		new ZLStringOption("SyncData", "CurrentBookTimestamp", "");
	private final ZLStringOption myLastSyncTimestamp =
		new ZLStringOption("SyncData", "LastSyncTimestamp", "");

	void update(IBookCollection collection) {
		final String oldHash = myCurrentBookHash.getValue();
		final String newHash = collection.getHash(collection.getRecentBook(0));
		if (newHash != null && !newHash.equals(oldHash)) {
			myCurrentBookHash.setValue(newHash);
			if (oldHash.length() != 0) {
				myCurrentBookTimestamp.setValue(String.valueOf(System.currentTimeMillis()));
			}
		}
	}

	Map<String,Object> data() {
		final Map<String,Object> map = new HashMap<String,Object>();

		map.put("generation", myGeneration.getValue());

		final String currentBookHash = myCurrentBookHash.getValue();
		if (currentBookHash.length() != 0) {
			final Map<String,Object> currentBook = new HashMap<String,Object>();
			currentBook.put("hash", currentBookHash);
			try {
				currentBook.put("timestamp", Long.parseLong(myCurrentBookTimestamp.getValue()));
			} catch (Exception e) {
			}
			map.put("currentbook", currentBook);
		}

		System.err.println("DATA = " + map);
		return map;
	}

	void updateFromServer(Map<String,Object> data) {
		System.err.println("RESPONSE = " + data);
		myGeneration.setValue((int)(long)(Long)data.get("generation"));
	}
}
