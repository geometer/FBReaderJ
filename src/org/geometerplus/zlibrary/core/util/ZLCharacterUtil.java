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

public abstract class ZLCharacterUtil {
	public static boolean isLetter(char ch) {
		return
			(('a' <= ch) && (ch <= 'z')) ||
			(('A' <= ch) && (ch <= 'Z')) ||
			// ' is "letter" (in French, for example)
			(ch == '\'') ||
			// ^ is "letter" (in Esperanto)
			(ch == '^') ||
			// latin1
			((0xC0 <= ch) && (ch <= 0xFF) && (ch != 0xD7) && (ch != 0xF7)) ||
			// extended latin1
			((0x100 <= ch) && (ch <= 0x178)) ||
			// cyrillic
			((0x410 <= ch) && (ch <= 0x44F)) ||
			// cyrillic YO & yo
			(ch == 0x401) || (ch == 0x451);
	}
}
