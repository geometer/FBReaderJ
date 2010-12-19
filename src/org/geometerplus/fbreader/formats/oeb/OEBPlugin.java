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

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class OEBPlugin extends FormatPlugin {
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension().intern();
		return (extension == "opf") || (extension == "oebzip") || (extension == "epub");
	}

	private ZLFile getOpfFile(ZLFile oebFile) {
		if (oebFile.getExtension().equals("opf")) {
			return oebFile;
		}

		for (ZLFile child : oebFile.children()) {
			if (child.getExtension().equals("opf")) {
				return child;
			}
		}
		return null;
	}

	@Override
	public boolean readMetaInfo(Book book) {
		final ZLFile opfFile = getOpfFile(book.File);
		return (opfFile != null) ? new OEBMetaInfoReader(book).readMetaInfo(opfFile) : false;
	}
	
	@Override
	public boolean readModel(BookModel model) {
		model.Book.File.setCached(true);
		final ZLFile opfFile = getOpfFile(model.Book.File);
		return (opfFile != null) ? new OEBBookReader(model).readBook(opfFile) : false;
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		final ZLFile opfFile = getOpfFile(file);
		return (opfFile != null) ? new OEBCoverReader().readCover(opfFile) : null;
	}

	@Override
	public String readAnnotation(ZLFile file) {
		final ZLFile opfFile = getOpfFile(file);
		return (opfFile != null) ? new OEBAnnotationReader().readAnnotation(opfFile) : null;
	}
}
