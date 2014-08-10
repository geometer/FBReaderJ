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

import java.util.*;

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.book.Book;
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

	private Map<String,Object> positionMap(IBookCollection collection, Book book) {
		if (book == null) {
			return null;
		}
		final ZLTextPosition pos = collection.getStoredPosition(book.getId());
		if (pos == null) {
			return null;
		}
		final Map<String,Object> map = new HashMap<String,Object>();
		map.put("para", pos.getParagraphIndex());
		map.put("elmt", pos.getElementIndex());
		map.put("char", pos.getCharIndex());
		if (pos instanceof ZLTextFixedPosition.WithTimestamp) {
			map.put("timestamp", ((ZLTextFixedPosition.WithTimestamp)pos).Timestamp);
		}
		return map;
	}

	Map<String,Object> data(IBookCollection collection) {
		final Map<String,Object> map = new HashMap<String,Object>();
		map.put("generation", myGeneration.getValue());

		final Book currentBook = collection.getRecentBook(0);
		if (currentBook != null) {
			final String oldHash = myCurrentBookHash.getValue();
			final String newHash = collection.getHash(currentBook);
			if (newHash != null && !newHash.equals(oldHash)) {
				myCurrentBookHash.setValue(newHash);
				if (oldHash.length() != 0) {
					myCurrentBookTimestamp.setValue(String.valueOf(System.currentTimeMillis()));
				}
			}
			final String currentBookHash = newHash != null ? newHash : oldHash;

			final Map<String,Object> currentBookMap = new HashMap<String,Object>();
			currentBookMap.put("hash", currentBookHash);
			try {
				currentBookMap.put("timestamp", Long.parseLong(myCurrentBookTimestamp.getValue()));
			} catch (Exception e) {
			}
			map.put("currentbook", currentBookMap);

			final List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
			Map<String,Object> posMap = positionMap(collection, currentBook);
			if (posMap != null) {
				posMap.put("hash", currentBookHash);
				lst.add(posMap);
			}
			if (!currentBookHash.equals(oldHash)) {
				posMap = positionMap(collection, collection.getBookByHash(oldHash));
				if (posMap != null) {
					posMap.put("hash", oldHash);
					lst.add(posMap);
				}
			}
			if (lst.size() > 0) {
				map.put("positions", lst);
			}
		}

		System.err.println("DATA = " + map);
		return map;
	}

	void updateFromServer(Map<String,Object> data) {
		System.err.println("RESPONSE = " + data);
		myGeneration.setValue((int)(long)(Long)data.get("generation"));
	}
}
