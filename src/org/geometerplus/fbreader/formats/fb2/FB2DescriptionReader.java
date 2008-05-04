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

package org.geometerplus.fbreader.formats.fb2;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.description.BookDescription.WritableBookDescription;

public class FB2DescriptionReader extends ZLXMLReaderAdapter {
	private final static int READ_NOTHING = 0;
	private final static int READ_SOMETHING = 1;
	private final static int READ_TITLE = 2;
	private final static int READ_AUTHOR = 3;
	private final static int READ_AUTHOR_NAME_0 = 4;
	private final static int READ_AUTHOR_NAME_1 = 5;
	private final static int READ_AUTHOR_NAME_2 = 6;
	private final static int READ_LANGUAGE = 7;
	private final static int READ_GENRE = 8;

	private WritableBookDescription myDescription;
	private int myReadState = READ_NOTHING;

	private	final String[] myAuthorNames = new String[3];
	private final StringBuilder myGenreBuffer = new StringBuilder();

	public FB2DescriptionReader(BookDescription description) {
		myDescription = new WritableBookDescription(description);
		myDescription.clearAuthor();
		myDescription.setTitle("");
		myDescription.setLanguage("");
		myDescription.removeAllTags();
	}
	
	public boolean dontCacheAttributeValues() {
		return true;
	}
	
	public boolean readDescription(String fileName) {
		myReadState = READ_NOTHING;
		myAuthorNames[0] = "";
		myAuthorNames[1] = "";
		myAuthorNames[2] = "";
		myGenreBuffer.delete(0, myGenreBuffer.length());
		return readDocument(fileName);
	}

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.BODY:
				return true;
			case FB2Tag.TITLE_INFO:
				myReadState = READ_SOMETHING;
				break;
			case FB2Tag.BOOK_TITLE:
				if (myReadState == READ_SOMETHING) {
					myReadState = READ_TITLE;
				}
				break;
			case FB2Tag.GENRE:
				if (myReadState == READ_SOMETHING) {
					myReadState = READ_GENRE;
				}
				break;
			case FB2Tag.AUTHOR:
				if (myReadState == READ_SOMETHING) {
					myReadState = READ_AUTHOR;
				}
				break;
			case FB2Tag.LANG:
				if (myReadState == READ_SOMETHING) {
					myReadState = READ_LANGUAGE;
				}
				break;
			case FB2Tag.FIRST_NAME:
				if (myReadState == READ_AUTHOR) {
					myReadState = READ_AUTHOR_NAME_0;
				}
				break;
			case FB2Tag.MIDDLE_NAME:
				if (myReadState == READ_AUTHOR) {
					myReadState = READ_AUTHOR_NAME_1;
				}
				break;
			case FB2Tag.LAST_NAME:
				if (myReadState == READ_AUTHOR) {
					myReadState = READ_AUTHOR_NAME_2;
				}
				break;
			case FB2Tag.SEQUENCE:
				if (myReadState == READ_SOMETHING) {
					String name = attributes.getValue("name");
					if (name != null) {
						String sequenceName = name;
						sequenceName.trim();
						myDescription.setSeriesName(sequenceName);
						String number = attributes.getValue("number");
						myDescription.setNumberInSeries((number != null) ? Integer.parseInt(number) : 0);
					}
				}
				break;
		}
		return false;
	}
	
	public boolean endElementHandler(String tag) {
		switch (FB2Tag.getTagByName(tag)) {
			case FB2Tag.TITLE_INFO:
				myReadState = READ_NOTHING;
				break;
			case FB2Tag.BOOK_TITLE:
				if (myReadState == READ_TITLE) {
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.GENRE:
				if (myReadState == READ_GENRE) {
					final String genre = myGenreBuffer.toString().trim();
					myGenreBuffer.delete(0, myGenreBuffer.length());
					if (genre.length() > 0) {
						final ArrayList tags = FB2TagManager.humanReadableTags(genre);
						if (tags != null) {
							final int len = tags.size();
							for (int i = 0; i < len; ++i) {
								myDescription.addTag((String)tags.get(i), false);
							}
						} else {
							myDescription.addTag(genre, true);
						}
					}
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.AUTHOR:
				if (myReadState == READ_AUTHOR) {
					myAuthorNames[0] = myAuthorNames[0].trim();
					myAuthorNames[1] = myAuthorNames[1].trim();
					myAuthorNames[2] = myAuthorNames[2].trim();
					String fullName = myAuthorNames[0];
					if (fullName.length() != 0 && myAuthorNames[1].length() != 0) {
						fullName += ' ';
					}
					fullName += myAuthorNames[1];
					if (fullName.length() != 0 && myAuthorNames[2].length() != 0) {
						fullName += ' ';
					}
					fullName += myAuthorNames[2];
					myDescription.addAuthor(fullName, myAuthorNames[2]);
					myAuthorNames[0] = "";
					myAuthorNames[1] = "";
					myAuthorNames[2] = "";
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.LANG:
				if (myReadState == READ_LANGUAGE) {
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.FIRST_NAME:
				if (myReadState == READ_AUTHOR_NAME_0) {
					myReadState = READ_AUTHOR;
				}
				break;
			case FB2Tag.MIDDLE_NAME:
				if (myReadState == READ_AUTHOR_NAME_1) {
					myReadState = READ_AUTHOR;
				}
				break;
			case FB2Tag.LAST_NAME:
				if (myReadState == READ_AUTHOR_NAME_2) {
					myReadState = READ_AUTHOR;
				}
				break;
			default:
				break;
		}	
		return false;
	}
	
	public void characterDataHandler(char[] data, int start, int length) {
		switch (myReadState) {
			case READ_TITLE:
				myDescription.setTitle(myDescription.getTitle() + new String(data, start, length));
				break;
			case READ_LANGUAGE:
				myDescription.setLanguage(myDescription.getLanguage() + new String(data, start, length));
				break;
			case READ_AUTHOR_NAME_0:
				myAuthorNames[0] += new String(data, start, length);
				break;
			case READ_AUTHOR_NAME_1:
				myAuthorNames[1] += new String(data, start, length);
				break;
			case READ_AUTHOR_NAME_2:
				myAuthorNames[2] += new String(data, start, length);
				break;
			case READ_GENRE:
				myGenreBuffer.append(data, start, length);
				break;
		}
	}

	public boolean readDocument(String fileName) {
		final ZLXMLProcessor processor = ZLXMLProcessorFactory.getInstance().createXMLProcessor();
		return processor.read(this, fileName);
	}
}
