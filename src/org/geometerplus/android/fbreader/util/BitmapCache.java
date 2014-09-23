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

package org.geometerplus.android.fbreader.util;

import java.util.Map;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache {
	public static class Pair {
		public final Bitmap Bitmap;
		public final boolean Presented;

		Pair(Bitmap bitmap) {
			if (bitmap != NULL) {
				Bitmap = bitmap;
				Presented = bitmap != null;
			} else {
				Bitmap = null;
				Presented = true;
			}
		}
	}

	private static final Bitmap NULL = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
	private final LruCache<String,Bitmap> myLruCache;

	public BitmapCache() {
		myLruCache = new LruCache<String,Bitmap>(getCacheSize()) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	private int getCacheSize() {
		return (int)(Runtime.getRuntime().maxMemory() / 8);
	}

	public Pair get(String book) {
		return new Pair(myLruCache.get(book));
	}

	public void put(String book, Bitmap bitmap) {
		myLruCache.put(book, bitmap != null ? bitmap : NULL);
	}

	public Map<String,Bitmap> snapshot() {
		return myLruCache.snapshot();
	}

	public void remove(String book) {
		myLruCache.remove(book);
	}
}
