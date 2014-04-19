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

package org.geometerplus.fbreader;

import java.io.*;
import java.util.*;

import android.os.Environment;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;

public abstract class Paths {
	public static ZLStringListOption BookPathOption =
		pathOption("BooksDirectory", defaultBookDirectory());

	public static ZLStringListOption FontPathOption =
		pathOption("FontPathOption", cardDirectory() + "/Fonts");

	public static ZLStringListOption WallpaperPathOption =
		pathOption("WallpapersDirectory", cardDirectory() + "/Wallpapers");

	public static ZLStringOption TempDirectoryOption =
		new ZLStringOption("Files", "TemporaryDirectory", "");

	public static ZLStringOption DownloadsDirectoryOption =
		new ZLStringOption("Files", "DownloadsDirectory", "");
	static {
		if ("".equals(DownloadsDirectoryOption.getValue())) {
			DownloadsDirectoryOption.setValue(mainBookDirectory());
		}
	}

	public static String cardDirectory() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return Environment.getExternalStorageDirectory().getPath();
		}

		final List<String> dirNames = new LinkedList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/proc/self/mounts"));
			String line;
			while ((line = reader.readLine()) != null) {
				final String[] parts = line.split("\\s+");
				if (parts.length >= 4 &&
					parts[2].toLowerCase().indexOf("fat") >= 0 &&
					parts[3].indexOf("rw") >= 0) {
					final File fsDir = new File(parts[1]);
					if (fsDir.isDirectory() && fsDir.canWrite()) {
						dirNames.add(fsDir.getPath());
					}
				}
			}
		} catch (Throwable e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		for (String dir : dirNames) {
			if (dir.toLowerCase().indexOf("media") > 0) {
				return dir;
			}
		}
		if (dirNames.size() > 0) {
			return dirNames.get(0);
		}

		return Environment.getExternalStorageDirectory().getPath();
	}

	private static String defaultBookDirectory() {
		return cardDirectory() + "/Books";
	}

	private static ZLStringListOption pathOption(String key, String defaultDirectory) {
		final ZLStringListOption option = new ZLStringListOption(
			"Files", key, Collections.<String>emptyList(), "\n"
		);
		if (option.getValue().isEmpty()) {
			option.setValue(Collections.singletonList(defaultDirectory));
		}
		return option;
	}

	public static List<String> bookPath() {
		final List<String> path = new ArrayList<String>(Paths.BookPathOption.getValue());
		final String downloadsDirectory = DownloadsDirectoryOption.getValue();
		if (!"".equals(downloadsDirectory) && !path.contains(downloadsDirectory)) {
			path.add(downloadsDirectory);
		}
		return path;
	}

	public static String mainBookDirectory() {
		final List<String> bookPath = BookPathOption.getValue();
		return bookPath.isEmpty() ? defaultBookDirectory() : bookPath.get(0);
	}

	public static String tempDirectory() {
		return TempDirectoryOption.getValue();
	}

	public static String networkCacheDirectory() {
		return tempDirectory() + "/cache";
	}

	public static String systemShareDirectory() {
		return "/system/usr/share/FBReader";
	}
}
