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

public abstract class ZLMiscUtil {
	public static boolean equals(Object o0, Object o1) {
		return (o0 == null) ? (o1 == null) : o0.equals(o1);
	}

	public static boolean matchesIgnoreCase(String text, String lowerCasePattern) {
		return (text.length() >= lowerCasePattern.length()) &&
			   (text.toLowerCase().indexOf(lowerCasePattern) >= 0);
	}
}
