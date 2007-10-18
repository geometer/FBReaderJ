package org.fbreader.formats.fb2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.impl.ZLModelFactory;

class FB2Handler extends DefaultHandler {
	private ZLTextModel myModel;
	private boolean myParagraphExists = false;
	private String myBuffer;
	
	public FB2Handler(ZLTextModel model) {
		myModel = model;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		FB2Tag tag;
		try {
			tag = FB2Tag.valueOf(qName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return;
		}		
		switch (tag) {
		case P:
			if (myParagraphExists) {
				myModel.addText(myBuffer);
				myParagraphExists = false;
			}			
			break;
			
		default:
			break;
		}		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		FB2Tag tag;
		try {
			tag = FB2Tag.valueOf(qName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return;
		}
		switch (tag) {
		case P:
			myModel.addParagraphInternal((new ZLModelFactory()).createParagraph());
			myParagraphExists = true;
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		myBuffer = String.valueOf(ch, start, length);
	}

}
