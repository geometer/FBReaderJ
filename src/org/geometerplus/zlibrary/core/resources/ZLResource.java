/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

	private static final List<Language> ourLanguages = new LinkedList<Language>();
	public static List<Language> languages() {
		if (ourLanguages.isEmpty()) {
			final ZLResource resource = ZLResource.resource("language-self");
			final ZLFile dir = ZLResourceFile.createResourceFile("resources/application");
			for (ZLFile file : dir.children()) {
				final String name = file.getShortName();
				if (name.endsWith(".xml")) {
					final String code = name.substring(0, name.length() - 4);
					ourLanguages.add(new Language(code, resource.getResource(code).getValue()));
				}
			}
			Collections.sort(ourLanguages);
		}
		final List<Language> allLanguages = new ArrayList<Language>(ourLanguages.size() + 1);
		allLanguages.add(new Language(Language.SYSTEM_CODE));
		allLanguages.addAll(ourLanguages);
		return allLanguages;
	}

	public static ZLStringOption getLanguageOption() {
		return new ZLStringOption("LookNFeel", "Language", Language.SYSTEM_CODE);
	}

	public static ZLResource resource(String key) {
		ZLTreeResource.buildTree();
		if (ZLTreeResource.ourRoot == null) {
			return ZLMissingResource.Instance;
		}
		return ZLTreeResource.ourRoot.getResource(key);
	}

	protected ZLResource(String name) {
		Name = name;
	}

	abstract public boolean hasValue();
	abstract public String getValue();
	abstract public String getValue(int condition);
	abstract public ZLResource getResource(String key);
}
