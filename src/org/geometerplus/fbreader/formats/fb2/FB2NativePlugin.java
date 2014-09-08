/*
 * Copyright (C) 2011-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;

import org.geometerplus.zlibrary.core.encodings.EncodingCollection;
import org.geometerplus.zlibrary.core.encodings.AutoEncodingCollection;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.NativeFormatPlugin;

public class FB2NativePlugin extends NativeFormatPlugin {
	public FB2NativePlugin() {
		super("fb2");
	}

	@Override
	public ZLFile realBookFile(ZLFile file) throws BookReadingException {
		final ZLFile realFile = getRealFB2File(file);
		if (realFile == null) {
			throw new BookReadingException("incorrectFb2ZipFile", file);
		}
		return realFile;
	}

	private static ZLFile getRealFB2File(ZLFile file) {
		final String name = file.getShortName().toLowerCase();
		if (name.endsWith(".fb2.zip") && file.isArchive()) {
			final List<ZLFile> children = file.children();
			if (children == null) {
				return null;
			}
			ZLFile candidate = null;
			for (ZLFile item : children) {
				if ("fb2".equals(item.getExtension())) {
					if (candidate == null) {
						candidate = item;
					} else {
						return null;
					}
				}
			}
			return candidate;
		} else {
			return file;
		}
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
		book.setEncoding("auto");
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return new FB2AnnotationReader().readAnnotation(file);
	}
}
