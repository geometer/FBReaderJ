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

import org.geometerplus.zlibrary.core.encodings.AutoEncodingCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;

public abstract class ExternalFormatPlugin extends FormatPlugin {
	protected ExternalFormatPlugin(String fileType) {
		super(fileType);
	}

	@Override
	public Type type() {
		return Type.EXTERNAL;
	}

	public abstract String packageName();

	@Override
	public PluginImage readCover(ZLFile file) {
		return new PluginImage(file, this);
	}

	@Override
	public AutoEncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return null;
	}

	@Override
	public void readUids(Book book) {
		if (book.uids().isEmpty()) {
			book.addUid(BookUtil.createUid(book.File, "SHA-256"));
		}
	}
}
