package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.text.model.ZLTextParagraph;

class FB2Handler extends DefaultHandler {
	private BookReader myBookReader;
	
	private boolean myInsidePoem = false;
	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private int mySectionDepth = 0;
	private boolean mySectionStarted = false;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	
	public FB2Handler(BookModel model) {
		myBookReader = new BookReader(model);
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
			myBookReader.endParagraph();		
			break;
			
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			myBookReader.addControl((byte) tag.ordinal(), false);
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			myBookReader.popKind();
			myBookReader.endParagraph();
			break;	
		
		case CITE:
		case EPIGRAPH:
			myBookReader.popKind();
			break;	
		
		case POEM:
			myInsidePoem = false;
			break;
		
		case STANZA:
			myBookReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
			myBookReader.endParagraph();
			myBookReader.popKind();
			break;
			
		case SECTION:
			if (myReadMainText) {
				--mySectionDepth;
				mySectionStarted = false;
			} else {
				myBookReader.unsetCurrentTextModel();
			}
			break;
		
		case ANNOTATION:
			myBookReader.popKind();
			if (myBodyCounter == 0) {
				myBookReader.insertEndOfSectionParagraph();
				myBookReader.unsetCurrentTextModel();
			}
			break;
		
		case TITLE:
			myBookReader.popKind();
			myBookReader.exitTitle();
			myInsideTitle = false;
			break;
			
		case BODY:
			myReadMainText = false;
			myBookReader.unsetCurrentTextModel();
			break;
			
		default:
			break;
		}		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		String id = attributes.getValue("id");
		if (id != null) {
			if (!myReadMainText) {
				myBookReader.setFootnoteTextModel(id);
			}
	//		myModelReader.addHyperlinkLabel(id);
		}
		FB2Tag tag;
		try {
			tag = getTag(qName);
		} catch (IllegalArgumentException e) {
			return;
		}
		switch (tag) {
		case P:
			if (mySectionStarted) {
				mySectionStarted = false;
			} else if (myInsideTitle) {
				//
			}
			myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			myBookReader.addControl((byte) tag.ordinal(), true);		
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			myBookReader.pushKind((byte) tag.ordinal());
			myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case EMPTY_LINE:
			myBookReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
			myBookReader.endParagraph();
			break;
		
		case CITE:
		case EPIGRAPH:
			myBookReader.pushKind((byte) tag.ordinal());
			break;
		
		case POEM:
			myInsidePoem = true;
			break;	
		
		case STANZA:
			myBookReader.pushKind((byte) tag.ordinal());
			myBookReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
			myBookReader.endParagraph();
			break;
			
		case SECTION:
			if (myReadMainText) {
				myBookReader.insertEndOfSectionParagraph();
				++mySectionDepth;
				mySectionStarted  = true;
			}
			break;
		
		case ANNOTATION:
			if (myBodyCounter == 0) {
				myBookReader.setMainTextModel();
			}
			myBookReader.pushKind((byte) tag.ordinal());
			break;
		
		case TITLE:
			if (myInsidePoem) {
				myBookReader.pushKind((byte) FB2Tag.POEM.ordinal()); //плохо
			} else if (mySectionDepth == 0) {
				myBookReader.insertEndOfSectionParagraph();
				myBookReader.pushKind((byte) tag.ordinal());
			} else {
				myBookReader.pushKind((byte) FB2Tag.SECTION.ordinal()); //плохо
				myInsideTitle = true;
				myBookReader.enterTitle();
			}
			break;
			
		case BODY:
			++myBodyCounter;
			if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
				myBookReader.setMainTextModel();
				myReadMainText = true;
			}
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		myBookReader.addData(String.valueOf(ch, start, length));	
	}

}
