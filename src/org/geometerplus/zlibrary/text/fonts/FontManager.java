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

package org.geometerplus.zlibrary.text.fonts;

import java.io.*;
import java.util.*;

import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class FontManager {
	private final ArrayList<List<String>> myFamilyLists = new ArrayList<List<String>>();
	public final Map<String,FontEntry> Entries =
		Collections.synchronizedMap(new HashMap<String,FontEntry>());

	public synchronized int index(List<String> families) {
		for (int i = 0; i < myFamilyLists.size(); ++i) {
			if (myFamilyLists.get(i).equals(families)) {
				return i;
			}
		}
		myFamilyLists.add(new ArrayList<String>(families));
		return myFamilyLists.size() - 1;
	}

	public synchronized List<String> getFamilyList(int index) {
		return index < myFamilyLists.size()
			? myFamilyLists.get(index) : Collections.<String>emptyList();
	}

	private static String alias(String family, boolean bold, boolean italic) {
		final StringBuilder builder = new StringBuilder("/mnt/sdcard/");
		builder.append(family);
		if (bold) {
			builder.append("-bold");
		}
		if (italic) {
			builder.append("-italic");
		}
		return builder.append(".font").toString();
	}

	private static boolean copy(String from, String to) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = ZLFile.createFileByPath(from).getInputStream();
			os = new FileOutputStream(to);
			final byte[] buffer = new byte[8192];
			while (true) {
				final int len = is.read(buffer);
				if (len <= 0) {
					break;
				}
				os.write(buffer, 0, len);
			}
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				os.close();
			} catch (Throwable t) {
				// ignore
			}
			try {
				is.close();
			} catch (Throwable t) {
				// ignore
			}
		}
	}

	private final Map<String,Object> myCachedTypefaces = new HashMap<String,Object>();
	private static final Object NULL_OBJECT = new Object();

	private Typeface getOrCreateTypeface(FontEntry entry, String family, boolean bold, boolean italic) {
		final String fileName = entry.fileName(bold, italic);
		if (fileName == null) {
			return null;
		}
		final String realFileName = alias(family, bold, italic);
		Object cached = myCachedTypefaces.get(realFileName);
		if (cached == null) {
			if (copy(fileName, realFileName)) {
				try {
					cached = Typeface.createFromFile(realFileName);
				} catch (Throwable t) {
					// ignore
				}
			}
			myCachedTypefaces.put(realFileName, cached != null ? cached : NULL_OBJECT);
		}
		return cached instanceof Typeface ? (Typeface)cached : null;
	}

	public Typeface getTypeface(String family, boolean bold, boolean italic) {
		final FontEntry entry = Entries.get(family);
		if (entry == null) {
			return null;
		}
		{
			final int index = (bold ? 1 : 0) + (italic ? 2 : 0);
			final Typeface tf = getOrCreateTypeface(entry, family, bold, italic);
			if (tf != null) {
				return tf;
			}
		}
		for (int i = 0; i < 4; ++i) {
			final Typeface tf = getOrCreateTypeface(entry, family, (i & 1) == 1, (i & 2) == 2);
			if (tf != null) {
				return tf;
			}
		}
		return null;
	}
}
