/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.EncodingCollection;
import org.geometerplus.fbreader.library.Book;

public class ExternalFormatPlugin extends FormatPlugin {
	private static class DefaultInfoReader implements InfoReader {
		public void readMetaInfo(Book book) {
		}
		public ZLImage readCover(ZLFile file) {
			return null;
		}
		public String readAnnotation(ZLFile file) {
			return null;
		}
	}

	private InfoReader myInfoReader;

	ExternalFormatPlugin(String fileType) {
		super(fileType);
		myInfoReader = new DefaultInfoReader();
	}

	ExternalFormatPlugin(String fileType, InfoReader ir) {
		super(fileType);
		myInfoReader = ir;
	}

	public String getPackage() {
		return Formats.filetypeOption(supportedFileType()).getValue();
	}

	@Override
	public Type type() {
		return Type.EXTERNAL;
	}

	@Override
	public void readMetaInfo(Book book) throws BookReadingException {
		myInfoReader.readMetaInfo(book);
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		// TODO: throw an "unsupported operation" exception
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		return myInfoReader.readCover(file);
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return myInfoReader.readAnnotation(file);
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
	}
}
