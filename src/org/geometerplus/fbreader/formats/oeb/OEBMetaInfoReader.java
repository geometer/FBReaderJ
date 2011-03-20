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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.library.Book;

class OEBMetaInfoReader extends ZLXMLReaderAdapter implements XMLNamespaces {
	private final Book myBook;

	private String myDCMetadataTag = "dc-metadata";
	private String myMetadataTag = "metadata";
	private String myOpfMetadataTag = "metadata";
	private String myMetadataTagRealName;
	private String myTitleTag;
	private String myAuthorTag;
	private String mySubjectTag;
	private String myLanguageTag;
	private String myMetaTag = "meta";

	private String mySeriesTitle = "";
	private float mySeriesIndex = 0;
	
	private final ArrayList<String> myAuthorList = new ArrayList<String>();
	private final ArrayList<String> myAuthorList2 = new ArrayList<String>();

	OEBMetaInfoReader(Book book) {
		myBook = book;
		myBook.setTitle(null);
		myBook.setLanguage(null);
	}

	boolean readMetaInfo(ZLFile file) {
		myReadMetaData = false;
		myReadState = READ_NONE;

		if (!ZLXMLProcessor.read(this, file, 512)) {
			return false;
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

		return true;
	}

	private static final int READ_NONE = 0;
	private static final int READ_AUTHOR = 1;
	private static final int READ_AUTHOR2 = 2;
	private static final int READ_TITLE = 3;
	private static final int READ_SUBJECT = 4;
	private static final int READ_LANGUAGE = 5;
	private int myReadState;
	private boolean myReadMetaData;

	private final StringBuilder myBuffer = new StringBuilder();

	@Override
	public boolean processNamespaces() {
		return true;
	}

	@Override
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myTitleTag = null;
		myAuthorTag = null;
		mySubjectTag = null;
		myLanguageTag = null;
		myOpfMetadataTag = "metadata";
		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String id = entry.getValue();
			if (id.startsWith(DublinCorePrefix) || id.startsWith(DublinCoreLegacyPrefix)) {
				final String name = entry.getKey();
				myTitleTag = (name + ":title").intern();
				myAuthorTag = (name + ":creator").intern();
				mySubjectTag = (name + ":subject").intern();
				myLanguageTag = (name + ":language").intern();
			} else if (id.equals(OpenPackagingFormat)) {
				final String name = entry.getKey();
				myOpfMetadataTag = (name + ":metadata").intern();
			}
		}
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (tag == myMetadataTag || tag == myDCMetadataTag || tag == myOpfMetadataTag) {
			myMetadataTagRealName = tag;
			myReadMetaData = true;
		} else if (myReadMetaData) {
			if (tag == myTitleTag) {
				myReadState = READ_TITLE;
			} else if (tag == myAuthorTag) {
				final String role = attributes.getValue("role");
				if (role == null) {
					myReadState = READ_AUTHOR2;
				} else if (role.equals("aut")) {
					myReadState = READ_AUTHOR;
				}
			} else if (tag == mySubjectTag) {
				myReadState = READ_SUBJECT;
			} else if (tag == myLanguageTag) {
				myReadState = READ_LANGUAGE;
			} else if (tag == myMetaTag) {
				if (attributes.getValue("name").equals("calibre:series")) {
					mySeriesTitle = attributes.getValue("content");
				} else if (attributes.getValue("name").equals("calibre:series_index")) {
					final String strIndex = attributes.getValue("content");
					try {
						mySeriesIndex = Float.parseFloat(strIndex);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int len) {
		switch (myReadState) {
			case READ_NONE:
				break;
			case READ_AUTHOR:
			case READ_AUTHOR2:
			case READ_TITLE:
			case READ_SUBJECT:
			case READ_LANGUAGE:
				myBuffer.append(data, start, len);
				break;
		}
	}

	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		if (tag.equals(myMetadataTagRealName)) {
			return true;
		}

		String bufferContent = myBuffer.toString().trim();
		if (bufferContent.length() != 0) {
			switch (myReadState) {
				case READ_TITLE:
					myBook.setTitle(bufferContent);
					break;
				case READ_AUTHOR:
					myAuthorList.add(bufferContent);
					break;
				case READ_AUTHOR2:
					myAuthorList2.add(bufferContent);
					break;
				case READ_SUBJECT:
					myBook.addTag(bufferContent);
					break;
				case READ_LANGUAGE:
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
					}
					break;
			}
		} else {
			if (tag.equals(myMetaTag)) {
				if (!"".equals(mySeriesTitle) && mySeriesIndex > 0) {
					myBook.setSeriesInfo(mySeriesTitle, mySeriesIndex);
				}
			}
		}
		myBuffer.delete(0, myBuffer.length());
		myReadState = READ_NONE;
		return false;
	}
}
