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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.encodings.EncodingCollection;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;

public abstract class FormatPlugin {
	private final String myFileType;

	protected FormatPlugin(String fileType) {
		myFileType = fileType;
	}

	public final String supportedFileType() {
		return myFileType;
	}

	public ZLFile realBookFile(ZLFile file) throws BookReadingException {
		return file;
	}
	public abstract void readMetaInfo(Book book) throws BookReadingException;
	public abstract String readEncryptionMethod(Book book);
	public abstract void readUids(Book book) throws BookReadingException;
	public abstract void readModel(BookModel model) throws BookReadingException;
	public abstract void detectLanguageAndEncoding(Book book) throws BookReadingException;
	public abstract ZLImage readCover(ZLFile file);
	public abstract String readAnnotation(ZLFile file);

	public enum Type {
		ANY(false),
		JAVA(true),
		NATIVE(true),
		PLUGIN(false),
		EXTERNAL(false),
		NONE(false);

		public final boolean Builtin;

		Type(boolean builtin) {
			Builtin = builtin;
		}
	};
	public abstract Type type();

	public abstract EncodingCollection supportedEncodings();
}
