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

package org.geometerplus.zlibrary.core.encodings;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Xml;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

abstract class FilteredEncodingCollection extends EncodingCollection {
	private final List<Encoding> myEncodings = new ArrayList<Encoding>();
	private final Map<String,Encoding> myEncodingByAlias = new HashMap<String,Encoding>();

	FilteredEncodingCollection() {
		try {
			final ZLResourceFile file = ZLResourceFile.createResourceFile("encodings/Encodings.xml");
			Xml.parse(file.getInputStream(), Xml.Encoding.UTF_8, new EncodingCollectionReader());
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	private class EncodingCollectionReader extends DefaultHandler {
		private String myCurrentFamilyName;
		private Encoding myCurrentEncoding;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("group".equals(localName)) {
				myCurrentFamilyName = attributes.getValue("name");
			} else if ("encoding".equals(localName)) {
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
			} else if ("code".equals(localName)) {
				if (myCurrentEncoding != null) {
					myEncodingByAlias.put(attributes.getValue("number"), myCurrentEncoding);
				}
			} else if ("alias".equals(localName)) {
				if (myCurrentEncoding != null) {
					myEncodingByAlias.put(attributes.getValue("name").toLowerCase(), myCurrentEncoding);
				}
			}
		}
	}
}
