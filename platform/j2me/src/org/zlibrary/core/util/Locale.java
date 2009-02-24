/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

public final class Locale {
	static private Locale ourDefaultLocale;

	private final String myLanguage;

	private Locale(String property) {
		if (property == null) {
			final int index = property.indexOf('-');
			myLanguage = (index != -1) ? property.substring(0, index) : property;
		} else {
			myLanguage = "en";
		}
	}

	public static Locale getDefault() {
		if (ourDefaultLocale == null) {
			ourDefaultLocale = new Locale(System.getProperty("microedition.locale"));
		}
		return ourDefaultLocale;
	}

	public String getLanguage() {
		return myLanguage;
	}
}
