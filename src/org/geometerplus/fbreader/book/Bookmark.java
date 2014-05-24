/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.util.*;

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.text.view.*;

public final class Bookmark extends ZLTextFixedPosition {
	public enum DateType {
		Creation,
		Modification,
		Access,
		Latest
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

	private long myId;
	private final long myBookId;
	private final String myBookTitle;
	private String myText;
	private final Date myCreationDate;
	private Date myModificationDate;
	private Date myAccessDate;
	private int myAccessCount;
	private Date myLatestDate;
	private ZLTextFixedPosition myEnd;
	private int myLength;
	private int myStyleId;

	public final String ModelId;
	public final boolean IsVisible;

	private Bookmark(long bookId, Bookmark original) {
		super(original);
		myId = -1;
		myBookId = bookId;
		myBookTitle = original.myBookTitle;
		myText = original.myText;
		myCreationDate = original.myCreationDate;
		myModificationDate = original.myModificationDate;
		myAccessDate = original.myAccessDate;
		myAccessCount = original.myAccessCount;
		myLatestDate = original.myLatestDate;
		myEnd = original.myEnd;
		myLength = original.myLength;
		myStyleId = original.myStyleId;
		ModelId = original.ModelId;
		IsVisible = original.IsVisible;
	}

	Bookmark(
		long id, long bookId, String bookTitle, String text,
		Date creationDate, Date modificationDate, Date accessDate, int accessCount,
		String modelId,
		int start_paragraphIndex, int start_elementIndex, int start_charIndex,
		int end_paragraphIndex, int end_elementIndex, int end_charIndex,
		boolean isVisible,
		int styleId
	) {
		super(start_paragraphIndex, start_elementIndex, start_charIndex);

		myId = id;
		myBookId = bookId;
		myBookTitle = bookTitle;
		myText = text;
		myCreationDate = creationDate;
		myModificationDate = modificationDate;
		myLatestDate = (modificationDate != null) ? modificationDate : creationDate;
		if (accessDate != null) {
			myAccessDate = accessDate;
			if (myLatestDate.compareTo(accessDate) < 0) {
				myLatestDate = accessDate;
			}
		}
		myAccessCount = accessCount;
		ModelId = modelId;
		IsVisible = isVisible;

		if (end_charIndex >= 0) {
			myEnd = new ZLTextFixedPosition(end_paragraphIndex, end_elementIndex, end_charIndex);
		} else {
			myLength = end_paragraphIndex;
		}

		myStyleId = styleId;
	}

	public Bookmark(Book book, String modelId, ZLTextPosition start, ZLTextPosition end, String text, boolean isVisible) {
		super(start);

		myId = -1;
		myBookId = book.getId();
		myBookTitle = book.getTitle();
		myText = text;
		myCreationDate = new Date();
		ModelId = modelId;
		IsVisible = isVisible;
		myEnd = new ZLTextFixedPosition(end);
		myStyleId = 1;
	}

	public void findEnd(ZLTextView view) {
		if (myEnd != null) {
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
		cursor.moveTo(this);

		ZLTextWord word = null;
mainLoop:
		for (int count = myLength; count > 0; cursor.nextWord()) {
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
				count -= word.Length;
			}
		}
		if (word != null) {
			myEnd = new ZLTextFixedPosition(
				cursor.getParagraphIndex(),
				cursor.getElementIndex(),
				word.Length
			);
		}
	}

	public long getId() {
		return myId;
	}

	public long getBookId() {
		return myBookId;
	}

	public int getStyleId() {
		return myStyleId;
	}

	public void setStyleId(int styleId) {
		myStyleId = styleId;
	}

	public String getText() {
		return myText;
	}

	public String getBookTitle() {
		return myBookTitle;
	}

	public Date getDate(DateType type) {
		switch (type) {
			case Creation:
				return myCreationDate;
			case Modification:
				return myModificationDate;
			case Access:
				return myAccessDate;
			default:
			case Latest:
				return myLatestDate;
		}
	}

	public int getAccessCount() {
		return myAccessCount;
	}

	public ZLTextPosition getEnd() {
		return myEnd;
	}

	public int getLength() {
		return myLength;
	}

	public void setText(String text) {
		if (!text.equals(myText)) {
			myText = text;
			myModificationDate = new Date();
			myLatestDate = myModificationDate;
		}
	}

	public void markAsAccessed() {
		myAccessDate = new Date();
		++myAccessCount;
		myLatestDate = myAccessDate;
	}

	public static class ByTimeComparator implements Comparator<Bookmark> {
		public int compare(Bookmark bm0, Bookmark bm1) {
			final Date date0 = bm0.getDate(DateType.Latest);
			final Date date1 = bm1.getDate(DateType.Latest);
			if (date0 == null) {
				return date1 == null ? 0 : -1;
			}
			return date1 == null ? 1 : date1.compareTo(date0);
		}
	}

	void setId(long id) {
		myId = id;
	}

	public void update(Bookmark other) {
		// TODO: copy other fields (?)
		if (other != null) {
			myId = other.myId;
		}
	}

	Bookmark transferToBook(Book book) {
		final long bookId = book.getId();
		return bookId != -1 ? new Bookmark(bookId, this) : null;
	}

	// not equals, we do not compare ids
	boolean sameAs(Bookmark other) {
		return
			ParagraphIndex == other.ParagraphIndex &&
			ElementIndex == other.ElementIndex &&
			CharIndex == other.CharIndex &&
			MiscUtil.equals(myText, other.myText);
	}

	private static class Buffer {
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
	}
}
