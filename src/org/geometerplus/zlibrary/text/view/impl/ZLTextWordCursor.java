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

package org.geometerplus.zlibrary.text.view.impl;

import org.geometerplus.zlibrary.text.model.ZLTextMark;

public final class ZLTextWordCursor {
	private ZLTextParagraphCursor myParagraphCursor;
	private int myWordIndex;
	private int myCharIndex;
	
//	private int myModelIndex;

	public ZLTextWordCursor() {
	}

	public ZLTextWordCursor(ZLTextWordCursor cursor) {
		setCursor(cursor);
	}

	public void setCursor(ZLTextWordCursor cursor) {
		myParagraphCursor = cursor.myParagraphCursor;
		myWordIndex = cursor.myWordIndex;
		myCharIndex = cursor.myCharIndex;
	}

	public ZLTextWordCursor(ZLTextParagraphCursor paragraphCursor) {
		setCursor(paragraphCursor);
	}

	public void setCursor(ZLTextParagraphCursor paragraphCursor) {
		myParagraphCursor = paragraphCursor;
		myWordIndex = 0;
		myCharIndex = 0;
	}

	public boolean isNull() {
		return myParagraphCursor == null;
	}

	public boolean equalsToCursor(ZLTextWordCursor cursor) {
		return (myWordIndex == cursor.myWordIndex) && (myCharIndex == cursor.myCharIndex) && (myParagraphCursor.Index == cursor.myParagraphCursor.Index);
	}

	public boolean isStartOfParagraph() {
		return (myWordIndex == 0 && myCharIndex == 0);
	}

	/*Why don't we check here whether myCharIndex is in the end of the word or not?*/

	public boolean isEndOfParagraph() {
		return myWordIndex == myParagraphCursor.getParagraphLength();
	}

	public int getWordIndex() {
		return myWordIndex;
	}

	public int getCharIndex() {
		return myCharIndex;
	}

	public ZLTextElement getElement() {
		return myParagraphCursor.getElement(myWordIndex);
	}

	public ZLTextParagraphCursor getParagraphCursor() {
		return myParagraphCursor;
	}

	public ZLTextMark getPosition() {
		if (myParagraphCursor == null) {
			return new ZLTextMark();
		}
		final ZLTextParagraphCursor paragraph = myParagraphCursor;
		int paragraphLength = paragraph.getParagraphLength();
		int wordIndex = myWordIndex;
		while ((wordIndex != paragraphLength) && (!(paragraph.getElement(wordIndex) instanceof ZLTextWord))) {
			wordIndex++;
		}
		if (wordIndex != paragraphLength) {
			return new ZLTextMark(paragraph.Index, ((ZLTextWord) paragraph.getElement(wordIndex)).getParagraphOffset(), 0);
		}
		return new ZLTextMark(paragraph.Index + 1, 0, 0);
	}
	
	public void nextWord() {
		myWordIndex++;
		myCharIndex = 0;
	}

	public void previousWord() {
		myWordIndex--;
		myCharIndex = 0;
	}

	public boolean nextParagraph() {
		if (!isNull()) {
			if (!myParagraphCursor.isLast()) {
				myParagraphCursor = myParagraphCursor.next();
				moveToParagraphStart();
				return true;
			}
		}
		return false;
	}

	public boolean previousParagraph() {
		if (!isNull()) {
			if (!myParagraphCursor.isFirst()) {
				myParagraphCursor = myParagraphCursor.previous();
				moveToParagraphStart();
				return true;
			}
		}
		return false;
	}

	public void moveToParagraphStart() {
		if (!isNull()) {
			myWordIndex = 0;
			myCharIndex = 0;
		}
	}

	public void moveToParagraphEnd() {
		if (!isNull()) {
			myWordIndex = myParagraphCursor.getParagraphLength();
			myCharIndex = 0;
		}
	}

	public void moveToParagraph(int paragraphIndex) {
		if (!isNull() && (paragraphIndex != myParagraphCursor.Index)) {
			myParagraphCursor = ZLTextParagraphCursor.cursor(myParagraphCursor.myModel, paragraphIndex);
			moveToParagraphStart();
		}		
	}

	public void moveTo(int wordIndex, int charIndex) {
		if (!isNull()) {
			if (wordIndex == 0 && charIndex == 0) {
				myWordIndex = 0;
				myCharIndex = 0;
			} else {
				wordIndex = Math.max(0, wordIndex);
				int size = myParagraphCursor.getParagraphLength();
				if (wordIndex > size) {
					myWordIndex = size;
					myCharIndex = 0;
				} else {
					myWordIndex = wordIndex;
					setCharIndex(charIndex);
				}
			}
		}
	}

	public void setCharIndex(int charIndex) {
		charIndex = Math.max(0, charIndex);
		myCharIndex = 0;
		if (charIndex > 0) {
			ZLTextElement element = myParagraphCursor.getElement(myWordIndex);
			if (element instanceof ZLTextWord) {
				if (charIndex <= ((ZLTextWord)element).Length) {
					myCharIndex = charIndex;
				}
			}
		}
	}	

	public void reset() {
		myParagraphCursor = null;
		myWordIndex = 0;
		myCharIndex = 0;
	}

	public void rebuild() {
		if (!isNull()) {
			myParagraphCursor.clear();
			myParagraphCursor.fill();
			moveTo(myWordIndex, myCharIndex);
		}
	}

/*	public int getModelIndex() {
		return myModelIndex;
	}

	public void setModelIndex(int modelIndex) {
		myModelIndex = modelIndex;
	}
	*/
}
