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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.ZLTTFInfoDetector;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.fbreader.Paths;

public final class AndroidFontUtil {
	private static Map<String,String[]> ourFontAssetMap;
	private static Map<String,File[]> ourFontFileMap;
	private static Set<File> ourFileSet;
	private static long ourTimeStamp;

	private static Map<String,String[]> getFontAssetMap() {
		if (ourFontAssetMap == null) {
			ourFontAssetMap = new HashMap<String,String[]>();
			new ZLXMLReaderAdapter() {
				@Override
				public boolean startElementHandler(String tag, ZLStringMap attributes) {
					if ("font".equals(tag)) {
						ourFontAssetMap.put(attributes.getValue("family"), new String[] {
							"fonts/" + attributes.getValue("regular"),
							"fonts/" + attributes.getValue("bold"),
							"fonts/" + attributes.getValue("italic"),
							"fonts/" + attributes.getValue("boldItalic")
						});
					}
					return false;
				}
			}.readQuietly(ZLFile.createFileByPath("fonts/fonts.xml"));
		}
		return ourFontAssetMap;
	}

	private static Map<String,File[]> getFontFileMap(boolean forceReload) {
		final long timeStamp = System.currentTimeMillis();
		if (forceReload && timeStamp < ourTimeStamp + 1000) {
			forceReload = false;
		}
		ourTimeStamp = timeStamp;
		if (ourFileSet == null || forceReload) {
			final HashSet<File> fileSet = new HashSet<File>();
			final FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.startsWith(".")) {
						return false;
					}
					final String lcName = name.toLowerCase();
					return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
				}
			};
			for (String dir : Paths.FontPathOption.getValue()) {
				final File[] fileList = new File(dir).listFiles(filter);
				if (fileList != null) {
					fileSet.addAll(Arrays.asList(fileList));
				}
			}
			if (!fileSet.equals(ourFileSet)) {
				ourFileSet = fileSet;
				ourFontFileMap = new ZLTTFInfoDetector().collectFonts(fileSet);
			}
		}
		return ourFontFileMap;
	}

	public static String realFontFamilyName(String fontFamily) {
		for (String name : getFontAssetMap().keySet()) {
			if (name.equalsIgnoreCase(fontFamily)) {
				return name;
			}
		}
		for (String name : getFontFileMap(false).keySet()) {
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

	public static void fillFamiliesList(ArrayList<String> families) {
		final TreeSet<String> familySet = new TreeSet<String>(getFontFileMap(true).keySet());
		familySet.addAll(getFontAssetMap().keySet());
		familySet.add("Droid Sans");
		familySet.add("Droid Serif");
		familySet.add("Droid Mono");
		families.addAll(familySet);
	}

	private static final HashMap<String,Typeface[]> ourTypefaces = new HashMap<String,Typeface[]>();

	private static Typeface createTypefaceFromAsset(Typeface[] typefaces, String family, int style) {
		final String[] assets = getFontAssetMap().get(family);
		if (assets == null) {
			return null;
		}
		try {
			return Typeface.createFromAsset(
				((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getAssets(), assets[style]
			);
		} catch (Throwable t) {
		}
		return null;
	}

	private static Typeface createTypefaceFromFile(Typeface[] typefaces, String family, int style) {
		final File[] files = getFontFileMap(false).get(family);
		if (files == null) {
			return null;
		}
		try {
			if (files[style] != null) {
				return Typeface.createFromFile(files[style]);
			}
			for (int i = 0; i < 4; ++i) {
				if (files[i] != null) {
					if (typefaces[i] == null) {
						typefaces[i] = Typeface.createFromFile(files[i]);
					}
					return typefaces[i];
				}
			}
		} catch (Throwable e) {
		}
		return null;
	}

	public static Typeface typeface(String family, boolean bold, boolean italic) {
		family = realFontFamilyName(family);
		final int style = (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
		Typeface[] typefaces = ourTypefaces.get(family);
		if (typefaces == null) {
			typefaces = new Typeface[4];
			ourTypefaces.put(family, typefaces);
		}
		Typeface tf = typefaces[style];
		if (tf == null) {
			tf = createTypefaceFromFile(typefaces, family, style);
		}
		if (tf == null) {
			tf = createTypefaceFromAsset(typefaces, family, style);
		}
		if (tf == null) {
			tf = Typeface.create(family, style);
		}
		typefaces[style] = tf;
		return tf;
	}

	public static void clearFontCache() {
		ourTypefaces.clear();
		ourFileSet = null;
	}
}
