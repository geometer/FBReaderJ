/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.pdb;

import java.io.*;

import org.geometerplus.zlibrary.core.html.ZLByteBuffer;
import org.geometerplus.zlibrary.core.html.ZLHtmlAttributeMap;

import org.geometerplus.fbreader.formats.html.HtmlReader;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.fbreader.bookmodel.BookModel;

public class MobipocketHtmlBookReader extends HtmlReader {
	MobipocketHtmlBookReader(BookModel model) throws UnsupportedEncodingException {
		super(model);
	}

	public InputStream getInputStream() throws IOException {
		return new MobipocketStream(Model.Book.File);
	}

	@Override
	public void startElementHandler(byte tag, int offset, ZLHtmlAttributeMap attributes) {
		switch (tag) {
			case HtmlTag.REFERENCE:
			{
				System.err.println("REFERENCE");
				final ZLByteBuffer fp = attributes.getValue("filepos");
				if (fp != null) {
					System.err.println(": filepos = " + fp);
				}
				final ZLByteBuffer title = attributes.getValue("title");
				if (title != null) {
					System.err.println(": title = " + title);
				}
				final ZLByteBuffer type = attributes.getValue("type");
				if (type != null) {
					System.err.println(": type = " + type);
				}
				break;
			}
			default:
			{
				//System.err.println("offset = " + offset);
				final ZLByteBuffer fp = attributes.getValue("filepos");
				if (fp != null) {
					System.err.println("filepos = " + fp);
				}
				super.startElementHandler(tag, offset, attributes);
				break;
			}
		}
	}
}
