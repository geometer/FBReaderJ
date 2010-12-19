/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

class OEBAnnotationReader extends ZLXMLReaderAdapter implements XMLNamespaces {
	private String myDescriptionTag;
	
	private static final int READ_NONE = 0;
	private static final int READ_DESCRIPTION = 1;
	private int myReadState;

	private final StringBuffer myBuffer = new StringBuffer();

	String readAnnotation(ZLFile file) {
		myReadState = READ_NONE;
		myBuffer.delete(0, myBuffer.length());

		if (ZLXMLProcessor.read(this, file, 512)) {
			final int len = myBuffer.length();
			if (len > 1) {
				if (myBuffer.charAt(len - 1) == '\n') {
					myBuffer.delete(len - 1, len);
				}
				return myBuffer.toString();
			}
		}
		return null;
	}

	public boolean processNamespaces() {
		return true;
	}

	public void namespaceMapChangedHandler(HashMap<String,String> namespaceMap) {
		myDescriptionTag = null;
		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String id = entry.getValue();
			if (id.startsWith(DublinCorePrefix) || id.startsWith(DublinCoreLegacyPrefix)) {
				myDescriptionTag = entry.getKey() + ":description";
			}
		}
	}

	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if (tag.equalsIgnoreCase(myDescriptionTag)) {
			myReadState = READ_DESCRIPTION;
		} else if (myReadState == READ_DESCRIPTION) {
			// TODO: process tags
			myBuffer.append(" ");
		}
		return false;
	}

	public void characterDataHandler(char[] data, int start, int len) {
		if (myReadState == READ_DESCRIPTION) {
			myBuffer.append(new String(data, start, len).trim());
		}
	}

	public boolean endElementHandler(String tag) {
		if (myReadState != READ_DESCRIPTION) {
			return false;
		}
		tag = tag.toLowerCase();
		if (tag.equalsIgnoreCase(myDescriptionTag)) {
			return true;
		}
		// TODO: process tags
		myBuffer.append(" ");
		return false;
	}
}
