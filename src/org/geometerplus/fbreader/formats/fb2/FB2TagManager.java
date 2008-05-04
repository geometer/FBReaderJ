/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.fb2;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.xml.*;

abstract class FB2TagManager {
	private static final HashMap ourMap = new HashMap();

	static ArrayList humanReadableTags(String id) {
		if (ourMap.isEmpty()) {
			new FB2TagInfoReader().read(
				ZLibrary.JAR_DATA_PREFIX + "data/formats/fb2/fb2genres.xml"
			);
		}
		return (ArrayList)ourMap.get(id);
	}

	private FB2TagManager() {
	}

	private static class FB2TagInfoReader extends ZLXMLReaderAdapter {
		private final String myLanguage;
		private String myCategoryName;
		private String mySubCategoryName;
		private final ArrayList myGenreIds = new ArrayList();

		FB2TagInfoReader() {
			final String language = Locale.getDefault().getLanguage();
			myLanguage = ("ru".equals(language)) ? "ru" : "en";
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if ((tag == "subgenre") || (tag == "genre-alt")) {
				final String id = attributes.getValue("value");
				if (id != null) {
					myGenreIds.add(id);
				}
			} else if (tag == "root-descr") {
				if (myLanguage == attributes.getValue("lang")) {
					final String name = attributes.getValue("genre-title");
					if (name != null) {
						myCategoryName = name.trim();
					}
				}
			} else if (tag == "genre-descr") {
				if (myLanguage == attributes.getValue("lang")) {
					final String name = attributes.getValue("title");
					if (name != null) {
						mySubCategoryName = name.trim();
					}
				}
			}
			return false;
		}

		public boolean endElementHandler(String tag) {
			if (tag == "genre") {
				myCategoryName = null;
				mySubCategoryName = null;
				myGenreIds.clear();
			} else if (tag == "subgenre") {
				if ((myCategoryName != null) && (mySubCategoryName != null)) {
					final String fullTagName = myCategoryName + '/' + mySubCategoryName;
					final int len = myGenreIds.size();
					for (int i = 0; i < len; ++i) {
						final Object id = myGenreIds.get(i);
						ArrayList list = (ArrayList)ourMap.get(id);
						if (list == null) {
							list = new ArrayList();
							ourMap.put(id, list);
						}
						list.add(fullTagName);
					}
				}
				mySubCategoryName = null;
				myGenreIds.clear();
			}
			return false;
		}
	}
}
