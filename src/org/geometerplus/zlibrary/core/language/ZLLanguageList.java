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

package org.geometerplus.zlibrary.core.language;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class ZLLanguageList {
	private static ArrayList<String> ourLanguageCodes = new ArrayList<String>();

	private ZLLanguageList() {
	}
	
	public static List<String> languageCodes() {
		if (ourLanguageCodes.isEmpty()) {
			TreeSet<String> codes = new TreeSet<String>();
			for (ZLFile file : patternsFile().children()) {
				String name = file.getName(false);
				final int index = name.indexOf("_");
				if (index != -1) {
					String str = name.substring(0, index);
					if (!codes.contains(str)) {
					    codes.add(str);
					}
				}
			}

			ourLanguageCodes.addAll(codes);
		}

		return Collections.unmodifiableList(ourLanguageCodes);
	}
	
	public static String languageName(String code) {
		return ZLResource.resource("language").getResource(code).getValue();
	}

	public	static ZLFile patternsFile() {
		return ZLResourceFile.createResourceFile("data/languagePatterns.tar");
	}
}
