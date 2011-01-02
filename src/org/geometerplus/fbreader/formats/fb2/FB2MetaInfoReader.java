/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Tag;

public class FB2MetaInfoReader extends ZLXMLReaderAdapter {
	private final static int READ_NOTHING = 0;
	private final static int READ_SOMETHING = 1;
	private final static int READ_TITLE = 2;
	private final static int READ_AUTHOR = 3;
	private final static int READ_AUTHOR_NAME_0 = 4;
	private final static int READ_AUTHOR_NAME_1 = 5;
	private final static int READ_AUTHOR_NAME_2 = 6;
	private final static int READ_LANGUAGE = 7;
	private final static int READ_GENRE = 8;

	private final Book myBook;
	private int myReadState = READ_NOTHING;

	private	final String[] myAuthorNames = new String[3];
	private final StringBuilder myBuffer = new StringBuilder();

	public FB2MetaInfoReader(Book book) {
		myBook = book;
		myBook.setTitle(null);
		myBook.setLanguage(null);
	}
	
	public boolean dontCacheAttributeValues() {
		return true;
	}
	
	public boolean readMetaInfo() {
		myReadState = READ_NOTHING;
		myAuthorNames[0] = "";
		myAuthorNames[1] = "";
		myAuthorNames[2] = "";
		myBuffer.delete(0, myBuffer.length());
		return readDocument(myBook.File);
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
						name.trim();
						if (name.length() != 0) {
							int index = 0;
							try {
								final String sIndex = attributes.getValue("number");
								if (sIndex != null) {
									index = Integer.parseInt(sIndex);
								}
							} catch (NumberFormatException e) {
							}
							myBook.setSeriesInfo(name, index);
						}
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
					myBook.setTitle(myBuffer.toString().trim());
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.GENRE:
				if (myReadState == READ_GENRE) {
					final String genre = myBuffer.toString().trim();
					if (genre.length() > 0) {
						final ArrayList<Tag> tags = FB2TagManager.humanReadableTags(genre);
						if (tags != null) {
							for (Tag t : tags) {
								myBook.addTag(t);
							}
						} else {
							myBook.addTag(genre);
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
					myBook.addAuthor(fullName, myAuthorNames[2]);
					myAuthorNames[0] = "";
					myAuthorNames[1] = "";
					myAuthorNames[2] = "";
					myReadState = READ_SOMETHING;
				}
				break;
			case FB2Tag.LANG:
				if (myReadState == READ_LANGUAGE) {
					myBook.setLanguage(myBuffer.toString().trim());
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
		myBuffer.delete(0, myBuffer.length());
		return false;
	}
	
	public void characterDataHandler(char[] data, int start, int length) {
		switch (myReadState) {
			case READ_AUTHOR_NAME_0:
				myAuthorNames[0] += new String(data, start, length);
				break;
			case READ_AUTHOR_NAME_1:
				myAuthorNames[1] += new String(data, start, length);
				break;
			case READ_AUTHOR_NAME_2:
				myAuthorNames[2] += new String(data, start, length);
				break;
			case READ_TITLE:
			case READ_LANGUAGE:
			case READ_GENRE:
				myBuffer.append(data, start, length);
				break;
		}
	}

	public boolean readDocument(ZLFile file) {
		return ZLXMLProcessor.read(this, file, 512);
	}
}
