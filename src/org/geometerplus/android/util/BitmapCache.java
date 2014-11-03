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

package org.geometerplus.android.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache {
	public static class Container {
		public final Bitmap Bitmap;

		private Container(Bitmap bitmap) {
			Bitmap = bitmap;
		}

		int size() {
			return Bitmap.getRowBytes() * Bitmap.getHeight();
		}
	}

	private static final Container NULL = new Container(null) {
		@Override
		int size() {
			return 1;
		}
	};

	private final LruCache<Long,Container> myLruCache;

	public BitmapCache(float factor) {
		myLruCache = new LruCache<Long,Container>((int)(factor * Runtime.getRuntime().maxMemory())) {
			@Override
			protected int sizeOf(Long key, Container container) {
				return container.size();
			}
		};
	}

	public Container get(Long key) {
		return myLruCache.get(key);
	}

	public void put(Long key, Bitmap bitmap) {
		myLruCache.put(key, bitmap != null ? new Container(bitmap) : NULL);
	}

	public void remove(Long key) {
		myLruCache.remove(key);
	}
}
