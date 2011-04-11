/*
 * Copyright (C) 2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class NativeFormatPlugin extends FormatPlugin {

	private final long myNativePointer;

	public NativeFormatPlugin(long ptr) {
		myNativePointer = ptr;
	}

	public final long getNativePointer() {
		return myNativePointer;
	}

	@Override
	public native boolean acceptsFile(ZLFile file);

	@Override
	public native boolean readMetaInfo(Book book);

	@Override
	public native boolean readModel(BookModel model);

	@Override
	public native ZLImage readCover(ZLFile file);

	@Override
	public native String readAnnotation(ZLFile file);
}
