/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;

public abstract class NativeFormatPlugin extends FormatPlugin {
	private static Object ourCoversLock = new Object();

	@Override
	public native boolean acceptsFile(ZLFile file);

	@Override
	public native boolean readMetaInfo(Book book);

	@Override
	public native boolean readLanguageAndEncoding(Book book);

	@Override
	public native boolean readModel(BookModel model);

	@Override
	public ZLImage readCover(final ZLFile file) {
		return new ZLImageProxy() {
			@Override
			public int sourceType() {
				return SourceType.DISK;
			}

			@Override
			public String getId() {
				return file.getPath();
			}

			@Override
			public ZLSingleImage getRealImage() {
				// Synchronized block is needed because of use of temporary storage files;
				synchronized (ourCoversLock) {
					return (ZLSingleImage)readCoverInternal(file);
				}
			}
		};
	}

	protected native ZLImage readCoverInternal(ZLFile file);

	@Override
	public Type type() {
		return Type.NATIVE;
	}
}
