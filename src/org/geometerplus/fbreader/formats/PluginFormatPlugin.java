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

import java.io.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.encodings.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.Paths;

public abstract class PluginFormatPlugin extends FormatPlugin {
	public PluginFormatPlugin(String fileType) {
		super(fileType);
	}

	public abstract String getPackage();
	
	@Override
	public Type type() {
		return Type.PLUGIN;
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		// TODO: throw an "unsupported operation" exception
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
	}
}
