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

package org.geometerplus.fbreader.formats.fb2;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.encodings.EncodingCollection;
import org.geometerplus.zlibrary.core.encodings.AutoEncodingCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.SystemInfo;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.NativeFormatPlugin;

public class FB2NativePlugin extends NativeFormatPlugin {
	public FB2NativePlugin(SystemInfo systemInfo) {
		super(systemInfo, "fb2");
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		super.readModel(model);
		model.setLabelResolver(new BookModel.LabelResolver() {
			public List<String> getCandidates(String id) {
				final List<String> candidates = new ArrayList<String>();
				try {
					final String c = URLDecoder.decode(id, "utf-8");
					if (c != null && !c.equals(id)) {
						candidates.add(c);
					}
				} catch (Exception e) {
					// ignore
				}
				try {
					final String c = URLDecoder.decode(id, "windows-1251");
					if (c != null && !c.equals(id)) {
						candidates.add(c);
					}
				} catch (Exception e) {
					// ignore
				}
				return candidates;
			}
		});
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
	public void detectLanguageAndEncoding(AbstractBook book) {
		book.setEncoding("auto");
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return new FB2AnnotationReader().readAnnotation(file);
	}

	@Override
	public int priority() {
		return 0;
	}
}
