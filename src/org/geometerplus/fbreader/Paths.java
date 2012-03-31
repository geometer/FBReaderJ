/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader;

import org.geometerplus.zlibrary.core.options.*;

import android.os.Environment;

public abstract class Paths {

	public static String BOOKS_DIRECTORY = "Books";
	public static String FONTS_DIRECTORY = "Fonts";
	public static String WALLPAPERS_DIRECTORY = "Walpapers";

	public static String cardDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static ZLStringListOption DirectoryOption(String s) {
		if (FONTS_DIRECTORY.equals(s)) return new ZLStringListOption("Files", "FontsDirectory", cardDirectory() + "/Fonts", "\n");
		if (WALLPAPERS_DIRECTORY.equals(s)) return new ZLStringListOption("Files", "WallpapersDirectory", cardDirectory() + "/Wallpapers", "\n");
		return null;
	}

	public static ZLStringOption BooksDirectoryOption() {
		return new ZLStringOption("Files", "BooksDirectory", cardDirectory() + "/Books");
	}

	public static String cacheDirectory() {
		return BooksDirectoryOption().getValue() + "/.FBReader";
	}

	public static String networkCacheDirectory() {
		return cacheDirectory() + "/cache";
	}

	public static String systemShareDirectory() {
		return "/system/usr/share/FBReader";
	}
}
