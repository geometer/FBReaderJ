package org.fbreader.formats.fb2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

class FB2Handler extends DefaultHandler {
	private ZLTextModel myModel;
	private boolean myParagraphExists = false;
	private String myBuffer = "";
	
	private void flushTextBufferToParagraph() {
		if (myBuffer != "") {
			myModel.addText(myBuffer);
			myBuffer = "";
		}		
	}
	
	private void addControl(Byte tag, boolean start) {
		if (myParagraphExists) {
			flushTextBufferToParagraph();
			myModel.addControl((byte) tag, start);
		}	
	}
	
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
				flushTextBufferToParagraph();
				myParagraphExists = false;
			}			
			break;
			
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			addControl((byte) tag.ordinal(), false);
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
		
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			addControl((byte) tag.ordinal(), true);		
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		if (myParagraphExists) {
			myBuffer = String.valueOf(ch, start, length);
		}		
	}

}
