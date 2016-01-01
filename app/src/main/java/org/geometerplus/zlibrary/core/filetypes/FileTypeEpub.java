/*
 * Copyright (C) 2012-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.filetypes;

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

class FileTypeEpub extends FileType {
	FileTypeEpub() {
		super("ePub");
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension();
		return
			"epub".equalsIgnoreCase(extension) ||
			"oebzip".equalsIgnoreCase(extension) ||
			("opf".equalsIgnoreCase(extension) && file != file.getPhysicalFile());
	}

	@Override
	public List<MimeType> mimeTypes() {
		return MimeType.TYPES_EPUB;
	}

	@Override
	public MimeType mimeType(ZLFile file) {
		final String extension = file.getExtension();
		if ("epub".equalsIgnoreCase(extension)) {
			return MimeType.APP_EPUB_ZIP;
		}
		// TODO: process other extensions (?)
		return MimeType.NULL;
	}

	@Override
	public MimeType rawMimeType(ZLFile file) {
		final String extension = file.getExtension();
		if ("epub".equalsIgnoreCase(extension)) {
			return MimeType.APP_ZIP;
		}
		// TODO: process other extensions (?)
		return MimeType.NULL;
	}

	@Override
	public String defaultExtension(MimeType mime) {
		return "epub";
	}
}
