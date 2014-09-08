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

package org.geometerplus.fbreader.formats;

import java.util.*;

import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.encodings.EncodingCollection;
import org.geometerplus.zlibrary.core.encodings.JavaEncodingCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;
import org.geometerplus.zlibrary.text.model.CachedCharStorageException;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.fb2.FB2NativePlugin;
import org.geometerplus.fbreader.formats.oeb.OEBNativePlugin;

public class NativeFormatPlugin extends BuiltinFormatPlugin {
	public static NativeFormatPlugin create(String fileType) {
		if ("fb2".equals(fileType)) {
			return new FB2NativePlugin();
		} else if ("ePub".equals(fileType)) {
			return new OEBNativePlugin();
		} else {
			return new NativeFormatPlugin(fileType);
		}
	}

	protected NativeFormatPlugin(String fileType) {
		super(fileType);
	}

	@Override
	synchronized public void readMetainfo(Book book) throws BookReadingException {
		final int code = readMetainfoNative(book);
		if (code != 0) {
			throw new BookReadingException(
				"nativeCodeFailure",
				book.File,
				new String[] { String.valueOf(code), book.File.getPath() }
			);
		}
	}

	private native int readMetainfoNative(Book book);

	@Override
	public List<FileEncryptionInfo> readEncryptionInfos(Book book) {
		final FileEncryptionInfo[] infos = readEncryptionInfosNative(book);
		return infos != null
			? Arrays.<FileEncryptionInfo>asList(infos)
			: Collections.<FileEncryptionInfo>emptyList();
	}

	private native FileEncryptionInfo[] readEncryptionInfosNative(Book book);

	@Override
	synchronized public void readUids(Book book) throws BookReadingException {
		readUidsNative(book);
		if (book.uids().isEmpty()) {
			book.addUid(BookUtil.createUid(book.File, "SHA-256"));
		}
	}

	private native boolean readUidsNative(Book book);

	@Override
	public void detectLanguageAndEncoding(Book book) {
		detectLanguageAndEncodingNative(book);
	}

	public native void detectLanguageAndEncodingNative(Book book);

	@Override
	synchronized public void readModel(BookModel model) throws BookReadingException {
		//android.os.Debug.startMethodTracing("ep.trace", 32 * 1024 * 1024);
		final int code = readModelNative(model);
		//android.os.Debug.stopMethodTracing();
		switch (code) {
			case 0:
				return;
			case 3:
				throw new CachedCharStorageException("Cannot write file from native code");
			default:
				throw new BookReadingException(
					"nativeCodeFailure",
					model.Book.File,
					new String[] { String.valueOf(code), model.Book.File.getPath() }
				);
		}
	}

	private native int readModelNative(BookModel model);

	@Override
	public ZLImage readCover(ZLFile file) {
		return new ZLImageFileProxy(file) {
			@Override
			protected ZLImage retrieveRealImage() {
				final ZLImage[] box = new ZLImage[1];
				readCoverInternal(File, box);
				return box[0];
			}
		};
	}

	protected native void readCoverInternal(ZLFile file, ZLImage[] box);

	@Override
	public String readAnnotation(ZLFile file) {
		return readAnnotationInternal(file);
	}

	protected native String readAnnotationInternal(ZLFile file);

	@Override
	public Type type() {
		return Type.BUILTIN;
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return JavaEncodingCollection.Instance();
	}

	@Override
	public String toString() {
		return "NativeFormatPlugin [" + supportedFileType() + "]";
	}
}
