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

class SimpleFileType extends FileType {
	private final String myExtension;
	private final List<MimeType> myMimeTypes;

	SimpleFileType(String id, String extension, List<MimeType> mimeTypes) {
		super(id);
		myExtension = extension;
		myMimeTypes = mimeTypes;
	}

	@Override
	public boolean acceptsFile(ZLFile file) {
		return myExtension.equalsIgnoreCase(file.getExtension());
	}

	@Override
	public List<MimeType> mimeTypes() {
		return myMimeTypes;
	}

	@Override
	public MimeType mimeType(ZLFile file) {
		return acceptsFile(file) ? myMimeTypes.get(0) : MimeType.NULL;
	}

	@Override
	public String defaultExtension(MimeType mime) {
		return myExtension;
	}

	@Override
	public String toString() {
		return "SimpleFileType [" + Id + "]";
	}
}
