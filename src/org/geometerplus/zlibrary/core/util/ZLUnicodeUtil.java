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

package org.geometerplus.zlibrary.core.util;

public class ZLUnicodeUtil {
	public static int utf8Length(byte[] buffer, int str, int len) {
		final int last = str + len;
		int counter = 0;
		while (str < last) {
			final int bt = buffer[str];
			if ((bt & 0x80) == 0) {
				++str;
			} else if ((bt & 0x20) == 0) {
				str += 2;
			} else if ((bt & 0x10) == 0) {
				str += 3;
			} else {
				str += 4;
			}
			++counter;
		}
		return counter;
	}
}
