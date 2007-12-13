package org.zlibrary.core.xml.sax;

import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.core.xml.ZLXMLReader;

class SAXHandler extends DefaultHandler {	
	private ZLXMLReader myXMLReader;
	private final TreeMap<String, String> myAttributes = new TreeMap<String, String>();
	
	SAXHandler(ZLXMLReader reader) {
		myXMLReader = reader;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		myXMLReader.endElementHandler(qName);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		myAttributes.clear();
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			myAttributes.put(attributes.getQName(i), attributes.getValue(i));
		}
		myXMLReader.startElementHandler(qName, myAttributes);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		myXMLReader.characterDataHandler(ch, start, length);	
	}

	@Override
	public void endDocument() throws SAXException {
		myXMLReader.endDocumentHandler();
	}

	@Override
	public void startDocument() throws SAXException {
		myXMLReader.startDocumentHandler();
	}
}
