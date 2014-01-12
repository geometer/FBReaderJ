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

package org.geometerplus.zlibrary.core.encodings;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

abstract class FilteredEncodingCollection extends EncodingCollection {
	private final List<Encoding> myEncodings = new ArrayList<Encoding>();
	private final Map<String,Encoding> myEncodingByAlias = new HashMap<String,Encoding>();

	FilteredEncodingCollection() {
		new EncodingCollectionReader().readQuietly(
			ZLResourceFile.createResourceFile("encodings/Encodings.xml")
		);
	}

	public abstract boolean isEncodingSupported(String name);

	@Override
	public List<Encoding> encodings() {
		return Collections.unmodifiableList(myEncodings);
	}

	@Override
	public Encoding getEncoding(String alias) {
		Encoding e = myEncodingByAlias.get(alias);
		if (e == null && isEncodingSupported(alias)) {
			e = new Encoding(null, alias, alias);
			myEncodingByAlias.put(alias, e);
			myEncodings.add(e);
		}
		return e;
	}

	@Override
	public Encoding getEncoding(int code) {
		return getEncoding(String.valueOf(code));
	}

	public boolean providesConverterFor(String alias) {
		return myEncodingByAlias.containsKey(alias) || isEncodingSupported(alias);
	}

	private class EncodingCollectionReader extends ZLXMLReaderAdapter {
		private String myCurrentFamilyName;
		private Encoding myCurrentEncoding;

		@Override
		public boolean dontCacheAttributeValues() {
			return true;
		}

		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if ("group".equals(tag)) {
				myCurrentFamilyName = attributes.getValue("name");
			} else if ("encoding".equals(tag)) {
				final String name = attributes.getValue("name").toLowerCase();
				final String region = attributes.getValue("region");
				if (isEncodingSupported(name)) {
					myCurrentEncoding = new Encoding(
						myCurrentFamilyName, name, name + " (" + region + ")"
					);
					myEncodings.add(myCurrentEncoding);
					myEncodingByAlias.put(name, myCurrentEncoding);
				} else {
					myCurrentEncoding = null;
				}
			} else if ("code".equals(tag)) {
				if (myCurrentEncoding != null) {
					myEncodingByAlias.put(attributes.getValue("number"), myCurrentEncoding);
				}
			} else if ("alias".equals(tag)) {
				if (myCurrentEncoding != null) {
					myEncodingByAlias.put(attributes.getValue("name").toLowerCase(), myCurrentEncoding);
				}
			}
			return false;
		}
	}
}
