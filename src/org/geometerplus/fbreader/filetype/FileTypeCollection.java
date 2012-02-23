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
		addType(new FileTypeByExtension("fb2", "fb2", MimeType.TEXT_FB2));
		addType(new FileTypeEpub());
		addType(new FileTypeMobipocket());
		addType(new FileTypeHtml());
		addType(new FileTypeByExtension("plain text", "txt", MimeType.TEXT_PLAIN));
		// TODO: change mime type
		addType(new FileTypeByExtension("RTF", "rtf", MimeType.TEXT_PLAIN));
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
