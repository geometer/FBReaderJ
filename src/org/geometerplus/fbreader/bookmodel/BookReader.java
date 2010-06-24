/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.model.*;

public class BookReader {
	public final BookModel Model;

	private ZLTextWritableModel myCurrentTextModel = null;
	
	private boolean myTextParagraphExists = false;
	private boolean myTextParagraphIsNonEmpty = false;
	
	private char[] myTextBuffer = new char[4096];
	private int myTextBufferLength;
	private StringBuilder myContentsBuffer = new StringBuilder();

	private byte[] myKindStack = new byte[20];
	private int myKindStackSize;
	
	private byte myHyperlinkKind;
	private String myHyperlinkReference = "";
	
	private boolean myInsideTitle = false;
	private boolean mySectionContainsRegularContents = false;
	
	private TOCTree myCurrentContentsTree;

	private CharsetDecoder myByteDecoder;

	public BookReader(BookModel model) {
		Model = model;
		myCurrentContentsTree = model.TOCTree;
	}

	public final void setByteDecoder(CharsetDecoder decoder) {
		myByteDecoder = decoder;
	}
	
	private final void flushTextBufferToParagraph() {
		if (myTextBufferLength > 0) {
			myCurrentTextModel.addText(myTextBuffer, 0, myTextBufferLength);
			myTextBufferLength = 0;
			if (myByteDecoder != null) {
				myByteDecoder.reset();
			}
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
	
	/*
	public final void addControl(ZLTextForcedControlEntry entry) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addControl(entry);
		}
	}
	*/
	
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
		final ZLTextWritableModel textModel = myCurrentTextModel;
		if (textModel != null) {
			textModel.createParagraph(kind);
			final byte[] stack = myKindStack;
			final int size = myKindStackSize;
			for (int i = 0; i < size; ++i) {
				textModel.addControl(stack[i], true);
			}
			if (myHyperlinkReference.length() != 0) {
				textModel.addHyperlinkControl(myHyperlinkKind, hyperlinkType(myHyperlinkKind), myHyperlinkReference);
			}
			myTextParagraphExists = true;
		}		
	}
	
	public final void endParagraph() {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myTextParagraphExists = false;
			myTextParagraphIsNonEmpty = false;
		}
	}
	
	private final void insertEndParagraph(byte kind) {
		final ZLTextWritableModel textModel = myCurrentTextModel;
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
		if (myCurrentTextModel != null) {
			myCurrentTextModel.stopReading();
		}
		myCurrentTextModel = null;
	}
	
	public final void enterTitle() {
		myInsideTitle = true;
	}
	
	public final void exitTitle() {
		myInsideTitle = false;
	}
	
	public final void setMainTextModel() {
		if ((myCurrentTextModel != null) && (myCurrentTextModel != Model.BookTextModel)) {
			myCurrentTextModel.stopReading();
		}
		myCurrentTextModel = (ZLTextWritableModel)Model.BookTextModel;
	}
	
	public final void setFootnoteTextModel(String id) {
		if ((myCurrentTextModel != null) && (myCurrentTextModel != Model.BookTextModel)) {
			myCurrentTextModel.stopReading();
		}
		myCurrentTextModel = (ZLTextWritableModel)Model.getFootnoteModel(id);
	}
	
	public final void addData(char[] data) {
		addData(data, 0, data.length, false);
	}

	public final void addData(char[] data, int offset, int length, boolean direct) {
		if (!myTextParagraphExists || (length == 0)) {
			return;
		}
		myTextParagraphIsNonEmpty = true;

		if (direct && (myTextBufferLength == 0) && !myInsideTitle) {
			myCurrentTextModel.addText(data, offset, length);
		} else {
			final int oldLength = myTextBufferLength;
			final int newLength = oldLength + length;
			if (myTextBuffer.length < newLength) {
				myTextBuffer = ZLArrayUtils.createCopy(myTextBuffer, oldLength, newLength);
			}
			System.arraycopy(data, offset, myTextBuffer, oldLength, length);
			myTextBufferLength = newLength;
			if (myInsideTitle) {
				addContentsData(myTextBuffer, oldLength, length);
			}
		}
		if (!myInsideTitle) {
			mySectionContainsRegularContents = true;
		}
	}

	private byte[] myUnderflowByteBuffer = new byte[4];
	private int myUnderflowLength;

	public final void addByteData(byte[] data, int start, int length) {
		if (!myTextParagraphExists || (length == 0)) {
			return;
		}
		myTextParagraphIsNonEmpty = true;

		final int oldLength = myTextBufferLength;
		if (myTextBuffer.length < oldLength + length) {
			myTextBuffer = ZLArrayUtils.createCopy(myTextBuffer, oldLength, oldLength + length);
		}
		final CharBuffer cb = CharBuffer.wrap(myTextBuffer, myTextBufferLength, length);

		if (myUnderflowLength > 0) {
			int l = myUnderflowLength;
			while (length-- > 0) {
				myUnderflowByteBuffer[l++] = data[start++];
				final ByteBuffer ubb = ByteBuffer.wrap(myUnderflowByteBuffer);
				myByteDecoder.decode(ubb, cb, false);
				if (cb.position() != oldLength) {
					myUnderflowLength = 0;
					break;
				}
			}
			if (length == 0) {
				myUnderflowLength = l;
				return;
			}
		}

		ByteBuffer bb = ByteBuffer.wrap(data, start, length);
		myByteDecoder.decode(bb, cb, false);
		myTextBufferLength = cb.position();
		int rem = bb.remaining();
		if (rem > 0) {
			for (int i = 0, j = start + length - rem; i < rem;) {
				myUnderflowByteBuffer[i++] = data[j++];
			}
			myUnderflowLength = rem;
		}

		if (myInsideTitle) {
			addContentsData(myTextBuffer, oldLength, myTextBufferLength - oldLength);
		} else {
			mySectionContainsRegularContents = true;
		}
	}
	
