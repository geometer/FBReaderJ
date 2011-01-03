/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.library.Tag;

abstract class FB2TagManager {
	private static final HashMap<String,ArrayList<Tag>> ourMap = new HashMap<String,ArrayList<Tag>>();

	static ArrayList<Tag> humanReadableTags(String id) {
		if (ourMap.isEmpty()) {
			new FB2TagInfoReader().read(
				ZLResourceFile.createResourceFile("formats/fb2/fb2genres.xml")
			);
		}
		return ourMap.get(id);
	}

	private FB2TagManager() {
	}

	private static class FB2TagInfoReader extends ZLXMLReaderAdapter {
		private final String myLanguage;
		private Tag myCategoryTag;
		private Tag mySubCategoryTag;
		private final ArrayList<String> myGenreIds = new ArrayList<String>();

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
					myCategoryTag = Tag.getTag(null, attributes.getValue("genre-title"));
				}
			} else if (tag == "genre-descr") {
				if (myLanguage == attributes.getValue("lang")) {
					mySubCategoryTag = Tag.getTag(myCategoryTag, attributes.getValue("title"));
				}
			}
			return false;
		}

		public boolean endElementHandler(String tag) {
			if (tag == "genre") {
				myCategoryTag = null;
				mySubCategoryTag = null;
				myGenreIds.clear();
			} else if (tag == "subgenre") {
				if (mySubCategoryTag != null) {
					for (String id : myGenreIds) {
						ArrayList<Tag> list = ourMap.get(id);
						if (list == null) {
							list = new ArrayList<Tag>();
							ourMap.put(id, list);
						}
						list.add(mySubCategoryTag);
					}
				}
				mySubCategoryTag = null;
				myGenreIds.clear();
			}
			return false;
		}
	}
}
