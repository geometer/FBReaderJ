/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.geometerplus.zlibrary.core.image.ZLImageData;

public abstract class ZLAndroidImageData implements ZLImageData {
	private Bitmap myBitmap;
	private int myRealWidth;
	private int myRealHeight;
	private int myLastRequestedWidth = -1;
	private int myLastRequestedHeight = -1;

	protected ZLAndroidImageData() {
	}

	protected abstract Bitmap decodeWithOptions(BitmapFactory.Options options);

	public Bitmap getFullSizeBitmap() {
		return getBitmap(0, 0, true);
	}

	public Bitmap getBitmap(int maxWidth, int maxHeight) {
		return getBitmap(maxWidth, maxHeight, false);
	}

	private synchronized Bitmap getBitmap(int maxWidth, int maxHeight, boolean ignoreSize) {
		if (!ignoreSize && (maxWidth == 0 || maxHeight == 0)) {
			return null;
		}
		if (maxWidth != myLastRequestedWidth || maxHeight != myLastRequestedHeight) {
			if (myBitmap != null) {
				myBitmap.recycle();
				myBitmap = null;
			}
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				if (myRealWidth <= 0) {
					options.inJustDecodeBounds = true;
					decodeWithOptions(options);
					myRealWidth = options.outWidth;
					myRealHeight = options.outHeight;
				}
				options.inJustDecodeBounds = false;
				int coefficient = 1;
				if (!ignoreSize) {
					while ((myRealHeight > maxHeight * coefficient) ||
						   (myRealWidth > maxWidth *coefficient)) {
						coefficient *= 2;
					}
				}
				options.inSampleSize = coefficient;
				myBitmap = decodeWithOptions(options);
				if (myBitmap != null) {
					myLastRequestedWidth = maxWidth;
					myLastRequestedHeight = maxHeight;
				}
			} catch (OutOfMemoryError e) {
			}
		}
		return myBitmap;
	}
}
