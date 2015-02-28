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

import java.io.InputStream;
import java.io.IOException;

import android.graphics.*;

import org.geometerplus.zlibrary.core.image.ZLStreamImage;

final class InputStreamImageData extends ZLAndroidImageData {
	private final ZLStreamImage myImage;

	InputStreamImageData(ZLStreamImage image) {
		myImage = image;
	}

	protected Bitmap decodeWithOptions(BitmapFactory.Options options) {
		final InputStream stream = myImage.inputStream();
		if (stream == null) {
			return null;
		}

		final Bitmap bmp = BitmapFactory.decodeStream(stream, new Rect(), options);
		try {
			stream.close();
		} catch (IOException e) {
		}
		return bmp;
	}
}
