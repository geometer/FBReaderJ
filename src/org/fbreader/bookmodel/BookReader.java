package org.fbreader.bookmodel;

import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.util.ZLArrayUtils;
import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.impl.ZLTextPlainModelImpl;

public class BookReader {
	private final BookModel myBookModel;
	private ZLTextPlainModelImpl myCurrentTextModel = null;
	
	private boolean myTextParagraphExists = false;
	
	private final ZLTextBuffer myBuffer = new ZLTextBuffer();
	private final ZLTextBuffer myContentsBuffer = new ZLTextBuffer();

	private byte[] myKindStack = new byte[20];
	private int myKindStackSize;
	
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	
	private boolean myInsideTitle = false;
	private boolean mySectionContainsRegularContents = false;
	
	private boolean myContentsParagraphExists = false;
	private final ArrayList myTOCStack = new ArrayList();
	private boolean myLastTOCParagraphIsEmpty = false;

	private final char[] PERIOD = "...".toCharArray();
	
	public BookReader(BookModel model) {
		myBookModel = model;
	}
	
	private final void flushTextBufferToParagraph() {
		final ZLTextBuffer buffer = myBuffer;
		if (!buffer.isEmpty()) {
			myCurrentTextModel.addText(buffer);
			buffer.clear();
		}		
	}
	
