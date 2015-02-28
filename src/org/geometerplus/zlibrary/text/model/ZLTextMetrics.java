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

package org.geometerplus.zlibrary.text.model;

public final class ZLTextMetrics {
	public final int DPI;
	public final int FullWidth;
	public final int FullHeight;
	public final int FontSize;

	public ZLTextMetrics(int dpi, int fullWidth, int fullHeight, int fontSize) {
		DPI = dpi;
		FullWidth = fullWidth;
		FullHeight = fullHeight;
		FontSize = fontSize;
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
			DPI == oo.DPI &&
			FullWidth == oo.FullWidth &&
			FullHeight == oo.FullHeight;
	}

	@Override
	public int hashCode() {
		return DPI + 13 * (FullHeight + 13 * FullWidth);
	}
}
