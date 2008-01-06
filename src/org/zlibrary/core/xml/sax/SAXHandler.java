package org.zlibrary.core.xml.sax;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.core.xml.*;

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
