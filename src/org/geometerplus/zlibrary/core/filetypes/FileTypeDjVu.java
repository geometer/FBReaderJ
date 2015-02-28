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

class FileTypeDjVu extends FileType {
	FileTypeDjVu() {
		super("DjVu");
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension();
		return "djvu".equalsIgnoreCase(extension) || "djv".equalsIgnoreCase(extension);
	}

	@Override
	public List<MimeType> mimeTypes() {
		return MimeType.TYPES_DJVU;
	}

	@Override
	public MimeType mimeType(ZLFile file) {
		return acceptsFile(file) ? MimeType.IMAGE_VND_DJVU : MimeType.NULL;
	}

	@Override
	public String defaultExtension(MimeType mime) {
		return "djvu";
	}
}
