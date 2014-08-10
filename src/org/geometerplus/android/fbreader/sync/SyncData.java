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

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.book.IBookCollection;

class SyncData {
	public final ZLIntegerOption Generation =
		new ZLIntegerOption("SyncData", "Generation", -1);
	public final ZLStringOption CurrentBookHash =
		new ZLStringOption("SyncData", "CurrentBookHash", "");
	private final ZLStringOption myCurrentBookTimestamp =
		new ZLStringOption("SyncData", "CurrentBookTimestamp", "");
	public final ZLStringOption LastSyncTimestamp =
		new ZLStringOption("SyncData", "LastSyncTimestamp", "");

	void update(IBookCollection collection) {
		final String oldHash = CurrentBookHash.getValue();
		final String newHash = collection.getHash(collection.getRecentBook(0));
		if (newHash != null && !newHash.equals(oldHash)) {
			CurrentBookHash.setValue(newHash);
			if (oldHash.length() != 0) {
				myCurrentBookTimestamp.setValue(String.valueOf(System.currentTimeMillis()));
			}
		}
	}

	final long currentBookTimestamp() {
		try {
			return Long.parseLong(myCurrentBookTimestamp.getValue());
		} catch (Exception e) {
			return -1L;
		}
	}
}
