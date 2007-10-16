package org.zlibrary.view;

public interface ZLTextWordCursor {
	
	void setCursor(ZLTextWordCursor cursor);
	void setCursor(ZLTextParagraphCursor paragraphCursor);

	boolean isNull();
	boolean equalWordNumber(ZLTextWordCursor cursor);
	boolean sameElementAs(ZLTextWordCursor cursor);
	boolean isStartOfParagraph();
	boolean isEndOfParagraph();
	int getWordNumber();
	int getCharNumber();
	ZLTextElement getElement();
	ZLTextParagraphCursor getParagraphCursor();

	void nextWord();
	void previousWord();
	boolean nextParagraph();
	boolean previousParagraph();
	void moveToParagraphStart();
	void moveToParagraphEnd();
	void moveToParagraph(int paragraphNumber);
	void moveTo(int wordNumber, int charNumber);
	void setCharNumber(int charNumber);

	void rebuild();
}
