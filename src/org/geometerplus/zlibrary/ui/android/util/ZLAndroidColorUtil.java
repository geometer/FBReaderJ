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

package org.geometerplus.zlibrary.ui.android.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class ZLAndroidColorUtil {
	public static int rgba(ZLColor color, int alpha) {
		return color != null
			? Color.argb(alpha, color.Red, color.Green, color.Blue)
			: Color.argb(alpha, 0, 0, 0);
	}

	public static int rgb(ZLColor color) {
		return color != null ? Color.rgb(color.Red, color.Green, color.Blue) : 0;
	}

	public static ZLColor getAverageColor(Bitmap bitmap) {
		final int w = Math.min(bitmap.getWidth(), 7);
		final int h = Math.min(bitmap.getHeight(), 7);
		long r = 0, g = 0, b = 0;
		for (int i = 0; i < w; ++i) {
			for (int j = 0; j < h; ++j) {
				int color = bitmap.getPixel(i, j);
				r += color & 0xFF0000;
				g += color & 0xFF00;
				b += color & 0xFF;
			}
		}
		r /= w * h;
		g /= w * h;
		b /= w * h;
		r >>= 16;
		g >>= 8;
		return new ZLColor((int)(r & 0xFF), (int)(g & 0xFF), (int)(b & 0xFF));
	}
}
