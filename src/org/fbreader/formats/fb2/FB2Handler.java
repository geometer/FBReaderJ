package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.text.model.ZLTextParagraph;

class FB2Handler extends DefaultHandler {	
	private BookReader myModelReader;
	
	private boolean myInsidePoem = false;
	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private int mySectionDepth = 0;
	private boolean mySectionStarted = false;
	
	private byte myHyperlinkType;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private String reference(Attributes attributes) {
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			if (attributes.getQName(i).endsWith(":href")) {
				return attributes.getValue(i);
			}
		}
		return "";
	}
	
	public FB2Handler(BookModel model) {
		myModelReader = new BookReader(model);
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
			myModelReader.endParagraph();		
			break;
			
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			myModelReader.addControl((byte) tag.ordinal(), false);
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			myModelReader.popKind();
			myModelReader.endParagraph();
			break;	
		
		case CITE:
		case EPIGRAPH:
			myModelReader.popKind();
			break;	
		
		case POEM:
			myInsidePoem = false;
			break;
		
		case STANZA:
			myModelReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
			myModelReader.endParagraph();
			myModelReader.popKind();
			break;
			
		case SECTION:
			if (myReadMainText) {
				myModelReader.endContentsParagraph();
				--mySectionDepth;
				mySectionStarted = false;
			} else {
				myModelReader.unsetCurrentTextModel();
			}
			break;
		
		case ANNOTATION:
			myModelReader.popKind();
			if (myBodyCounter == 0) {
				myModelReader.insertEndOfSectionParagraph();
				myModelReader.unsetCurrentTextModel();
			}
			break;
		
		case TITLE:
			myModelReader.popKind();
			myModelReader.exitTitle();
			myInsideTitle = false;
			break;
			
		case BODY:
			myModelReader.popKind();
			myReadMainText = false;
			myModelReader.unsetCurrentTextModel();
			break;
		
		case A:
			myModelReader.addControl(myHyperlinkType, false);
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
				myModelReader.setFootnoteTextModel(id);
			}
			myModelReader.addHyperlinkLabel(id);
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
				myModelReader.addContentsData(" ");
			}
			myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case SUB:
		case SUP:
		case CODE:
		case EMPHASIS:
		case STRONG:
		case STRIKETHROUGH:
			myModelReader.addControl((byte) tag.ordinal(), true);		
			break;
		
		case V:
		case SUBTITLE:
		case TEXT_AUTHOR:
		case DATE:
			myModelReader.pushKind((byte) tag.ordinal());
			myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			break;
		
		case EMPTY_LINE:
			myModelReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
			myModelReader.endParagraph();
			break;
		
		case CITE:
		case EPIGRAPH:
			myModelReader.pushKind((byte) tag.ordinal());
			break;
		
		case POEM:
			myInsidePoem = true;
			break;	
		
		case STANZA:
			myModelReader.pushKind((byte) tag.ordinal());
			myModelReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
			myModelReader.endParagraph();
			break;
			
		case SECTION:
			if (myReadMainText) {
				myModelReader.insertEndOfSectionParagraph();
				++mySectionDepth;
				myModelReader.beginContentsParagraph();
				mySectionStarted  = true;
			}
			break;
		
		case ANNOTATION:
			if (myBodyCounter == 0) {
				myModelReader.setMainTextModel();
			}
			myModelReader.pushKind((byte) tag.ordinal());
			break;
		
		case TITLE:
			if (myInsidePoem) {
				myModelReader.pushKind((byte) FB2Tag.POEM.ordinal()); //плохо
			} else if (mySectionDepth == 0) {
				myModelReader.insertEndOfSectionParagraph();
				myModelReader.pushKind((byte) tag.ordinal());
			} else {
				myModelReader.pushKind((byte) FB2Tag.SECTION.ordinal()); //плохо
				myInsideTitle = true;
				myModelReader.enterTitle();
			}
			break;
			
		case BODY:
			++myBodyCounter;
			if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
				myModelReader.setMainTextModel();
				myReadMainText = true;
			}
			myModelReader.pushKind((byte) FB2Tag.BODY.ordinal());
			break;
		
		case A:
			String ref = reference(attributes);
			if (ref != "") {
				if (ref.charAt(0) == '#') {
					myHyperlinkType = (byte) FB2Tag.FOOTNOTE.ordinal();
					ref = ref.substring(1);
				} else {
					myHyperlinkType = (byte) FB2Tag.A.ordinal();
				}
				myModelReader.addHyperlinkControl(myHyperlinkType, ref);
			} else {
				myHyperlinkType = (byte) FB2Tag.FOOTNOTE.ordinal();
				myModelReader.addControl(myHyperlinkType, true);
			}
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		myModelReader.addData(String.valueOf(ch, start, length));	
	}

}
