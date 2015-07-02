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

package org.geometerplus.zlibrary.core.util;

/**
 * class Color. Color is presented as the triple of short's (Red, Green, Blue components)
 * Each component should be in the range 0..255
 */
public final class ZLColor {
	public final short Red;
	public final short Green;
	public final short Blue;

	public ZLColor(int r, int g, int b) {
		Red = (short)(r & 0xFF);
		Green = (short)(g & 0xFF);
		Blue = (short)(b & 0xFF);
	}

	public ZLColor(int intValue) {
		Red = (short)((intValue >> 16) & 0xFF);
		Green = (short)((intValue >> 8) & 0xFF);
		Blue = (short)(intValue & 0xFF);
	}

	public int intValue() {
		return (Red << 16) + (Green << 8) + Blue;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ZLColor)) {
			return false;
		}

		ZLColor color = (ZLColor)o;
		return color.Red == Red && color.Green == Green && color.Blue == Blue;
	}

	@Override
	public int hashCode() {
		return intValue();
	}

	@Override
	public String toString() {
		return new StringBuilder("ZLColor(")
			.append(String.valueOf(Red)).append(", ")
			.append(String.valueOf(Green)).append(", ")
			.append(String.valueOf(Blue)).append(")")
			.toString();
	}
}
