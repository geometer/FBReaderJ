/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.api;

import org.geometerplus.zlibrary.core.library.ZLibrary;

import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ApiImplementation extends ApiInterface.Stub implements ApiMethods {
	private final FBReaderApp myReader = (FBReaderApp)FBReaderApp.Instance();

	@Override
	public ApiObject request(int method, ApiObject[] parameters) {
		try {
			switch (method) {
				case GET_FBREADER_VERSION:
					return ApiObject.envelope(
						ZLibrary.Instance().getVersionName()
					);
				case GET_BOOK_LANGUAGE:
					return ApiObject.envelope(getBookLanguage());
				case GET_PARAGRAPHS_NUMBER:
					return ApiObject.envelope(getParagraphsNumber());
				case GET_ELEMENTS_NUMBER:
					return ApiObject.envelope(getElementsNumber(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_PARAGRAPH_TEXT:
					return ApiObject.envelope(getParagraphText(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_PAGE_START:
					return getTextPosition(myReader.getTextView().getStartCursor());
				case GET_PAGE_END:
					return getTextPosition(myReader.getTextView().getEndCursor());
				case IS_PAGE_END_OF_SECTION:
					return ApiObject.envelope(isPageEndOfSection());
				case IS_PAGE_END_OF_TEXT:
					return ApiObject.envelope(isPageEndOfText());
				case SET_PAGE_START:
					setPageStart(
						(TextPosition)parameters[0]
					);
					return ApiObject.Void.Instance;
				default:
					return new ApiObject.Error("Unsupported method code: " + method);
			}
		} catch (Throwable e) {
			return new ApiObject.Error("Exception in method " + method + ": " + e);
		} 
	}

	private String getBookLanguage() {
		return myReader.Model.Book.getLanguage();
	}

	private TextPosition getTextPosition(ZLTextWordCursor cursor) {
		return new TextPosition(
			cursor.getParagraphIndex(),
			cursor.getElementIndex(),
			cursor.getCharIndex()
		);
	}

	private boolean isPageEndOfSection() {
		final ZLTextWordCursor cursor = myReader.getTextView().getEndCursor();
		return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isEndOfSection();
	}

	private boolean isPageEndOfText() {
		final ZLTextWordCursor cursor = myReader.getTextView().getEndCursor();
		return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isLast();
	}

	private void setPageStart(TextPosition position) {
		myReader.getTextView().gotoPosition(position.ParagraphIndex, position.ElementIndex, position.CharIndex);
		myReader.getViewWidget().repaint();
	}

	private int getParagraphsNumber() {
		return myReader.Model.BookTextModel.getParagraphsNumber();
	}

	private int getElementsNumber(int paragraphIndex) {
		final ZLTextWordCursor cursor = new ZLTextWordCursor(myReader.getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphEnd();
		return cursor.getElementIndex();
	}

	private String getParagraphText(int paragraphIndex) {
		final StringBuffer sb = new StringBuffer();
		final ZLTextWordCursor cursor = new ZLTextWordCursor(myReader.getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphStart();
		while (!cursor.isEndOfParagraph()) {
			ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				sb.append(element.toString() + " ");
			}
			cursor.nextWord();
		}
		return sb.toString();
	}
}
