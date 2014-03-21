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

package org.geometerplus.fbreader.formats.oeb;

import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.encodings.AutoEncodingCollection;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.JavaFormatPlugin;

public class OEBPlugin extends JavaFormatPlugin {
	public OEBPlugin() {
		super("ePub");
	}

	private ZLFile getOpfFile(ZLFile oebFile) throws BookReadingException {
		if ("opf".equals(oebFile.getExtension())) {
			return oebFile;
		}

		final ZLFile containerInfoFile = ZLFile.createFile(oebFile, "META-INF/container.xml");
		if (containerInfoFile.exists()) {
			final ContainerFileReader reader = new ContainerFileReader();
			reader.readQuietly(containerInfoFile);
			final String opfPath = reader.getRootPath();
			if (opfPath != null) {
				return ZLFile.createFile(oebFile, opfPath);
			}
		}

		for (ZLFile child : oebFile.children()) {
			if (child.getExtension().equals("opf")) {
				return child;
			}
		}
		throw new BookReadingException("opfFileNotFound", oebFile);
	}

	@Override
	public void readMetaInfo(Book book) throws BookReadingException {
		new OEBMetaInfoReader(book).readMetaInfo(getOpfFile(book.File));
	}

	@Override
	public String readEncryptionMethod(Book book) {
		return EncryptionMethod.NONE;
	}

	@Override
	public void readUids(Book book) throws BookReadingException {
		// this method does nothing, we expect it will be never called
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		model.Book.File.setCached(true);
		new OEBBookReader(model).readBook(getOpfFile(model.Book.File));
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		try {
			return new OEBCoverReader().readCover(getOpfFile(file));
		} catch (BookReadingException e) {
			return null;
		}
	}

	@Override
	public String readAnnotation(ZLFile file) {
		try {
			return new OEBAnnotationReader().readAnnotation(getOpfFile(file));
		} catch (BookReadingException e) {
			return null;
		}
	}

	@Override
	public AutoEncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
		book.setEncoding("auto");
	}
}
