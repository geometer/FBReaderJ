/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.IOException;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

public class FB2AnnotationReader extends ZLXMLReaderAdapter {
	private final static int READ_NOTHING = 0;
	private final static int READ_ANNOTATION = 1;

	private int myReadState = READ_NOTHING;
	private final StringBuilder myBuffer = new StringBuilder();

	public FB2AnnotationReader() {
	}

	@Override
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

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if ("body".equalsIgnoreCase(tag)) {
			return true;
		} else if ("annotation".equalsIgnoreCase(tag)) {
			myReadState = READ_ANNOTATION;
		} else if (myReadState == READ_ANNOTATION) {
			// TODO: add tag to buffer
			myBuffer.append(" ");
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		if (myReadState != READ_ANNOTATION) {
			return false;
		}
		if ("annotation".equalsIgnoreCase(tag)) {
			return true;
		} else if ("p".equalsIgnoreCase(tag)) {
			myBuffer.append("\n");
		} else {
			// TODO: add tag to buffer
			myBuffer.append(" ");
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int length) {
		if (myReadState == READ_ANNOTATION) {
			myBuffer.append(new String(data, start, length).trim());
		}
	}

	private boolean readDocument(ZLFile file) {
		try {
			ZLXMLProcessor.read(this, file, 512);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
