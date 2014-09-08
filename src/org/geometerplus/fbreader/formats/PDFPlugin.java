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

package org.geometerplus.fbreader.formats;

import org.pdfparse.model.PDFDocInfo;
import org.pdfparse.model.PDFDocument;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public class PDFPlugin extends ExternalFormatPlugin {
	public PDFPlugin() {
		super("PDF");
	}

	@Override
	public String packageName() {
		return "org.geometerplus.fbreader.plugin.pdf";
	}

	@Override
	public void readMetainfo(Book book) {
		final ZLFile file = book.File;
		if (file != file.getPhysicalFile()) {
			// TODO: throw BookReadingException
			System.err.println("Only physical PDF files are supported");
			return;
		}
		try {
			final PDFDocument doc = new PDFDocument(book.File.getPath());
			// TODO: solution for rc4 encryption
			if (!doc.isEncrypted()) {
				final PDFDocInfo info = doc.getDocumentInfo();
				book.setTitle(info.getTitle());
				book.addAuthor(info.getAuthor());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
