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

package org.geometerplus.zlibrary.core.xml.sax;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.geometerplus.zlibrary.core.xml.*;

class SAXHandler extends DefaultHandler {	
	private ZLXMLReader myXMLReader;
	private final ZLStringMap myAttributes = new ZLStringMap();
	
	SAXHandler(ZLXMLReader reader) {
		myXMLReader = reader;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		myXMLReader.endElementHandler(qName);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		ZLStringMap attributesMap = myAttributes;
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			attributesMap.put(attributes.getQName(i), attributes.getValue(i));
		}
		myXMLReader.startElementHandler(qName, attributesMap);
		attributesMap.clear();
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		myXMLReader.characterDataHandler(ch, start, length);	
	}

	public void endDocument() throws SAXException {
		myXMLReader.endDocumentHandler();
	}

	public void startDocument() throws SAXException {
		myXMLReader.startDocumentHandler();
	}
}
