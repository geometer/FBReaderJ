/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.bookmodel;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.model.*;

public class BookReader {
	public final BookModel Model;

	private ZLTextPlainModel myCurrentTextModel = null;
	
	private boolean myTextParagraphExists = false;
	
	private final ZLTextBuffer myBuffer = new ZLTextBuffer();
	private boolean myBufferIsNotEmpty = false;
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
		Model = model;
	}
	
	private final void flushTextBufferToParagraph() {
		if (myBufferIsNotEmpty) {
			final ZLTextBuffer buffer = myBuffer;
			myCurrentTextModel.addText(buffer);
			buffer.clear();
			myBufferIsNotEmpty = false;
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
	
	public final void addControl(ZLTextForcedControlEntry entry) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl(entry);
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
	
	public final boolean popKind() {
		if (myKindStackSize != 0) {
			--myKindStackSize;
			return true;
		}
		return false;
	}
	
	public final void beginParagraph() {
		beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}

	public final void beginParagraph(byte kind) {
		final ZLTextPlainModel textModel = myCurrentTextModel;
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
	
	// new method
	public boolean isTextParagraphExists() {
		return myTextParagraphExists;
	}
	
	private final void insertEndParagraph(byte kind) {
		final ZLTextPlainModel textModel = myCurrentTextModel;
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
	
/*	public final void insertEndOfTextParagraph() {
		insertEndParagraph(ZLTextParagraph.Kind.END_OF_TEXT_PARAGRAPH);
	}
*/	
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
		myCurrentTextModel = Model.BookTextModel;
	}
	
	public final void setFootnoteTextModel(String id) {
		myCurrentTextModel = Model.getFootnoteModel(id);
	}
	
	public final void addData(char[] data) {
		addData(data, 0, data.length);
	}

	public final void addData(char[] data, int offset, int length) {
		if (myTextParagraphExists) {
			myBuffer.append(data, offset, length);
			myBufferIsNotEmpty = true;
			if (!myInsideTitle) {
				mySectionContainsRegularContents = true;
			} else {
				addContentsData(data, offset, length);
			}
		}	
	}
	
	public final void addDataFinal(char[] data, int offset, int length) {
		if (myBufferIsNotEmpty) {
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
		final ZLTextPlainModel textModel = myCurrentTextModel;
		if (textModel != null) {
			int paragraphNumber = textModel.getParagraphsNumber();
			if (myTextParagraphExists) {
				--paragraphNumber;
			}
			Model.addHyperlinkLabel(label, textModel, paragraphNumber);
		}
	}
	
	public final void addHyperlinkLabel(String label, int paragraphNumber) {
		Model.addHyperlinkLabel(label, myCurrentTextModel, paragraphNumber);
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
		beginContentsParagraph(Model.BookTextModel, referenceNumber);
	}

	public final void beginContentsParagraph(ZLTextModel bookTextModel, int referenceNumber) {
		final ZLTextPlainModel textModel = myCurrentTextModel;
		final ArrayList tocStack = myTOCStack;
		if (textModel == bookTextModel) {
			ContentsModel contentsModel = Model.ContentsModel;
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
			contentsModel.setReference(para, myCurrentTextModel, referenceNumber);
			tocStack.add(para);
			myLastTOCParagraphIsEmpty = true;
			myContentsParagraphExists = true;
		}
	}
	
	public final void endContentsParagraph() {
		final ArrayList tocStack = myTOCStack;
		if (!tocStack.isEmpty()) {
			final ContentsModel contentsModel = Model.ContentsModel;
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
		setReference(contentsParagraphNumber, myCurrentTextModel, referenceNumber);
	}
	
	public final void setReference(int contentsParagraphNumber, ZLTextModel textModel, int referenceNumber) {
		final ContentsModel contentsModel = Model.ContentsModel;
		if (contentsParagraphNumber < contentsModel.getParagraphsNumber()) {
			contentsModel.setReference(
				contentsModel.getTreeParagraph(contentsParagraphNumber), textModel, referenceNumber
			);
		}
	}
	
	public final boolean paragraphIsOpen() {
		return myTextParagraphExists;
	}

	public final boolean contentsParagraphIsOpen() {
		return myContentsParagraphExists;
	}

	public final void beginContentsParagraph() {
		beginContentsParagraph(-1);
	}
	
	public final void addImageReference(String ref, short offset) {
		final ZLTextPlainModel textModel = myCurrentTextModel;
		if (textModel != null) {
			mySectionContainsRegularContents = true;
			if (myTextParagraphExists) {
				flushTextBufferToParagraph();
				textModel.addImage(ref, Model.getImageMap(), offset);
			} else {
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				textModel.addControl(FBTextKind.IMAGE, true);
				textModel.addImage(ref, Model.getImageMap(), offset);
				textModel.addControl(FBTextKind.IMAGE, false);
				endParagraph();
			}
		}
	}

	public final void addImage(String id, ZLImage image) {
		Model.addImage(id, image);
	}

	public final void addFixedHSpace(short length) {
		if (myTextParagraphExists) {
			myCurrentTextModel.addFixedHSpace(length);
		}
	}
	
	//
	public final void setNewTextModel() {
		myCurrentTextModel = Model.addBookTextModel();
	}
	
	public final void addHyperlinkLabel(String label, int modelNumber, int paragraphNumber) {
		Model.addHyperlinkLabel(label, (ZLTextModel) Model.getBookTextModels().get(modelNumber), paragraphNumber);
	}
}
