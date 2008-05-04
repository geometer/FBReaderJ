/*
 * Copyright (C) 2004-2008 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.fbreader.description.BookDescription;

class OEBDescriptionReader extends ZLXMLReaderAdapter {
	private final BookDescription.WritableBookDescription myDescription;

	private String myDCMetadataTag;
	private String myTitleTag;
	private String myAuthorTag;
	private String mySubjectTag;

	private final ArrayList myAuthorList = new ArrayList();
	private final ArrayList myAuthorList2 = new ArrayList();

	OEBDescriptionReader(BookDescription description) {
		myDescription = new BookDescription.WritableBookDescription(description);
		myDescription.clearAuthor();
		myDescription.setTitle("");
		myDescription.removeAllTags();
	}

	boolean readDescription(String fileName) {
		myReadMetaData = false;
		myReadState = READ_NONE;
		if (!read(fileName)) {
			return false;
		}

		final ArrayList authors = myAuthorList.isEmpty() ? myAuthorList2 : myAuthorList;
		final int len = authors.size();
		for (int i = 0; i < len; ++i) {
			myDescription.addAuthor((String)authors.get(i));
		}

		return true;
	}

	private static final int READ_NONE = 0;
	private static final int READ_AUTHOR = 1;
	private static final int READ_AUTHOR2 = 2;
	private static final int READ_TITLE = 3;
	private static final int READ_SUBJECT = 4;
	private int myReadState;
	private boolean myReadMetaData;

	private final StringBuffer myBuffer = new StringBuffer();

	public boolean processNamespaces() {
		return true;
	}

	public void namespaceListChangedHandler(HashMap namespaces) {
		myTitleTag = null;
		myAuthorTag = null;
		mySubjectTag = null;
		for (Object o : namespaces.keySet()) {
			final String id = (String)o;
			if (id.startsWith("http://purl.org/dc/elements")) {
				final String name = (String)namespaces.get(id);
				myTitleTag = (name + ":title").intern();
				myAuthorTag = (name + ":creator").intern();
				mySubjectTag = (name + ":subject").intern();
				break;
			}
		}
	}

	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if ((tag == "metadata") || (tag == "dc-metadata")) {
			myDCMetadataTag = tag;
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
			}
		}
		return false;
	}

	public void characterDataHandler(char[] data, int start, int len) {
		switch (myReadState) {
			case READ_NONE:
				break;
			case READ_AUTHOR:
			case READ_AUTHOR2:
			case READ_TITLE:
			case READ_SUBJECT:
				myBuffer.append(data, start, len);
				break;
		}
	}

	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		if (myDCMetadataTag.equals(tag)) {
			return true;
		}

		final String bufferContent = myBuffer.toString().trim();
		if (bufferContent.length() != 0) {
			switch (myReadState) {
				case READ_TITLE:
					myDescription.setTitle(bufferContent);
					break;
				case READ_AUTHOR:
					myAuthorList.add(bufferContent);
					break;
				case READ_AUTHOR2:
					myAuthorList2.add(bufferContent);
					break;
				case READ_SUBJECT:
					myDescription.addTag(bufferContent, true);
					break;
			}
		}
		myBuffer.delete(0, myBuffer.length());
		myReadState = READ_NONE;
		return false;
	}
}
