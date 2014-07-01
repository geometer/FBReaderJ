/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.external;

import java.io.IOException;
import java.util.Collections;

import org.pdfparse.model.PDFDocInfo;
import org.pdfparse.model.PDFDocument;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public class PDFPlugin extends ExternalFormatPlugin {
	private final String PACKAGE = "org.geometerplus.fbreader.plugin.pdf";

	public PDFPlugin() {
		super("PDF");
	}

	@Override
	public String getPackage() {
		return PACKAGE;
	}

	@Override
	public void readMetainfo(Book book) {
		try {
			final PDFDocument doc = new PDFDocument(book.File.getPath());
			final PDFDocInfo info = doc.getDocumentInfo();
			book.setTitle(info.getTitle());
			book.setAuthors(Collections.singletonList(new Author(info.getAuthor(), "")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
