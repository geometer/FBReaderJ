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

package org.geometerplus.fbreader.formats.html;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.library.Book;

public class HtmlMetaInfoReader extends ZLXMLReaderAdapter {
	private final Book myBook;

	private boolean myReadTitle;

	public HtmlMetaInfoReader(Book book) {
		myBook = book;
		myBook.setTitle("");
	}

	public boolean dontCacheAttributeValues() {
		return true;
	}

	public boolean readMetaInfo() {
		myReadTitle = false;
		return readDocument(myBook.File);
	}

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (HtmlTag.getTagByName(tagName)) {
			case HtmlTag.TITLE:
				myReadTitle = true;
				break;
			default:
				break;
		}
		return false;
	}

	public boolean endElementHandler(String tag) {
		switch (HtmlTag.getTagByName(tag)) {
			case HtmlTag.TITLE:
				myReadTitle = false;
				break;
			default:
				break;
		}
		return false;
	}

	public void characterDataHandler(char[] ch, int start, int length) {
		// TODO + length -- remove
		final String text = new String(ch).substring(start, start + length);
		if (myReadTitle) {
			myBook.setTitle(myBook.getTitle() + text);
		}
	}

	public boolean readDocument(ZLFile file) {
		return ZLXMLProcessor.read(this, file);
	}

}
