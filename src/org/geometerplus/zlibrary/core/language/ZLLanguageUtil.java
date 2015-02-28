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

package org.geometerplus.zlibrary.core.language;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;

public abstract class ZLLanguageUtil {
	private static ArrayList<String> ourLanguageCodes = new ArrayList<String>();

	private ZLLanguageUtil() {
	}

	public static String defaultLanguageCode() {
		return Locale.getDefault().getLanguage();
	}

	public static List<String> languageCodes() {
		if (ourLanguageCodes.isEmpty()) {
			TreeSet<String> codes = new TreeSet<String>();
			for (ZLFile file : patternsFile().children()) {
				String name = file.getShortName();
				final int index = name.indexOf("_");
				if (index != -1) {
					String str = name.substring(0, index);
					if (!codes.contains(str)) {
						codes.add(str);
					}
				}
			}
			codes.add("id");
			codes.add("de-traditional");

			ourLanguageCodes.addAll(codes);
		}

		return Collections.unmodifiableList(ourLanguageCodes);
	}

	public static ZLFile patternsFile() {
		return ZLResourceFile.createResourceFile("languagePatterns");
	}
}
