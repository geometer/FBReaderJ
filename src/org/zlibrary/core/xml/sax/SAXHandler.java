package org.zlibrary.core.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.core.xml.ZLXMLReader;

class SAXHandler extends DefaultHandler {	
	private ZLXMLReader myXMLReader;
	
	SAXHandler(ZLXMLReader reader) {
		myXMLReader = reader;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		myXMLReader.endElementHandler(qName);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		int length = attributes.getLength()*2;
		String [] attrs = new String[length];
		for (int i = 0; i < length; i+=2) {
			attrs[i] = attributes.getQName(i/2);
			attrs[i+1] = attributes.getValue(i/2);
		}
		myXMLReader.startElementHandler(qName, attrs);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		myXMLReader.characterDataHandler(ch, start, length);	
	}
}
