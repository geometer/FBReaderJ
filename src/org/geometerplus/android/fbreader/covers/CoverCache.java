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

package org.geometerplus.android.fbreader.covers;

import java.util.*;

import android.graphics.Bitmap;

import org.geometerplus.fbreader.tree.FBTree;

class CoverCache {
	static class NullObjectException extends Exception {
	}

	private static final Object NULL_BITMAP = new Object();

	volatile int HoldersCounter = 0;

	private final Map<FBTree.Key,Object> myBitmaps =
		Collections.synchronizedMap(new LinkedHashMap<FBTree.Key,Object>(10, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<FBTree.Key,Object> eldest) {
				return size() > 3 * HoldersCounter;
			}
		});

	Bitmap getBitmap(FBTree.Key key) throws NullObjectException {
		final Object bitmap = myBitmaps.get(key);
		if (bitmap == NULL_BITMAP) {
			throw new NullObjectException();
		}
		return (Bitmap)bitmap;
	}

	void putBitmap(FBTree.Key key, Bitmap bitmap) {
		myBitmaps.put(key, bitmap != null ? bitmap : NULL_BITMAP);
	}
}
