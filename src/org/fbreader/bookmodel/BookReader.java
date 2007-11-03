package org.fbreader.bookmodel;

import java.util.Stack;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

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
	
	public BookReader(BookModel model) {
		myBookModel = model;
	}
	
	public void flushTextBufferToParagraph() {
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
		myCurrentTextModel.createParagraph(kind);
		for (Byte b : myKindStack) {
			myCurrentTextModel.addControl(b, true);
		}
		if (myHyperlinkReference != "") {
			myCurrentTextModel.addHyperlinkControl(myHyperlinkKind, myHyperlinkReference);
		}
		myTextParagraphExists = true;
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
			}
		}	
	}
}
