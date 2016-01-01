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

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

public class FileTypeCollection {
	public static final FileTypeCollection Instance = new FileTypeCollection();

	private final TreeMap<String,FileType> myTypes = new TreeMap<String,FileType>();

	private FileTypeCollection() {
		addType(new FileTypeFB2());
		addType(new FileTypeEpub());
		addType(new FileTypeMobipocket());
		addType(new FileTypeHtml());
		addType(new SimpleFileType("txt", "txt", MimeType.TYPES_TXT));
		addType(new SimpleFileType("RTF", "rtf", MimeType.TYPES_RTF));
		addType(new SimpleFileType("PDF", "pdf", MimeType.TYPES_PDF));
		addType(new FileTypeDjVu());
		addType(new FileTypeCBZ());
		addType(new SimpleFileType("ZIP archive", "zip", Collections.singletonList(MimeType.APP_ZIP)));
		addType(new SimpleFileType("msdoc", "doc", MimeType.TYPES_DOC));
	}

	private void addType(FileType type) {
		myTypes.put(type.Id.toLowerCase(), type);
	}

	public Collection<FileType> types() {
		return myTypes.values();
	}

	public FileType typeById(String id) {
		return myTypes.get(id.toLowerCase());
	}

	public FileType typeForFile(ZLFile file) {
		for (FileType type : types()) {
			if (type.acceptsFile(file)) {
				return type;
			}
		}
		return null;
	}

	public FileType typeForMime(MimeType mime) {
		if (mime == null) {
			return null;
		}
		mime = mime.clean();
		for (FileType type : types()) {
			if (type.mimeTypes().contains(mime)) {
				return type;
			}
		}
		return null;
	}

	public MimeType mimeType(ZLFile file) {
		for (FileType type : types()) {
			final MimeType mime = type.mimeType(file);
			if (mime != MimeType.NULL) {
				return mime;
			}
		}
		return MimeType.UNKNOWN;
	}

	public MimeType rawMimeType(ZLFile file) {
		for (FileType type : types()) {
			final MimeType mime = type.rawMimeType(file);
			if (mime != MimeType.NULL) {
				return mime;
			}
		}
		return MimeType.UNKNOWN;
	}
}
