/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.image.ZLImage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;

public final class ZLAndroidResourceBasedImageData extends ZLAndroidImageData implements ZLImage {
	private final Resources myResources;
	private final int myId;

	public ZLAndroidResourceBasedImageData(Resources resources, int id) {
		myResources = resources;
		myId = id;
	}

	protected Bitmap decodeWithOptions(BitmapFactory.Options options) {
		return BitmapFactory.decodeResource(myResources, myId, options);
	}
}
