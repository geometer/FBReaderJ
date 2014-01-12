/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.model;

public final class ZLTextMetrics {
	public final int DPI;
	public final int DefaultFontSize;
	public final int FontSize;
	public final int FontXHeight;
	public final int FullWidth;
	public final int FullHeight;

	public ZLTextMetrics(int dpi, int defaultFontSize, int fontSize, int fontXHeight, int fullWidth, int fullHeight) {
		DPI = dpi;
		DefaultFontSize = defaultFontSize;
		FontSize = fontSize;
		FontXHeight = fontXHeight;
		FullWidth = fullWidth;
		FullHeight = fullHeight;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ZLTextMetrics)) {
			return false;
		}
		final ZLTextMetrics oo = (ZLTextMetrics)o;
		return
			FontSize == oo.FontSize &&
			FontXHeight == oo.FontXHeight &&
			FullWidth == oo.FullWidth &&
			FullHeight == oo.FullHeight;
	}

	@Override
	public int hashCode() {
		return FontSize + 13 * (FontXHeight + 13 * (FullHeight + 13 * FullWidth));
	}
}
