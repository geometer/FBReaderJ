/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.*;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.fonts.FileInfo;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.fbreader.Paths;

public final class AndroidFontUtil {
	private static volatile Map<String,String[]> ourFontAssetMap;
	private static volatile Map<String,File[]> ourFontFileMap;
	private static volatile Set<File> ourFileSet;
	private static volatile long ourTimeStamp;

	private static Map<String,String[]> getFontAssetMap() {
		if (ourFontAssetMap == null) {
			ourFontAssetMap = new HashMap<String,String[]>();
			XmlUtil.parseQuietly(
				ZLFile.createFileByPath("fonts/fonts.xml"),
				new DefaultHandler() {
					@Override
					public void startElement(String uri, String localName, String qName, Attributes attributes) {
						if ("font".equals(localName)) {
							ourFontAssetMap.put(attributes.getValue("family"), new String[] {
								"fonts/" + attributes.getValue("regular"),
								"fonts/" + attributes.getValue("bold"),
								"fonts/" + attributes.getValue("italic"),
								"fonts/" + attributes.getValue("boldItalic")
							});
						}
					}
				}
			);
		}
		return ourFontAssetMap;
	}

	private static synchronized Map<String,File[]> getFontFileMap(boolean forceReload) {
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

	public static Typeface typeface(SystemInfo systemInfo, FontEntry entry, boolean bold, boolean italic) {
		if (entry.isSystem()) {
			return systemTypeface(entry.Family, bold, italic);
		} else {
			return embeddedTypeface(systemInfo, entry, bold, italic);
		}
	}

	public static Typeface systemTypeface(String family, boolean bold, boolean italic) {
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

	private static final class Spec {
		FontEntry Entry;
		boolean Bold;
		boolean Italic;

		Spec(FontEntry entry, boolean bold, boolean italic) {
			Entry = entry;
			Bold = bold;
			Italic = italic;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}

			if (!(other instanceof Spec)) {
				return false;
			}

			final Spec spec = (Spec)other;
			return Bold == spec.Bold && Italic == spec.Italic && Entry.equals(spec.Entry);
		}

		@Override
		public int hashCode() {
			return 4 * Entry.hashCode() + (Bold ? 2 : 0) + (Italic ? 1 : 0);
		}
	}

	private static final Map<Spec,Object> ourCachedEmbeddedTypefaces = new HashMap<Spec,Object>();
	private static final Object NULL_OBJECT = new Object();

	private static String alias(SystemInfo systemInfo, String family, boolean bold, boolean italic) {
		final StringBuilder builder = new StringBuilder(systemInfo.tempDirectory());
		builder.append("/");
		builder.append(family);
		if (bold) {
			builder.append("-bold");
		}
		if (italic) {
			builder.append("-italic");
		}
		return builder.append(".font").toString();
	}

	private static boolean copy(FileInfo from, String to) {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = ZLFile.createFileByPath(from.Path).getInputStream(from.EncryptionInfo);
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
		} catch (Exception e) {
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

	private static Typeface getOrCreateEmbeddedTypeface(SystemInfo systemInfo, FontEntry entry, boolean bold, boolean italic) {
		final Spec spec = new Spec(entry, bold, italic);
		Object cached = ourCachedEmbeddedTypefaces.get(spec);
		if (cached == null) {
			final FileInfo fileInfo = entry.fileInfo(bold, italic);
			if (fileInfo != null) {
				final String realFileName = alias(systemInfo, entry.Family, bold, italic);
				if (copy(fileInfo, realFileName)) {
					try {
						cached = Typeface.createFromFile(realFileName);
					} catch (Throwable t) {
						// ignore
					}
				}
				new File(realFileName).delete();
			}
			ourCachedEmbeddedTypefaces.put(spec, cached != null ? cached : NULL_OBJECT);
		}
		return cached instanceof Typeface ? (Typeface)cached : null;
	}

	private static Typeface embeddedTypeface(SystemInfo systemInfo, FontEntry entry, boolean bold, boolean italic) {
		{
			final int index = (bold ? 1 : 0) + (italic ? 2 : 0);
			final Typeface tf = getOrCreateEmbeddedTypeface(systemInfo, entry, bold, italic);
			if (tf != null) {
				return tf;
			}
		}
		for (int i = 0; i < 4; ++i) {
			final Typeface tf = getOrCreateEmbeddedTypeface(systemInfo, entry, (i & 1) == 1, (i & 2) == 2);
			if (tf != null) {
				return tf;
			}
		}
		return null;
	}

	public static void clearFontCache() {
		ourTypefaces.clear();
		ourFileSet = null;
		ourCachedEmbeddedTypefaces.clear();
	}
}
