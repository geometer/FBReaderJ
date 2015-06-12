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

package org.geometerplus.zlibrary.core.resources;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

abstract public class ZLResource {
	public final String Name;

	private static final List<String> ourLanguageCodes = new LinkedList<String>();
	public static List<String> languageCodes() {
		synchronized (ourLanguageCodes) {
			if (ourLanguageCodes.isEmpty()) {
				final ZLFile dir = ZLResourceFile.createResourceFile("resources/application");
				final List<ZLFile> children = dir.children();
				for (ZLFile file : children) {
					final String name = file.getShortName();
					final String postfix = ".xml";
					if (name.endsWith(postfix) && !"neutral.xml".equals(name)) {
						ourLanguageCodes.add(name.substring(0, name.length() - postfix.length()));
					}
				}
			}
		}
		return Collections.unmodifiableList(ourLanguageCodes);
	}

	public static List<Language> interfaceLanguages() {
		final List<Language> allLanguages = new LinkedList<Language>();
		final ZLResource resource = ZLResource.resource("language-self");
		for (String c : languageCodes()) {
			allLanguages.add(new Language(c, resource));
		}
		Collections.sort(allLanguages);
		allLanguages.add(0, new Language(Language.SYSTEM_CODE));
		return allLanguages;
	}

	private static final ZLStringOption ourLanguageOption =
		new ZLStringOption("LookNFeel", "Language", Language.SYSTEM_CODE);
	public static ZLStringOption getLanguageOption() {
		return ourLanguageOption;
	}
	public static String getLanguage() {
		final String lang = getLanguageOption().getValue();
		return Language.SYSTEM_CODE.equals(lang) ? Locale.getDefault().getLanguage() : lang;
	}

	public static ZLResource resource(String key) {
		ZLTreeResource.buildTree();
		if (ZLTreeResource.ourRoot == null) {
			return ZLMissingResource.Instance;
		}
		try {
			return ZLTreeResource.ourRoot.getResource(key);
		} finally {
		}
	}

	protected ZLResource(String name) {
		Name = name;
	}

	abstract public boolean hasValue();
	abstract public String getValue();
	abstract public String getValue(int condition);
	abstract public ZLResource getResource(String key);
}
