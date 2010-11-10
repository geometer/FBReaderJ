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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.util.ZLTTFInfo;
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
	public static Map<String,File[]> getFontMap() {
		if (ourFontMap == null) {
			if (ourFontCreationMethod == null) {
				ourFontMap = new HashMap<String,File[]>();
			} else {
				ourFontMap = new ZLTTFInfoDetector().collectFonts(new File(Paths.FontsDirectoryOption().getValue()).listFiles(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".ttf") && !name.startsWith(".");
						}
					}
				));
			}
		}
		return ourFontMap;
	}

	public static String realFontFamilyName(String fontFamily) {
		if ("serif".equalsIgnoreCase(fontFamily) || "droid serif".equalsIgnoreCase(fontFamily)) {
			return "serif";
		}
		if ("sans-serif".equalsIgnoreCase(fontFamily) || "sans serif".equalsIgnoreCase(fontFamily) || "droid sans".equalsIgnoreCase(fontFamily)) {
			return "sans-serif";
		}
		if ("monospace".equalsIgnoreCase(fontFamily) || "droid mono".equalsIgnoreCase(fontFamily)) {
			return "monospace";
		}
		for (String name : getFontMap().keySet()) {
			if (name.equalsIgnoreCase(fontFamily)) {
				return name;
			}
		}
		return "sans-serif";
	}

	public static void fillFamiliesList(ArrayList<String> families) {
		families.add("Droid Sans");
		families.add("Droid Serif");
		families.add("Droid Mono");
		families.addAll(getFontMap().keySet());
		Collections.sort(families);
	}
}
