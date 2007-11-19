package org.fbreader.bookmodel;

import java.util.Stack;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

public class BookReader {
	private BookModel myBookModel;
	private ZLTextPlainModel myCurrentTextModel = null;
	
	private boolean myTextParagraphExists = false;
	
	private StringBuffer myBuffer = new StringBuffer();
	private Stack<Byte> myKindStack = new Stack<Byte>();
	
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	
	private boolean myInsideTitle = false;
	private boolean mySectionContainsRegularContents = false;
	
	private boolean myContentsParagraphExists = false;
	private Stack<ZLTextTreeParagraph> myTOCStack = new Stack<ZLTextTreeParagraph>();
	private boolean myLastTOCParagraphIsEmpty = false;
	private StringBuffer myContentsBuffer = new StringBuffer();
	
	public BookReader(BookModel model) {
		myBookModel = model;
	}
	
	private void flushTextBufferToParagraph() {
		if (myBuffer.length() != 0) {
			myCurrentTextModel.addText(myBuffer);
			myBuffer.delete(0, myBuffer.length());
		}		
	}
	
	public void addControl(Byte kind, boolean start) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl((byte) kind, start);
		}
		if (!start && myHyperlinkReference != "" && (kind == myHyperlinkKind)) {
			myHyperlinkReference = "";
		}
	}
	
	public void pushKind(byte kind) {
		myKindStack.push(kind);
	}
	
	public boolean popKind() {
		if (!myKindStack.empty()) {
			myKindStack.pop();
			return true;
		}
		return false;
	}
	
	public void beginParagraph(ZLTextParagraph.Kind kind) {
		if (myCurrentTextModel != null) {
			myCurrentTextModel.createParagraph(kind);
			for (Byte b : myKindStack) {
				myCurrentTextModel.addControl(b, true);
			}
			if (myHyperlinkReference != "") {
				myCurrentTextModel.addHyperlinkControl(myHyperlinkKind, myHyperlinkReference);
			}
			myTextParagraphExists = true;
		}		
	}
	
	public void endParagraph() {
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
	
	public void insertEndOfSectionParagraph() {
		insertEndParagraph(ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}
	
	public void unsetCurrentTextModel() {
		myCurrentTextModel = null;
	}
	
	public void enterTitle() {
		myInsideTitle = true;
	}
	
	public void exitTitle() {
		myInsideTitle = false;
	}
	
	public void setMainTextModel() {
		myCurrentTextModel = myBookModel.getBookModel();
	}
	
	public void setFootnoteTextModel(String id) {
		myCurrentTextModel = myBookModel.getFootnoteModel(id);
	}
	
	public void addData(String data) {
		if (myTextParagraphExists) {
			myBuffer.append(data);
			if (!myInsideTitle) {
				mySectionContainsRegularContents = true;
			} else {
				addContentsData(data);
			}
		}	
	}
	
	public void addHyperlinkControl(Byte kind, String label) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addHyperlinkControl(kind, label);
		}
		myHyperlinkKind = kind;
		myHyperlinkReference = label;
	}
	
	public void addHyperlinkLabel(String label) {
		if (myCurrentTextModel != null) {
			int paragraphNumber = myCurrentTextModel.getParagraphsNumber();
			if (myTextParagraphExists) {
				--paragraphNumber;
			}
			myBookModel.addHyperlinkLabel(label, myCurrentTextModel, paragraphNumber);
		}
	}
	
	public void addContentsData(String data) {
		if (data != "" && !myTOCStack.empty()) {
			myContentsBuffer.append(data);
		}
	}
	
	public void beginContentsParagraph(int referenceNumber) {
		if (myCurrentTextModel == myBookModel.getBookModel()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			if (referenceNumber == -1) {
				referenceNumber = myCurrentTextModel.getParagraphsNumber();
			}
			ZLTextTreeParagraph peek = myTOCStack.empty() ? null : myTOCStack.peek();
			if (myContentsBuffer.length() != 0) {
				contentsModel.addText(myContentsBuffer);
				myContentsBuffer.delete(0, myContentsBuffer.length());
				myLastTOCParagraphIsEmpty = false;
			}
			if (myLastTOCParagraphIsEmpty) {
				contentsModel.addText("...");
			}
			ZLTextTreeParagraph para = contentsModel.createParagraph(peek);
			contentsModel.addControl((byte)ZLTextKind.CONTENTS_TABLE_ENTRY.ordinal(), true);
			contentsModel.setReference(para, referenceNumber);
			myTOCStack.push(para);
			myLastTOCParagraphIsEmpty = true;
			myContentsParagraphExists = true;
		}
	}

	public void endContentsParagraph() {
		if (!myTOCStack.empty()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			if (myContentsBuffer.length() != 0) {
				contentsModel.addText(myContentsBuffer);
				myContentsBuffer.delete(0, myContentsBuffer.length());
				myLastTOCParagraphIsEmpty = false;
			}
			if (myLastTOCParagraphIsEmpty) {
				contentsModel.addText("...");
				myLastTOCParagraphIsEmpty = false;
			}
			myTOCStack.pop();
		}
		myContentsParagraphExists = false;
	}

	public void setReference(int contentsParagraphNumber, int referenceNumber) {
		ContentsModel contentsModel = myBookModel.getContentsModel();
		if (contentsParagraphNumber >= contentsModel.getParagraphsNumber()) {
			return;
		}
		contentsModel.setReference((ZLTextTreeParagraph) 
				contentsModel.getParagraph(contentsParagraphNumber), referenceNumber);
	}
	
	public boolean contentsParagraphIsOpen() {
		return myContentsParagraphExists;
	}

	public void beginContentsParagraph() {
		beginContentsParagraph(-1);
	}
	
	public BookModel getModel() {
		return myBookModel;
	}
	
}