	public final void addControl(byte kind, boolean start) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl(kind, start);
		}
		if (!start && (myHyperlinkReference.length() != 0) && (kind == myHyperlinkKind)) {
			myHyperlinkReference = "";
		}
	}
	
	public final void pushKind(byte kind) {
		byte[] stack = myKindStack;
		if (stack.length == myKindStackSize) {
			stack = ZLArrayUtils.createCopy(stack, myKindStackSize, myKindStackSize << 1);
			myKindStack = stack;
		}
		stack[myKindStackSize++] = kind;
	}
	
	public final void popKind() {
		if (myKindStackSize != 0) {
			--myKindStackSize;
		}
	}
	
	public final void beginParagraph(byte kind) {
		final ZLTextPlainModelImpl textModel = myCurrentTextModel;
		if (textModel != null) {
			textModel.createParagraph(kind);
			final byte[] stack = myKindStack;
			final int size = myKindStackSize;
			for (int i = 0; i < size; ++i) {
				textModel.addControl(stack[i], true);
			}
			if (myHyperlinkReference.length() != 0) {
				textModel.addHyperlinkControl(myHyperlinkKind, myHyperlinkReference);
			}
			myTextParagraphExists = true;
		}		
	}
	
	public final void endParagraph() {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myTextParagraphExists = false;
		}
	}
	
	private final void insertEndParagraph(byte kind) {
		final ZLTextPlainModelImpl textModel = myCurrentTextModel;
		if ((textModel != null) && mySectionContainsRegularContents) {
			int size = textModel.getParagraphsNumber();
			if ((size > 0) && (textModel.getParagraph(size-1).getKind() != kind)) {
				textModel.createParagraph(kind);
				mySectionContainsRegularContents = false;
			}
		}
	}
	
	public final void insertEndOfSectionParagraph() {
		insertEndParagraph(ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}
	
	public final void unsetCurrentTextModel() {
		myCurrentTextModel = null;
	}
	
	public final void enterTitle() {
		myInsideTitle = true;
	}
	
	public final void exitTitle() {
		myInsideTitle = false;
	}
	
	public final void setMainTextModel() {
		myCurrentTextModel = myBookModel.getBookTextModel();
	}
	
	public final void setFootnoteTextModel(String id) {
		myCurrentTextModel = myBookModel.getFootnoteModel(id);
	}
	
	public final void addData(char[] data) {
		addData(data, 0, data.length);
	}

	public final void addData(char[] data, int offset, int length) {
		if (myTextParagraphExists) {
			myBuffer.append(data, offset, length);
			if (!myInsideTitle) {
				mySectionContainsRegularContents = true;
			} else {
				addContentsData(data, offset, length);
			}
		}	
	}
	
	public final void addDataFinal(char[] data, int offset, int length) {
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
	
	public final void addHyperlinkControl(byte kind, String label) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addHyperlinkControl(kind, label);
		}
		myHyperlinkKind = kind;
		myHyperlinkReference = label;
	}
	
	public final void addHyperlinkLabel(String label) {
		final ZLTextPlainModelImpl textModel = myCurrentTextModel;
		if (textModel != null) {
			int paragraphNumber = textModel.getParagraphsNumber();
			if (myTextParagraphExists) {
				--paragraphNumber;
			}
			myBookModel.addHyperlinkLabel(label, textModel, paragraphNumber);
		}
	}
	
	public final void addContentsData(char[] data) {
		addContentsData(data, 0, data.length);
	}

	public final void addContentsData(char[] data, int offset, int length) {
		if ((length != 0) && !myTOCStack.isEmpty()) {
			myContentsBuffer.append(data, offset, length);
		}
	}
	
	public final void beginContentsParagraph(int referenceNumber) {
		final ZLTextPlainModelImpl textModel = myCurrentTextModel;
		final ArrayList tocStack = myTOCStack;
		if (textModel == myBookModel.getBookTextModel()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			if (referenceNumber == -1) {
				referenceNumber = textModel.getParagraphsNumber();
			}
			int size = tocStack.size();
			ZLTextTreeParagraph peek = (size != 0) ? (ZLTextTreeParagraph)tocStack.get(size - 1) : null;
			final ZLTextBuffer contentsBuffer = myContentsBuffer;
			if (!contentsBuffer.isEmpty()) {
				contentsModel.addText(contentsBuffer);
				contentsBuffer.clear();
				myLastTOCParagraphIsEmpty = false;
			} else if (myLastTOCParagraphIsEmpty) {
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

	public final void endContentsParagraph() {
		final ArrayList tocStack = myTOCStack;
		if (!tocStack.isEmpty()) {
			ContentsModel contentsModel = myBookModel.getContentsModel();
			final ZLTextBuffer contentsBuffer = myContentsBuffer;
			if (!contentsBuffer.isEmpty()) {
				contentsModel.addText(contentsBuffer);
				contentsBuffer.clear();
				myLastTOCParagraphIsEmpty = false;
			} else if (myLastTOCParagraphIsEmpty) {
				contentsModel.addText(PERIOD);
				myLastTOCParagraphIsEmpty = false;
			}
			tocStack.remove(tocStack.size() - 1);
		}
		myContentsParagraphExists = false;
	}

	public final void setReference(int contentsParagraphNumber, int referenceNumber) {
		ContentsModel contentsModel = myBookModel.getContentsModel();
		if (contentsParagraphNumber < contentsModel.getParagraphsNumber()) {
			contentsModel.setReference(
				contentsModel.getTreeParagraph(contentsParagraphNumber), referenceNumber
			);
		}
	}
	
	public final boolean contentsParagraphIsOpen() {
		return myContentsParagraphExists;
	}

	public final void beginContentsParagraph() {
		beginContentsParagraph(-1);
	}
	
	public final BookModel getModel() {
		return myBookModel;
	}

	public final void addImageReference(String ref, short offset) {
		final ZLTextPlainModelImpl textModel = myCurrentTextModel;
		if (textModel != null) {
			mySectionContainsRegularContents = true;
			if (myTextParagraphExists) {
				flushTextBufferToParagraph();
				textModel.addImage(ref, myBookModel.getImageMap(), offset);
			} else {
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				textModel.addControl(FBTextKind.IMAGE, true);
				textModel.addImage(ref, myBookModel.getImageMap(), offset);
				textModel.addControl(FBTextKind.IMAGE, false);
				endParagraph();
			}
		}
	}

	public final void addImage(String id, ZLImage image) {
		myBookModel.addImage(id, image);
	}
}
