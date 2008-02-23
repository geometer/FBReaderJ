package org.zlibrary.text.view.impl;

//import java.util.*;

public final class ZLTextWordCursor {
	private ZLTextParagraphCursor myParagraphCursor;
	private int myWordNumber;
	private int myCharNumber;

	public ZLTextWordCursor() {
		myWordNumber = 0;
		myCharNumber = 0;
	}

	public ZLTextWordCursor(ZLTextWordCursor cursor) {
		setCursor(cursor);
	}

	public void setCursor(ZLTextWordCursor cursor) {
		myParagraphCursor = cursor.myParagraphCursor;
		myWordNumber = cursor.myWordNumber;
		myCharNumber = cursor.myCharNumber;
	}

	public ZLTextWordCursor(ZLTextParagraphCursor paragraphCursor) {
		setCursor(paragraphCursor);
	}

	public void setCursor(ZLTextParagraphCursor paragraphCursor) {
		myParagraphCursor = paragraphCursor;
		myWordNumber = 0;
		myCharNumber = 0;
	}

	public boolean isNull() {
		return myParagraphCursor == null;
	}

	public boolean equalsToCursor(ZLTextWordCursor cursor) {
		return (myWordNumber == cursor.myWordNumber) && (myCharNumber == cursor.myCharNumber) && (myParagraphCursor.getIndex() == cursor.myParagraphCursor.getIndex());
	}

	public boolean isStartOfParagraph() {
		return (myWordNumber == 0 && myCharNumber == 0);
	}

	/*Why don't we check here whether myCharNumber is in the end of the word or not?*/

	public boolean isEndOfParagraph() {
		return myWordNumber == myParagraphCursor.getParagraphLength();
	}

	public int getWordNumber() {
		return myWordNumber;
	}

	public int getCharNumber() {
		return myCharNumber;
	}

	public ZLTextElement getElement() {
		return myParagraphCursor.getElement(myWordNumber);
	}

	public ZLTextParagraphCursor getParagraphCursor() {
		return myParagraphCursor;
	}

	public void nextWord() {
		myWordNumber++;
		myCharNumber = 0;
	}

	public void previousWord() {
		myWordNumber--;
		myCharNumber = 0;
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
			myWordNumber = 0;
			myCharNumber = 0;
		}
	}

	/*Why are we moving outside of the paragraph?*/

	public void moveToParagraphEnd() {
		if (!isNull()) {
			myWordNumber = myParagraphCursor.getParagraphLength();
			myCharNumber = 0;
		}
	}

	/*Why do we create new object here instead of just changing myParagraphNumber?*/

	public void moveToParagraph(int paragraphNumber) {
		if (!isNull() && (paragraphNumber != myParagraphCursor.getIndex())) {
			myParagraphCursor = ZLTextParagraphCursor.cursor(myParagraphCursor.myModel, paragraphNumber);
			moveToParagraphStart();
		}		
	}

	public void moveTo(int wordNumber, int charNumber) {
		if (!isNull()) {
			if (wordNumber == 0 && charNumber == 0) {
				myWordNumber = 0;
				myCharNumber = 0;
			} else {
				wordNumber = Math.max(0, wordNumber);
				int size = myParagraphCursor.getParagraphLength();
				if (wordNumber > size) {
					myWordNumber = size;
					myCharNumber = 0;
				} else {
					myWordNumber = wordNumber;
					setCharNumber(charNumber);
				}
			}
		}
	}

	public void setCharNumber(int charNumber) {
		charNumber = Math.max(0, charNumber);
		myCharNumber = 0;
		if (charNumber > 0) {
			ZLTextElement element = myParagraphCursor.getElement(myWordNumber);
			if (element instanceof ZLTextWord) {
				if (charNumber <= ((ZLTextWord)element).Length) {
					myCharNumber = charNumber;
				}
			}
		}
	}	

	public void reset() {
		myParagraphCursor = null;
		myWordNumber = 0;
		myCharNumber = 0;
	}

	public void rebuild() {
		if (!isNull()) {
			myParagraphCursor.clear();
			myParagraphCursor.fill();
		}
	}
}
