package org.geometerplus.fbreader.book;

import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

public class Buffer {
	final StringBuilder Builder = new StringBuilder();
	final ZLTextWordCursor Cursor;

	Buffer(ZLTextWordCursor cursor) {
		Cursor = new ZLTextWordCursor(cursor);
	}

	boolean isEmpty() {
		return Builder.length() == 0;
	}

	void append(Buffer buffer) {
		Builder.append(buffer.Builder);
		Cursor.setCursor(buffer.Cursor);
		buffer.Builder.delete(0, buffer.Builder.length());
	}

	void append(CharSequence data) {
		Builder.append(data);
	}
	
	public static Bookmark createBookmark(Book book, String modelId, ZLTextWordCursor startCursor, int maxWords, boolean isVisible) {
		final ZLTextWordCursor cursor = new ZLTextWordCursor(startCursor);

		final Buffer buffer = new Buffer(cursor);
		final Buffer sentenceBuffer = new Buffer(cursor);
		final Buffer phraseBuffer = new Buffer(cursor);

		int wordCounter = 0;
		int sentenceCounter = 0;
		int storedWordCounter = 0;
		boolean lineIsNonEmpty = false;
		boolean appendLineBreak = false;
mainLoop:
		while (wordCounter < maxWords && sentenceCounter < 3) {
			while (cursor.isEndOfParagraph()) {
				if (!cursor.nextParagraph()) {
					break mainLoop;
				}
				if (!buffer.isEmpty() && cursor.getParagraphCursor().isEndOfSection()) {
					break mainLoop;
				}
				if (!phraseBuffer.isEmpty()) {
					sentenceBuffer.append(phraseBuffer);
				}
				if (!sentenceBuffer.isEmpty()) {
					if (appendLineBreak) {
						buffer.append("\n");
					}
					buffer.append(sentenceBuffer);
					++sentenceCounter;
					storedWordCounter = wordCounter;
				}
				lineIsNonEmpty = false;
				if (!buffer.isEmpty()) {
					appendLineBreak = true;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord)element;
				if (lineIsNonEmpty) {
					phraseBuffer.append(" ");
				}
				phraseBuffer.Builder.append(word.Data, word.Offset, word.Length);
				phraseBuffer.Cursor.setCursor(cursor);
				phraseBuffer.Cursor.setCharIndex(word.Length);
				++wordCounter;
				lineIsNonEmpty = true;
				switch (word.Data[word.Offset + word.Length - 1]) {
					case ',':
					case ':':
					case ';':
					case ')':
						sentenceBuffer.append(phraseBuffer);
						break;
					case '.':
					case '!':
					case '?':
						++sentenceCounter;
						if (appendLineBreak) {
							buffer.append("\n");
							appendLineBreak = false;
						}
						sentenceBuffer.append(phraseBuffer);
						buffer.append(sentenceBuffer);
						storedWordCounter = wordCounter;
						break;
				}
			}
			cursor.nextWord();
		}
		if (storedWordCounter < 4) {
			if (sentenceBuffer.isEmpty()) {
				sentenceBuffer.append(phraseBuffer);
			}
			if (appendLineBreak) {
				buffer.append("\n");
			}
			buffer.append(sentenceBuffer);
		}
		return new Bookmark(book, modelId, startCursor, buffer.Cursor, buffer.Builder.toString(), isVisible);
	}
	

	public static void findEnd(ZLTextView view, Bookmark bookmark) {
		if (bookmark.myEnd != null) {
			return;
		}
		ZLTextWordCursor cursor = view.getStartCursor();
		if (cursor.isNull()) {
			cursor = view.getEndCursor();
		}
		if (cursor.isNull()) {
			return;
		}
		cursor = new ZLTextWordCursor(cursor);
		cursor.moveTo(bookmark);

		ZLTextWord word = null;
mainLoop:
		for (int count = bookmark.myLength; count > 0; cursor.nextWord()) {
			while (cursor.isEndOfParagraph()) {
				if (!cursor.nextParagraph()) {
					break mainLoop;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				if (word != null) {
					--count;
				}
				word = (ZLTextWord)element;
				System.err.println(new String(word.Data, word.Offset, word.Length));
				count -= word.Length;
			}
		}
		if (word != null) {
			bookmark.myEnd = new ZLTextFixedPosition(
				cursor.getParagraphIndex(),
				cursor.getElementIndex(),
				word.Length
			);
		}
	}

}
