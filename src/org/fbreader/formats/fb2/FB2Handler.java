package org.fbreader.formats.fb2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class FB2Handler extends DefaultHandler {	
	private FB2Reader myFB2Reader;
	
	public FB2Handler(FB2Reader reader) {
		myFB2Reader = reader;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		myFB2Reader.endElementHandler(qName);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		int length = attributes.getLength()*2;
		String [] attrs = new String[length];
		for (int i = 0; i < length; i+=2) {
			attrs[i] = attributes.getQName(i/2);
			attrs[i+1] = attributes.getValue(i/2);
		}
		myFB2Reader.startElementHandler(qName, attrs);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		myFB2Reader.characterDataHandler(ch, start, length);	
	}

}
