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

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.description.BookDescription.WritableBookDescription;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessorFactory;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class FB2DescriptionReader extends ZLXMLReaderAdapter {
	private WritableBookDescription myDescription;
	private	boolean myReadSomething;
	private	boolean myReadTitle;
	private	boolean myReadAuthor;
	private	boolean[] myReadAuthorName = new boolean[3];
	private	boolean myReadLanguage;
	private	String[] myAuthorNames = new String[3];

	public FB2DescriptionReader(BookDescription description) {
		myDescription = new WritableBookDescription(description);
		myDescription.clearAuthor();
		myDescription.setTitle("");
		myDescription.setLanguage("");
		myAuthorNames[0] = "";
		myAuthorNames[1] = "";
		myAuthorNames[2] = "";
	}
	
	public boolean dontCacheAttributeValues() {
		return true;
	}
	
	public boolean readDescription(String fileName) {
		myReadSomething = false;
		myReadTitle = false;
		myReadAuthor = false;
		myReadLanguage = false;
		for (int i = 0; i < 3; ++i) {
			myReadAuthorName[i] = false;
		}
		return readDocument(fileName);
		//TODO!!
		//return true;
	}

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.BODY:
				return true;
			case FB2Tag.TITLE_INFO:
				myReadSomething = true;
				break;
			case FB2Tag.BOOK_TITLE:
				myReadTitle = true;
				break;
			case FB2Tag.AUTHOR:
				myReadAuthor = true;
				break;
			case FB2Tag.LANG:
				myReadLanguage = true;
				break;
			case FB2Tag.FIRST_NAME:
				if (myReadAuthor) {
					myReadAuthorName[0] = true;
				}
				break;
			case FB2Tag.MIDDLE_NAME:
				if (myReadAuthor) {
					myReadAuthorName[1] = true;
				}
				break;
			case FB2Tag.LAST_NAME:
				if (myReadAuthor) {
					myReadAuthorName[2] = true;
				}
				break;
			case FB2Tag.SEQUENCE:
				if (myReadSomething) {
					String name = attributes.getValue("name");
					if (name != null) {
						String sequenceName = name;
						sequenceName.trim();
						myDescription.setSequenceName(sequenceName);
						String number = attributes.getValue("number");
						myDescription.setNumberInSequence((number != null) ? Integer.parseInt(number) : 0);
					}
				}
				break;
		}
		return false;
	}
	
	public boolean endElementHandler(String tag) {
		switch (FB2Tag.getTagByName(tag)) {
			case FB2Tag.TITLE_INFO:
				myReadSomething = false;
				break;
			case FB2Tag.BOOK_TITLE:
				myReadTitle = false;
				break;
			case FB2Tag.AUTHOR:
				if (myReadSomething) {
					myAuthorNames[0].trim();
						myAuthorNames[1].trim();
					myAuthorNames[2].trim();
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
					myReadAuthor = false;
				}
				break;
			case FB2Tag.LANG:
				myReadLanguage = false;
				break;
			case FB2Tag.FIRST_NAME:
				myReadAuthorName[0] = false;
				break;
			case FB2Tag.MIDDLE_NAME:
				myReadAuthorName[1] = false;
				break;
			case FB2Tag.LAST_NAME:
				myReadAuthorName[2] = false;
				break;
			default:
				break;
		}	
		return false;
	}
	
	public void characterDataHandler(char[] ch, int start, int length) {
		//TODO + length -- remove
		final String text = new String(ch).substring(start, start + length);
		//for (int i = 0 ; i < ch.length; i++) {
		//	System.out.print(ch[i]);
		//}
		//System.out.println();
		//System.out.println("characterDataHandler---" +text + "	start=" + start + "	lenght="+length);
		if (myReadSomething) {
			if (myReadTitle) {
				myDescription.setTitle(myDescription.getTitle()+text);//.append(text, len);
			} else if (myReadLanguage) {
				myDescription.setLanguage(myDescription.getLanguage()+text);
			} else {
				for (int i = 0; i < 3; ++i) {
					if (myReadAuthorName[i]) {
						myAuthorNames[i] += text;
						break;
					}
				}
			}
		}
	}

	//------------------------------------------------
	
	//private boolean myInterrupted;
		
	//public void interrupt() {
	//	myInterrupted = true;
	//}

	public boolean readDocument(String fileName) {
		final ZLXMLProcessor processor = ZLXMLProcessorFactory.getInstance().createXMLProcessor();
		return processor.read(this, fileName);
	}
}
