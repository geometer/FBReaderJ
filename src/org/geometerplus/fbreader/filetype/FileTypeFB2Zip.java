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

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.MimeType;

class FileTypeFB2Zip extends FileType {
	FileTypeFB2Zip() {
		super("fb2.zip");
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		// TODO: implement
		return false;
	}

	@Override
	public String extension() {
		return "fb2.zip";
	}

	@Override
	public List<MimeType> mimeTypes() {
		return MimeType.TYPES_FB2_ZIP;
	}
}
