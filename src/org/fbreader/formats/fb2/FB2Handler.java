package org.fbreader.formats.fb2;

import java.util.Stack;

import org.fbreader.bookmodel.BookModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

class FB2Handler extends DefaultHandler {
	private BookModel myBookModel;
	private ZLTextPlainModel myCurrentTextModel = null;
	private boolean myTextParagraphExists = false;
	private StringBuffer myBuffer = new StringBuffer();
	private Stack<Byte> myKindStack = new Stack<Byte>();
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	private boolean myInsidePoem = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private boolean myInsideTitle = false;
	private boolean mySectionContainsRegularContents = false;
	private int mySectionDepth = 0;
	private boolean mySectionStarted = false;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private void flushTextBufferToParagraph() {
		if (myBuffer.length() != 0) {
			myCurrentTextModel.addText(myBuffer);
			myBuffer.delete(0, myBuffer.length());
		}		
	}
	
	private void addControl(Byte kind, boolean start) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl((byte) kind, start);
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
		myCurrentTextModel.createParagraph(kind);
		for (Byte b : myKindStack) {
			myCurrentTextModel.addControl(b, true);
		}
		if (myHyperlinkReference != "") {
			myCurrentTextModel.addHyperlinkControl(myHyperlinkKind, myHyperlinkReference);
		}
		myTextParagraphExists = true;
	}
	
	private void endParagraph() {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myTextParagraphExists = false;
		}
	}
	
	private void insertEndParagraph(ZLTextParagraph.Kind kind) {
		if ((myCurrentTextModel != null) && mySectionContainsRegularContents) {
			int size = myCurrentTextModel.getParagraphsNumber();
			if ((size > 0) && (myCurrentTextModel.getParagraph(size-1).getKind() != kind)) {
				myCurrentTextModel.createParagraph(kind);
				mySectionContainsRegularContents = false;
			}
		}
	}
	
	private void insertEndOfSectionParagraph() {
		insertEndParagraph(ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}
	
	public FB2Handler(BookModel model) {
		myBookModel = model;
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
		
		case STANZA:
			beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
			endParagraph();
			popKind();
			break;
			
		case SECTION:
			if (myReadMainText) {
				--mySectionDepth;
				mySectionStarted = false;
			} else {
				myCurrentTextModel = null;
			}
			break;
		
		case ANNOTATION:
			popKind();
			if (myBodyCounter == 0) {
				insertEndOfSectionParagraph();
				myCurrentTextModel = null;
			}
			break;
		
		case TITLE:
			popKind();
			myInsideTitle = false;
			break;
			
		case BODY:
			myReadMainText = false;
			myCurrentTextModel = null;
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
				myCurrentTextModel = myBookModel.getFootnoteModel(id);
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
		
		case STANZA:
			pushKind((byte) tag.ordinal());
			beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
			endParagraph();
			break;
			
		case SECTION:
			if (myReadMainText) {
				insertEndOfSectionParagraph();
				++mySectionDepth;
				mySectionStarted  = true;
			}
			break;
		
		case ANNOTATION:
			if (myBodyCounter == 0) {
				myCurrentTextModel = myBookModel.getBookModel();
			}
			pushKind((byte) tag.ordinal());
			break;
		
		case TITLE:
			if (myInsidePoem) {
				pushKind((byte) FB2Tag.POEM.ordinal()); //плохо
			} else if (mySectionDepth == 0) {
				insertEndOfSectionParagraph();
				pushKind((byte) tag.ordinal());
			} else {
				pushKind((byte) FB2Tag.SECTION.ordinal()); //плохо
				myInsideTitle = true;
			}
			break;
			
		case BODY:
			++myBodyCounter;
			if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
				myCurrentTextModel = myBookModel.getBookModel();
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
		if (myTextParagraphExists) {
			myBuffer.append(String.valueOf(ch, start, length));
			if (!myInsideTitle) {
				mySectionContainsRegularContents = true;
			}
		}		
	}

}
