package org.zlibrary.core.xml.sax;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.core.xml.ZLXMLReader;

class SAXHandler extends DefaultHandler {	
	private ZLXMLReader myXMLReader;
	private static final class StringMap extends HashMap<String,String> implements ZLXMLReader.StringMap {
		public int getSize() {
			return size();
		}

		public String getKey(int index) {
			int i = 0;
			for (String s : keySet()) {
				if (i++ == index) {
					return s;
				}
			}
			return null;
		}

		public String getValue(String key) {
			return get(key);
		}
	}
	private final StringMap myAttributes = new StringMap();
	
	SAXHandler(ZLXMLReader reader) {
		myXMLReader = reader;
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		myXMLReader.endElementHandler(qName);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		StringMap attributesMap = myAttributes;
		attributesMap.clear();
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			attributesMap.put(attributes.getQName(i), attributes.getValue(i));
		}
		myXMLReader.startElementHandler(qName, attributesMap);
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

	public String getValue(String key) {
		return myAttributes.get(key);
	}
}
