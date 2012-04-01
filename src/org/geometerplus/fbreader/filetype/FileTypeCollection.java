/*
 * Copyright (C) 2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.filetype;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

public class FileTypeCollection {
	public static final FileTypeCollection Instance = new FileTypeCollection();

	private final TreeMap<String,FileType> myTypes = new TreeMap<String,FileType>();

	private FileTypeCollection() {
		addType(new SimpleFileType("fb2", "fb2", MimeType.TYPES_FB2));
		addType(new FileTypeEpub());
		addType(new FileTypeMobipocket());
		addType(new FileTypeHtml());
		addType(new SimpleFileType("plain text", "txt", MimeType.TYPES_TXT));
		addType(new SimpleFileType("RTF", "rtf", MimeType.TYPES_RTF));
		addType(new SimpleFileType("PDF", "pdf", MimeType.TYPES_PDF));
		addType(new FileTypeDjVu());
		addType(new FileTypeFB2Zip());
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
}
