/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.text.view.*;

public final class Bookmark extends ZLTextFixedPosition {
	public enum DateType {
		Creation,
		Modification,
		Access,
		Latest
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

	public final String ModelId;
	public final boolean IsVisible;

	Bookmark(long id, long bookId, String bookTitle, String text, Date creationDate, Date modificationDate, Date accessDate, int accessCount, String modelId, int paragraphIndex, int elementIndex, int charIndex, boolean isVisible) {
		super(paragraphIndex, elementIndex, charIndex);

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
	}

	public Bookmark(Book book, String modelId, ZLTextWordCursor cursor, int maxLength, boolean isVisible) {
		this(book, modelId, cursor, createBookmarkText(cursor, maxLength), isVisible);
	}

	public Bookmark(Book book, String modelId, ZLTextPosition position, String text, boolean isVisible) {
		super(position);

		myId = -1;
		myBookId = book.getId();
		myBookTitle = book.getTitle();
		myText = text;
		myCreationDate = new Date();
		ModelId = modelId;
		IsVisible = isVisible;
	}

	public long getId() {
		return myId;
	}

	public long getBookId() {
		return myBookId;
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

	private static String createBookmarkText(ZLTextWordCursor cursor, int maxWords) {
		cursor = new ZLTextWordCursor(cursor);

		final StringBuilder builder = new StringBuilder();
		final StringBuilder sentenceBuilder = new StringBuilder();
		final StringBuilder phraseBuilder = new StringBuilder();

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
				if ((builder.length() > 0) && cursor.getParagraphCursor().isEndOfSection()) {
					break mainLoop;
				}
				if (phraseBuilder.length() > 0) {
					sentenceBuilder.append(phraseBuilder);
					phraseBuilder.delete(0, phraseBuilder.length());
				}
				if (sentenceBuilder.length() > 0) {
					if (appendLineBreak) {
						builder.append("\n");
					}
					builder.append(sentenceBuilder);
					sentenceBuilder.delete(0, sentenceBuilder.length());
					++sentenceCounter;
					storedWordCounter = wordCounter;
				}
				lineIsNonEmpty = false;
				if (builder.length() > 0) {
					appendLineBreak = true;
				}
			}
			final ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				final ZLTextWord word = (ZLTextWord)element;
				if (lineIsNonEmpty) {
					phraseBuilder.append(" ");
				}
				phraseBuilder.append(word.Data, word.Offset, word.Length);
				++wordCounter;
				lineIsNonEmpty = true;
				switch (word.Data[word.Offset + word.Length - 1]) {
					case ',':
					case ':':
					case ';':
					case ')':
						sentenceBuilder.append(phraseBuilder);
						phraseBuilder.delete(0, phraseBuilder.length());
						break;
					case '.':
					case '!':
					case '?':
						++sentenceCounter;
						if (appendLineBreak) {
							builder.append("\n");
							appendLineBreak = false;
						}
						sentenceBuilder.append(phraseBuilder);
						phraseBuilder.delete(0, phraseBuilder.length());
						builder.append(sentenceBuilder);
						sentenceBuilder.delete(0, sentenceBuilder.length());
						storedWordCounter = wordCounter;
						break;
				}
			}
			cursor.nextWord();
		}
		if (storedWordCounter < 4) {
			if (sentenceBuilder.length() == 0) {
				sentenceBuilder.append(phraseBuilder);
			}
			if (appendLineBreak) {
				builder.append("\n");
			}
			builder.append(sentenceBuilder);
		}
		return builder.toString();
	}

	void setId(long id) {
		myId = id;
	}

	public void update(Bookmark other) {
		// TODO: copy other fields (?)
		myId = other.myId;
	}
}
