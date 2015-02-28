/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

final class BitmapImageData extends ZLAndroidImageData {
	static BitmapImageData get(ZLBitmapImage image) {
		final Bitmap bitmap = image.getBitmap();
		return bitmap != null ? new BitmapImageData(bitmap) : null;
	}

	private final Bitmap myBitmap;

	private BitmapImageData(Bitmap bitmap) {
		myBitmap = bitmap;
	}

	protected Bitmap decodeWithOptions(BitmapFactory.Options options) {
		final int scaleFactor = options.inSampleSize;
		if (scaleFactor <= 1) {
			return myBitmap;
		}
		try {
			return Bitmap.createScaledBitmap(
				myBitmap, myBitmap.getWidth() / scaleFactor, myBitmap.getHeight() / scaleFactor, false
			);
		} catch (Exception e) {
			return null;
		}
	}
}
