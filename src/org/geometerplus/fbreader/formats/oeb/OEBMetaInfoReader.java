/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;
import java.io.IOException;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

class OEBMetaInfoReader extends ZLXMLReaderAdapter implements XMLNamespaces {
	private final Book myBook;

	private String mySeriesTitle = "";
	private String mySeriesIndex = null;

	private final ArrayList<String> myAuthorList = new ArrayList<String>();
	private final ArrayList<String> myAuthorList2 = new ArrayList<String>();

	OEBMetaInfoReader(Book book) {
		myBook = book;
		myBook.setTitle(null);
		myBook.setLanguage(null);
	}

	void readMetaInfo(ZLFile file) throws BookReadingException {
		myReadState = ReadState.Nothing;
		mySeriesTitle = "";
		mySeriesIndex = null;

		try {
			ZLXMLProcessor.read(this, file, 512);
		} catch (IOException e) {
			throw new BookReadingException(e, file);
		}

		final ArrayList<String> authors = myAuthorList.isEmpty() ? myAuthorList2 : myAuthorList;
		for (String a : authors) {
			final int index = a.indexOf(',');
			if (index >= 0) {
				a = a.substring(index + 1).trim() + ' ' + a.substring(0, index).trim();
			} else {
				a = a.trim();
			}
			myBook.addAuthor(a);
		}

		if (!"".equals(mySeriesTitle)) {
			myBook.setSeriesInfo(mySeriesTitle, mySeriesIndex);
		}
	}

	enum ReadState {
		Nothing,
		Metadata,
		Author,
		Author2,
		Title,
		Subject,
		Language
	};
	private ReadState myReadState;

	private final StringBuilder myBuffer = new StringBuilder();

	@Override
	public boolean processNamespaces() {
		return true;
	}

	private boolean testDCTag(String name, String tag) {
		return testTag(DublinCore, name, tag) || testTag(DublinCoreLegacy, name, tag);
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase();
		switch (myReadState) {
			default:
				break;
			case Nothing:
				if (testTag(OpenPackagingFormat, "metadata", tag) || "dc-metadata".equals(tag)) {
					myReadState = ReadState.Metadata;
				}
				break;
			case Metadata:
				if (testDCTag("title", tag)) {
					myReadState = ReadState.Title;
				} else if (testDCTag("author", tag)) {
					final String role = attributes.getValue("role");
					if (role == null) {
						myReadState = ReadState.Author2;
					} else if (role.equals("aut")) {
						myReadState = ReadState.Author;
					}
				} else if (testDCTag("subject", tag)) {
					myReadState = ReadState.Subject;
				} else if (testDCTag("language", tag)) {
					myReadState = ReadState.Language;
				} else if (testTag(OpenPackagingFormat, "meta", tag)) {
					if (attributes.getValue("name").equals("calibre:series")) {
						mySeriesTitle = attributes.getValue("content");
					} else if (attributes.getValue("name").equals("calibre:series_index")) {
						mySeriesIndex = attributes.getValue("content");
					}
				}
				break;
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int len) {
		switch (myReadState) {
			case Nothing:
			case Metadata:
				break;
			case Author:
			case Author2:
			case Title:
			case Subject:
			case Language:
				myBuffer.append(data, start, len);
				break;
		}
	}

	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		if (myReadState == ReadState.Metadata &&
			(testTag(OpenPackagingFormat, "metadata", tag) || "dc-metadata".equals(tag))) {
			myReadState = ReadState.Nothing;
			return true;
		}

		String bufferContent = myBuffer.toString().trim();
		if (bufferContent.length() != 0) {
			switch (myReadState) {
				case Title:
					myBook.setTitle(bufferContent);
					break;
				case Author:
					myAuthorList.add(bufferContent);
					break;
				case Author2:
					myAuthorList2.add(bufferContent);
					break;
				case Subject:
					myBook.addTag(bufferContent);
					break;
				case Language:
				{
					int index = bufferContent.indexOf('_');
					if (index >= 0) {
						bufferContent = bufferContent.substring(0, index);
					}
					index = bufferContent.indexOf('-');
					if (index >= 0) {
						bufferContent = bufferContent.substring(0, index);
					}
					myBook.setLanguage("cz".equals(bufferContent) ? "cs" : bufferContent);
					break;
				}
			}
		}
		myBuffer.delete(0, myBuffer.length());
		myReadState = ReadState.Metadata;
		return false;
	}
}
