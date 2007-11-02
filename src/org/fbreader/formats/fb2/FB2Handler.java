package org.fbreader.formats.fb2;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

class FB2Handler extends DefaultHandler {
	private ZLTextPlainModel myModel;
	private boolean myTextParagraphExists = false;
	private String myBuffer = "";
	private Stack<Byte> myKindStack = new Stack<Byte>();
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	private boolean myInsidePoem = false;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private void flushTextBufferToParagraph() {
		if (myBuffer != "") {
			myModel.addText(myBuffer);
			myBuffer = "";
		}		
	}
	
	private void addControl(Byte kind, boolean start) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myModel.addControl((byte) kind, start);
		}
		if (!start && myHyperlinkReference != "" && (kind == myHyperlinkKind)) {
			myHyperlinkReference = "";
		}
	}
	
	private void pushKind(byte kind) {
		myKindStack.push(kind);
	}
	
	private boolean popKind() {
		if (!myKindStack.empty()) {
			myKindStack.pop();
			return true;
		}
		return false;
	}
	
	private void beginParagraph(ZLTextParagraph.Kind kind) {
		myModel.createParagraph(kind);
		for (Byte b : myKindStack) {
			myModel.addControl(b, true);
		}
		if (myHyperlinkReference != "") {
			myModel.addHyperlinkControl(myHyperlinkKind, myHyperlinkReference);
		}
		myTextParagraphExists = true;
	}
	
	private void endParagraph() {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myTextParagraphExists = false;
		}
	}
	
	public FB2Handler(ZLTextPlainModel model) {
		myModel = model;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		FB2Tag tag;
		try {
			tag = getTag(qName);
		} catch (IllegalArgumentException e) {
			return;
		}		
		switch (tag) {
		case P:
			endParagraph();		
			break;
			
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			addControl((byte) tag.ordinal(), false);
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			popKind();
			endParagraph();
			break;	
		
		case CITE:
		case EPIGRAPH:
			popKind();
			break;	
		
		case POEM:
			myInsidePoem = false;
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
			tag = getTag(qName);
		} catch (IllegalArgumentException e) {
			return;
		}
		switch (tag) {
		case P:
			beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			addControl((byte) tag.ordinal(), true);		
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			pushKind((byte) tag.ordinal());
			beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case EMPTY_LINE:
			beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
			endParagraph();
			break;
		
		case CITE:
		case EPIGRAPH:
			pushKind((byte) tag.ordinal());
			break;
		
		case POEM:
			myInsidePoem = true;
			break;	
			
		default:
			break;
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		if (myTextParagraphExists) {
			myBuffer = String.valueOf(ch, start, length);
		}		
	}

}
