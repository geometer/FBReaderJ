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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.util.ZLTTFInfoDetector;

import org.geometerplus.fbreader.Paths;

public final class AndroidFontUtil {
	private static Method ourFontCreationMethod;
	static {
		try {
			ourFontCreationMethod = Typeface.class.getMethod("createFromFile", File.class);
		} catch (NoSuchMethodException e) {
			ourFontCreationMethod = null;
		}
	}

	public static boolean areExternalFontsSupported() {
		return ourFontCreationMethod != null;
	}

	public static Typeface createFontFromFile(File file) {
		if (ourFontCreationMethod == null) {
			return null;
		}
		try {
			return (Typeface)ourFontCreationMethod.invoke(null, file);
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	private static Map<String,File[]> ourFontMap;
	private static File[] ourFileList;
	private static long myTimeStamp;
	public static Map<String,File[]> getFontMap(boolean forceReload) {
		final long timeStamp = System.currentTimeMillis();
		if (forceReload && timeStamp < myTimeStamp + 1000) {
			forceReload = false;
		}
		myTimeStamp = timeStamp;
		if (ourFontMap == null || forceReload) {
			boolean rebuildMap = ourFontMap == null;
			if (ourFontCreationMethod == null) {
				if (rebuildMap) {
					ourFontMap = new HashMap<String,File[]>();
				}
			} else {
				final File[] fileList = new File(Paths.FontsDirectoryOption().getValue()).listFiles(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.startsWith(".")) {
								return false;
							}
							final String lcName = name.toLowerCase();
							return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
						}
					}
				);
				if (fileList == null) {
					if (ourFileList != null) {
						ourFileList = null;
						rebuildMap = true;
					}
				}
				if (fileList != null && !fileList.equals(ourFileList)) {
					ourFileList = fileList;
					rebuildMap = true;
				}
				if (rebuildMap) {
					ourFontMap = new ZLTTFInfoDetector().collectFonts(fileList);
				}
			}
		}
		return ourFontMap;
	}

	public static String realFontFamilyName(String fontFamily) {
		for (String name : getFontMap(false).keySet()) {
			if (name.equalsIgnoreCase(fontFamily)) {
				return name;
			}
		}
		if ("serif".equalsIgnoreCase(fontFamily) || "droid serif".equalsIgnoreCase(fontFamily)) {
			return "serif";
		}
		if ("sans-serif".equalsIgnoreCase(fontFamily) || "sans serif".equalsIgnoreCase(fontFamily) || "droid sans".equalsIgnoreCase(fontFamily)) {
			return "sans-serif";
		}
		if ("monospace".equalsIgnoreCase(fontFamily) || "droid mono".equalsIgnoreCase(fontFamily)) {
			return "monospace";
		}
		return "sans-serif";
	}

	public static void fillFamiliesList(ArrayList<String> families, boolean forceReload) {
		final TreeSet<String> familySet = new TreeSet<String>(getFontMap(forceReload).keySet());
		familySet.add("Droid Sans");
		familySet.add("Droid Serif");
		familySet.add("Droid Mono");
		families.addAll(familySet);
	}
}
