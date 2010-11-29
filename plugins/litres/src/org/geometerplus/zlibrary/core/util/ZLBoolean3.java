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

public final class ZLBoolean3 {
	public static final int B3_FALSE = 0;
	public static final int B3_TRUE = 1;
	public static final int B3_UNDEFINED = 2;
	
	private static final String STRING_FALSE = "false";
	private static final String STRING_TRUE = "true";
	private static final String STRING_UNDEFINED = "undefined";

	public static int getByString(String name) {
		if (STRING_TRUE.equals(name)) {
			return B3_TRUE;
		}
		if (STRING_FALSE.equals(name)) {
			return B3_FALSE;
		}
		return B3_UNDEFINED;
	}
	
	public static String getName(int value) {
		switch (value) {
			case B3_FALSE:
				return STRING_FALSE;
			case B3_TRUE:
				return STRING_TRUE;
			default:
				return STRING_UNDEFINED;
		}
	}

	private ZLBoolean3() {
	}
}
