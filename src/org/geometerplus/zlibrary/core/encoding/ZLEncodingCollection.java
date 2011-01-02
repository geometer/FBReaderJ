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

package org.geometerplus.zlibrary.core.encoding;

import java.util.HashMap;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public final class ZLEncodingCollection {
	private static ZLEncodingCollection ourInstance;
	
	public static ZLEncodingCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new ZLEncodingCollection();
		}
		return ourInstance;
	}

	private final HashMap<String,String> myEncodingByAlias = new HashMap<String,String>();

	private ZLEncodingCollection() {
		new ZLEncodingCollectionReader().read(
			ZLResourceFile.createResourceFile("encodings/Encodings.xml")
		);
	}
	
	public String getEncodingName(String alias) {
		final String name = myEncodingByAlias.get(alias);
		return (name != null) ? name : alias;
	}

	public String getEncodingName(int code) {
		return myEncodingByAlias.get("" + code);
	}

	private class ZLEncodingCollectionReader extends ZLXMLReaderAdapter {
		private String myCurrentEncodingName;

		public ZLEncodingCollectionReader() {
		}

		public boolean dontCacheAttributeValues() {
			return true;
		}

		private static final String ENCODING = "encoding";
		private static final String NAME = "name";
		private static final String ALIAS = "alias";
		private static final String CODE = "code";
		private static final String NUMBER = "number";

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if (ENCODING == tag) {
				myCurrentEncodingName = attributes.getValue(NAME);
			} else if (myCurrentEncodingName != null) {
				String alias = null;
				if (ALIAS == tag) {
					alias = attributes.getValue(NAME);
				} else if (CODE == tag) {
					alias = attributes.getValue(NUMBER);
				}
				if (alias != null) {
					myEncodingByAlias.put(alias, myCurrentEncodingName);
				}
			}
			return false;
		}

		public boolean endElementHandler(String tag) {
			if (ENCODING == tag) {
				myCurrentEncodingName = null;
			}
			return false;
		}
	}
}
