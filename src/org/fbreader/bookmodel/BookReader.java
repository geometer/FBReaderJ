package org.fbreader.bookmodel;

import java.util.ArrayList;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

public final class BookReader {
	private final BookModel myBookModel;
	private ZLTextPlainModel myCurrentTextModel = null;
	
	private boolean myTextParagraphExists = false;
	
	private final ZLTextBuffer myBuffer = new ZLTextBuffer();
	private final ZLTextBuffer myContentsBuffer = new ZLTextBuffer();

	private byte[] myKindStack = new byte[10];
	private int myKindStackSize = 0;
	
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	
	private boolean myInsideTitle = false;
	private boolean mySectionContainsRegularContents = false;
	
	private boolean myContentsParagraphExists = false;
	private final ArrayList<ZLTextTreeParagraph> myTOCStack = new ArrayList<ZLTextTreeParagraph>();
	private boolean myLastTOCParagraphIsEmpty = false;

	private final char[] PERIOD = "...".toCharArray();
	
	public BookReader(BookModel model) {
		myBookModel = model;
	}
	
	private void flushTextBufferToParagraph() {
		if (!myBuffer.isEmpty()) {
			myCurrentTextModel.addText(myBuffer);
			myBuffer.clear();
		}		
	}
	
	public void addControl(byte kind, boolean start) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl((byte) kind, start);
		}
		if (!start && (myHyperlinkReference.length() != 0) && (kind == myHyperlinkKind)) {
			myHyperlinkReference = "";
		}
	}
	
	public void pushKind(byte kind) {
		if (myKindStackSize == myKindStack.length) {
			byte[] newStack = new byte[myKindStackSize + 10];
			System.arraycopy(myKindStack, 0, newStack, 0, myKindStackSize);
		}
		myKindStack[myKindStackSize++] = kind;
	}
	
	public boolean popKind() {
		if (myKindStackSize > 0) {
			--myKindStackSize;
			return true;
		}
		return false;
	}
	
	public void beginParagraph(byte kind) {
		if (myCurrentTextModel != null) {
			myCurrentTextModel.createParagraph(kind);
			final int size = myKindStackSize;
			for (int i = 0; i < size; ++i) {
				myCurrentTextModel.addControl(myKindStack[i], true);
			}
			if (myHyperlinkReference.length() != 0) {
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
	
	private void insertEndParagraph(byte kind) {
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
	
	public void addData(char[] data) {
		addData(data, 0, data.length);
	}

	public void addData(char[] data, int offset, int length) {
		if (myTextParagraphExists) {
			myBuffer.append(data, offset, length);
			if (!myInsideTitle) {
				mySectionContainsRegularContents = true;
			} else {
				addContentsData(data, offset, length);
			}
		}	
	}
	
	public void addDataFinal(char[] data, int offset, int length) {
		if (!myBuffer.isEmpty()) {
			addData(data, offset, length);
		} else {
			if (myTextParagraphExists) {
				myCurrentTextModel.addText(data, offset, length);
				if (!myInsideTitle) {
					mySectionContainsRegularContents = true;
				} else {
					addContentsData(data, offset, length);
				}
			}	
		}	
	}
	
	public void addHyperlinkControl(byte kind, String label) {
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
	
	public void addContentsData(char[] data) {
		addContentsData(data, 0, data.length);
	}

	public void addContentsData(char[] data, int offset, int length) {
		if ((length != 0) && !myTOCStack.isEmpty()) {
			myContentsBuffer.append(data, offset, length);
		}
	}
	
	public void beginContentsParagraph(int referenceNumber) {
		final ArrayList<ZLTextTreeParagraph> tocStack = myTOCStack;
		if (myCurrentTextModel == myBookModel.getBookModel()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			if (referenceNumber == -1) {
				referenceNumber = myCurrentTextModel.getParagraphsNumber();
			}
			int size = tocStack.size();
			ZLTextTreeParagraph peek = (size == 0) ? null : tocStack.get(size - 1);
			if (!myContentsBuffer.isEmpty()) {
				contentsModel.addText(myContentsBuffer);
				myContentsBuffer.clear();
				myLastTOCParagraphIsEmpty = false;
			}
			if (myLastTOCParagraphIsEmpty) {
				contentsModel.addText(PERIOD);
			}
			ZLTextTreeParagraph para = contentsModel.createParagraph(peek);
			contentsModel.addControl(FBTextKind.CONTENTS_TABLE_ENTRY, true);
			contentsModel.setReference(para, referenceNumber);
			tocStack.add(para);
			myLastTOCParagraphIsEmpty = true;
			myContentsParagraphExists = true;
		}
	}

	public void endContentsParagraph() {
		if (!myTOCStack.isEmpty()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			if (!myContentsBuffer.isEmpty()) {
				contentsModel.addText(myContentsBuffer);
				myContentsBuffer.clear();
				myLastTOCParagraphIsEmpty = false;
			}
			if (myLastTOCParagraphIsEmpty) {
				contentsModel.addText(PERIOD);
				myLastTOCParagraphIsEmpty = false;
			}
			myTOCStack.remove(myTOCStack.size() - 1);
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

	public void addImageReference(String ref, short offset) {
		if (myCurrentTextModel != null) {
			mySectionContainsRegularContents = true;
			if (myTextParagraphExists) {
				flushTextBufferToParagraph();
				myCurrentTextModel.addImage(ref, myBookModel.getImageMap(), offset);
			} else {
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				myCurrentTextModel.addControl(FBTextKind.IMAGE, true);
				myCurrentTextModel.addImage(ref, myBookModel.getImageMap(), offset);
				myCurrentTextModel.addControl(FBTextKind.IMAGE, false);
				endParagraph();
			}
		}
		
	}

	public void addImage(String id, ZLImage image) {
		myBookModel.addImage(id, image);
	}
	
}
