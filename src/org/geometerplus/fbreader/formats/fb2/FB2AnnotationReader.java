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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

public class FB2AnnotationReader extends ZLXMLReaderAdapter {
	private final static int READ_NOTHING = 0;
	private final static int READ_ANNOTATION = 1;

	private int myReadState = READ_NOTHING;
	private final StringBuilder myBuffer = new StringBuilder();

	public FB2AnnotationReader() {
	}
	
	public boolean dontCacheAttributeValues() {
		return true;
	}
	
	public String readAnnotation(ZLFile file) {
		myReadState = READ_NOTHING;
		myBuffer.delete(0, myBuffer.length());
		if (readDocument(file)) {
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

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.BODY:
				return true;
			case FB2Tag.ANNOTATION:
				myReadState = READ_ANNOTATION;
				break;
			default:
				if (myReadState == READ_ANNOTATION) {
					// TODO: add tag to buffer
					myBuffer.append(" ");
				}
				break;
		}
		return false;
	}
	
	public boolean endElementHandler(String tag) {
		if (myReadState != READ_ANNOTATION) {
			return false;
		}
		switch (FB2Tag.getTagByName(tag)) {
			case FB2Tag.ANNOTATION:
				return true;
			case FB2Tag.P:
				myBuffer.append("\n");
				break;
			default:
				// TODO: add tag to buffer
				myBuffer.append(" ");
				break;
		}
		return false;
	}
	
	public void characterDataHandler(char[] data, int start, int length) {
		if (myReadState == READ_ANNOTATION) {
			myBuffer.append(new String(data, start, length).trim());
		}
	}

	public boolean readDocument(ZLFile file) {
		return ZLXMLProcessor.read(this, file, 512);
	}
}
