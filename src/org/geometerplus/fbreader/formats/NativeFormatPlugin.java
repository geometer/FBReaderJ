/*
 * Copyright (C) 2011-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import org.geometerplus.zlibrary.core.util.SystemInfo;
import org.geometerplus.zlibrary.text.model.CachedCharStorageException;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.fb2.FB2NativePlugin;
import org.geometerplus.fbreader.formats.oeb.OEBNativePlugin;

public class NativeFormatPlugin extends BuiltinFormatPlugin {
	private static final Object ourNativeLock = new Object();

	public static NativeFormatPlugin create(SystemInfo systemInfo, String fileType) {
		if ("fb2".equals(fileType)) {
			return new FB2NativePlugin(systemInfo);
		} else if ("ePub".equals(fileType)) {
			return new OEBNativePlugin(systemInfo);
		} else {
			return new NativeFormatPlugin(systemInfo, fileType);
		}
	}

	protected NativeFormatPlugin(SystemInfo systemInfo, String fileType) {
		super(systemInfo, fileType);
	}

	@Override
	synchronized public void readMetainfo(AbstractBook book) throws BookReadingException {
		final int code;
		synchronized (ourNativeLock) {
			code = readMetainfoNative(book);
		}
		if (code != 0) {
			throw new BookReadingException(
				"nativeCodeFailure",
				BookUtil.fileByBook(book),
				new String[] { String.valueOf(code), book.getPath() }
			);
		}
	}

	private native int readMetainfoNative(AbstractBook book);

	@Override
	public List<FileEncryptionInfo> readEncryptionInfos(AbstractBook book) {
		final FileEncryptionInfo[] infos;
		synchronized (ourNativeLock) {
			infos = readEncryptionInfosNative(book);
		}
		return infos != null
			? Arrays.<FileEncryptionInfo>asList(infos)
			: Collections.<FileEncryptionInfo>emptyList();
	}

	private native FileEncryptionInfo[] readEncryptionInfosNative(AbstractBook book);

	@Override
	synchronized public void readUids(AbstractBook book) throws BookReadingException {
		synchronized (ourNativeLock) {
			readUidsNative(book);
		}
		if (book.uids().isEmpty()) {
			book.addUid(BookUtil.createUid(book, "SHA-256"));
		}
	}

	private native boolean readUidsNative(AbstractBook book);

	@Override
	public void detectLanguageAndEncoding(AbstractBook book) {
		synchronized (ourNativeLock) {
			detectLanguageAndEncodingNative(book);
		}
	}

	private native void detectLanguageAndEncodingNative(AbstractBook book);

	@Override
	synchronized public void readModel(BookModel model) throws BookReadingException {
		final int code;
		final String tempDirectory = SystemInfo.tempDirectory();
		synchronized (ourNativeLock) {
			code = readModelNative(model, tempDirectory);
		}
		switch (code) {
			case 0:
				return;
			case 3:
				throw new CachedCharStorageException(
					"Cannot write file from native code to " + tempDirectory
				);
			default:
				throw new BookReadingException(
					"nativeCodeFailure",
					BookUtil.fileByBook(model.Book),
					new String[] { String.valueOf(code), model.Book.getPath() }
				);
		}
	}

	private native int readModelNative(BookModel model, String cacheDir);

	@Override
	public final ZLFileImageProxy readCover(ZLFile file) {
		return new ZLFileImageProxy(file) {
			@Override
			protected ZLFileImage retrieveRealImage() {
				final ZLFileImage[] box = new ZLFileImage[1];
				synchronized (ourNativeLock) {
					readCoverNative(File, box);
				}
				return box[0];
			}
		};
	}

	private native void readCoverNative(ZLFile file, ZLFileImage[] box);

	@Override
	public String readAnnotation(ZLFile file) {
		synchronized (ourNativeLock) {
			return readAnnotationNative(file);
		}
	}

	private native String readAnnotationNative(ZLFile file);

	@Override
	public int priority() {
		return 5;
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