	private static byte hyperlinkType(byte kind) {
		return (kind == FBTextKind.EXTERNAL_HYPERLINK) ?
			FBHyperlinkType.EXTERNAL : FBHyperlinkType.INTERNAL;
	}

	public final void addHyperlinkControl(byte kind, String label) {
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel.addHyperlinkControl(kind, hyperlinkType(kind), label);
		}
		myHyperlinkKind = kind;
		myHyperlinkReference = label;
	}
	
	public final void addHyperlinkLabel(String label) {
		final ZLTextWritableModel textModel = myCurrentTextModel;
		if (textModel != null) {
			int paragraphNumber = textModel.getParagraphsNumber();
			if (myTextParagraphExists) {
				--paragraphNumber;
			}
			Model.addHyperlinkLabel(label, textModel, paragraphNumber);
		}
	}
	
	public final void addHyperlinkLabel(String label, int paragraphIndex) {
		Model.addHyperlinkLabel(label, myCurrentTextModel, paragraphIndex);
	}
	
	public final void addContentsData(char[] data) {
		addContentsData(data, 0, data.length);
	}

	public final void addContentsData(char[] data, int offset, int length) {
		if ((length != 0) && (myCurrentContentsTree != null)) {
			myContentsBuffer.append(data, offset, length);
		}
	}
	
	public final void beginContentsParagraph(int referenceNumber) {
		beginContentsParagraph(Model.BookTextModel, referenceNumber);
	}

	public final void beginContentsParagraph(ZLTextModel bookTextModel, int referenceNumber) {
		final ZLTextModel textModel = myCurrentTextModel;
		if (textModel == bookTextModel) {
			if (referenceNumber == -1) {
				referenceNumber = textModel.getParagraphsNumber();
			}
			TOCTree parentTree = myCurrentContentsTree;
			if (parentTree.Level > 0) {
				if (myContentsBuffer.length() > 0) {
					parentTree.setText(myContentsBuffer.toString());
					myContentsBuffer.delete(0, myContentsBuffer.length());
				} else if (parentTree.getText() == null) {
					parentTree.setText("...");
				}
			} else {
				myContentsBuffer.delete(0, myContentsBuffer.length());
			}
			TOCTree tree = new TOCTree(parentTree);
			tree.setReference(myCurrentTextModel, referenceNumber);
			myCurrentContentsTree = tree;
		}
	}
	
	public final void endContentsParagraph() {
		final TOCTree tree = myCurrentContentsTree;
		if (tree.Level == 0) {
			myContentsBuffer.delete(0, myContentsBuffer.length());
			return;
		}
		if (myContentsBuffer.length() > 0) {
			tree.setText(myContentsBuffer.toString());
			myContentsBuffer.delete(0, myContentsBuffer.length());
		} else if (tree.getText() == null) {
			tree.setText("...");
		}
		myCurrentContentsTree = tree.Parent;
	}

	public final void setReference(int contentsParagraphNumber, int referenceNumber) {
		setReference(contentsParagraphNumber, myCurrentTextModel, referenceNumber);
	}
	
	public final void setReference(int contentsParagraphNumber, ZLTextWritableModel textModel, int referenceNumber) {
		final TOCTree contentsTree = Model.TOCTree;
		if (contentsParagraphNumber < contentsTree.getSize()) {
			contentsTree.getTreeByParagraphNumber(contentsParagraphNumber).setReference(
				textModel, referenceNumber
			);
		}
	}
	
	public final boolean paragraphIsOpen() {
		return myTextParagraphExists;
	}
	
	public boolean paragraphIsNonEmpty() {
		return myTextParagraphIsNonEmpty;
	}

	public final boolean contentsParagraphIsOpen() {
		return myCurrentContentsTree.Level > 0;
	}

	public final void beginContentsParagraph() {
		beginContentsParagraph(-1);
	}
	
	public final void addImageReference(String ref) {
		addImageReference(ref, (short)0);
	}

	public final void addImageReference(String ref, short vOffset) {
		final ZLTextWritableModel textModel = myCurrentTextModel;
		if (textModel != null) {
			mySectionContainsRegularContents = true;
			if (myTextParagraphExists) {
				flushTextBufferToParagraph();
				textModel.addImage(ref, vOffset);
			} else {
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				textModel.addControl(FBTextKind.IMAGE, true);
				textModel.addImage(ref, vOffset);
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
}
