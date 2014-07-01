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

package org.geometerplus.fbreader.formats.external;

import java.io.*;

import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.encodings.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.Formats;

public class ExternalProgramFormatPlugin extends FormatPlugin {
	private FormatPlugin myInfoReader;

	public ExternalProgramFormatPlugin(String fileType) {
		super(fileType);
	}

	public ExternalProgramFormatPlugin(String fileType, FormatPlugin ir) {
		super(fileType);
		myInfoReader = ir;
	}

	public String getPackage() {
		return Formats.filetypeOption(supportedFileType()).getValue();
	}

	public ZLFile prepareFile(ZLFile f) {
		if (f.getPath().contains(":")) {
			try {
				String filepath = f.getPath();
				int p1 = filepath.lastIndexOf(":");
				String filename = filepath.substring(p1 + 1);
				final File dirFile = new File(Paths.tempDirectory());
				dirFile.mkdirs();
				String path = Paths.tempDirectory() + "/" + filename;
				OutputStream out = new FileOutputStream(path);

				int read = 0;
				byte[] bytes = new byte[1024];
				InputStream inp = f.getInputStream();

				while ((read = inp.read(bytes)) > 0) {
					out.write(bytes, 0, read);
				}

				out.flush();
				out.close();
				f = new ZLPhysicalFile(new File(path));
			} catch (IOException e) {
				f = null;
			}
		}
		return f;
	}

	@Override
	public Type type() {
		return Type.EXTERNAL_PROGRAM;
	}

	@Override
	public void readMetainfo(Book book) throws BookReadingException {
		if (myInfoReader != null) {
			myInfoReader.readMetainfo(book);
		}
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		if (myInfoReader != null) {
			return myInfoReader.readCover(file);
		}
		return null;
	}

	@Override
	public String readAnnotation(ZLFile file) {
		if (myInfoReader != null) {
			return myInfoReader.readAnnotation(file);
		}
		return null;
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
	}

	@Override
	public void readUids(Book book) throws BookReadingException {
		if (myInfoReader != null) {
			myInfoReader.readUids(book);
			return;
		}
		if (book.uids().isEmpty()) {
			book.addUid(BookUtil.createUid(book.File, "SHA-256"));
		}
	}
}
